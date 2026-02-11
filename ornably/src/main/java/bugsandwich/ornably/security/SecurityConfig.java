package bugsandwich.ornably.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

/*스프링 시큐리티 = 보안동작을 조립하는 설계도
 * - 어떤 URL을 누가 접근할 수 있는지(permitAll / authenticated / 권한) 정의한다.
 * - React(프론트) <-> Spring(백엔드) 포트가 다르면 CORS 설정이 필수다.
 * - 세션(JSESSIONID)을 쓰면 쿠키가 오가야 하므로 CORS + credentials 설정이 중요하다.*/


@Configuration
//이 클래스가 스프링 설정 클래스임을 의미
@EnableWebSecurity
//Spring Security를 활성화하여 보안 필터 체인을 적용
public class SecurityConfig {
	//커스텀 설정들 사용가능하게 멤버 변수로 설정
	private final CustomOAuth2UserService customOAuth2UserService;
	//SNS에서 받아온 사용자 정보들을 우리서비스 principal(loginuser)로 변환하는역할
	private final LoginSuccessHandler loginSuccessHandler;
	//로그인 성공후 (일반/소셜) 무엇을 응답할지 결정한다 권한에 따라 다음 경로를 내려주거나 리다이렉트 할수있다
	//로그인 실패후 상태에 따라 에러코드 띄워줌
	

	//DI 생성자 주입 사용
	public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
			LoginSuccessHandler loginSuccessHandler) {
		this.customOAuth2UserService = customOAuth2UserService;
		this.loginSuccessHandler = loginSuccessHandler;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12); // 비밀번호 보안 강도
		//BCryptPasswordEncoder
		//사용자가 입력한 비밀번호를 안전하게 변환하여 DB에 저장 -> $2a$12$.7vZbzd.A5WVUNwTO05MSuxWqWMddlQ6GkOMVty6OOo/SZ7UVGoe.
		//로그인 시 비밀번호를 다시 변환해 저장된 값과 비교함으로써 일치 여부를 확인함
	}
	@Bean
	//CORS 설정 
	//리액트에서 스프링으로 요청하면 브라우저가 오리진이 다르다고 판단한다
	//이때 서버가 cors를 허용해주지 않는다면 브라우저가 응답을 막는다
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		//허용할 프론트 개발서버 주소
		config.setAllowedOrigins(List.of("http://localhost:5173"));
		//프론트에서 사용할 HTTP메서드 허용하기
		config.setAllowedMethods(List.of("*"));
		//프론트가 보내는 헤더 허용하기
		config.setAllowedHeaders(List.of("*"));
		//세션쿠키(JSESSIONID)를 포함한 요청을 허용한다
		config.setAllowCredentials(true);

		//모든경로에 CORS를 적용한다
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}

	@Bean
	// SecurityFilterChain 객체를 Bean으로 등록
	// Spring Security의 모든 요청 흐름은 이 필터 체인을 기준으로 동작
	//여기서 CORS/CSRF/권한/로그인/로그아웃 정책을 한 번에 정의한다.
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
		//스프링 시큐리티에 cors 적용
		.cors(cors -> cors.configurationSource(corsConfigurationSource()))
		.csrf(csrf -> csrf.disable())
		// CSRF(Cross Site Request Forgery) 보호 비활성화
		// REST/API 기반이거나 OAuth2 로그인 테스트 단계에서는 보통 비활성화
		//로그인된 사용자의 브라우저가 사용자가 원하지 않는 요청을 다른 사이트에서 몰래 보내는 공격을 막기 위해 쓰는 보호장치

		// 요청 URL별 접근 권한 설정
		.authorizeHttpRequests(auth -> auth

				//프리플라이트 옵션 허용
				//브라우저가 cors 허용여부를 확인하기 위해 먼저 options 요청을 보낸다
				//options를 막으면 cors 에러 처럼 보이면서 api 호출에 실패한다
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

				// ✅ React 페이지/정적리소스는 열어두기
				.requestMatchers("/", "/index.html", "/images/**", "/favicon.ico", "/error").permitAll()
				
				//OAuth2 진입 콜백 허용
				.requestMatchers("/oauth2/**","/login/oauth2/**").permitAll()

				// /api/all/** -> 공개, 누구나가능
				.requestMatchers("/api/all/**").permitAll()

				// 비로그인만 가능 /api/guest/** = authenticated == false
				.requestMatchers("/api/guest/**").anonymous()

				//소셜로 최초로그인시 우리서비스에 필요한 정보를 받기위해 온보드라는 권한을 줌
				//정보입력페이지만 접근 가능
				.requestMatchers("/api/onboard/**").hasRole("ONBOARD")

				// /api/user/** = 로그인한 회원만 사용가능
				.requestMatchers("/api/user/**").hasRole("USER")

				// /api/admin/** = 관리자만 사용가능
				.requestMatchers("/api/admin/**").hasRole("ADMIN")

				// 위에서 허용한 경로를 제외한 모든 요청은 인증이 필요하다
				.anyRequest().authenticated()
				)

		//일반 로그인(폼 로그인) 설정
		//리액트에서 로그인 요청을 보내고 서버는 세션(JSESSIONID)을 발급하는 구조가 된다
		.formLogin(form -> form
				// 실제 로그인 요청 (POST)
				//아이디 비번을 보내면 시큐리티 필터가 인증을 진행한다
				.loginProcessingUrl("/login")
				.successHandler(loginSuccessHandler)//로그인 성공시 권한에 따라 상태 분기
				.failureHandler((req, res, auth) -> {
					res.setStatus(401);
				}) // 로그인 실패시 에러코드 띄움
				.permitAll() //누구나 접근 가능
				)

		// OAuth2 로그인 설정
		//카카오와 구글 로그인 성공시 동일하게 세션(JSESSIONID)을 만든다
		.oauth2Login(oauth -> oauth
				// → 인증이 필요할 때 Spring Security의 기본 OAuth2 흐름 사용
				.loginPage("/login") // 인증필요시 로그인 페이지로 이동 
				//sns에서 받아온 사용자 정보를 우리 Principal로 가공하는 로직을 연결함
				.userInfoEndpoint(userInfo -> userInfo
						.userService(customOAuth2UserService))
				// OAuth2 로그인 성공 시 권한에 따라 상태분기
				.successHandler((req, res, auth) -> {
	                   res.sendRedirect("http://localhost:5173/");
	              })
				)

		// 로그아웃 설정
		//로그아웃 시점에서 세션무효화 + 쿠키삭제 진행
		//상태코드 200과 json을 주고 프론트가 처리하는 방식이 자연스럽다
		.logout(logout -> logout
				.logoutUrl("/logout") //로그아웃 처리할 URL지정
				.invalidateHttpSession(true) // 세션무효화
				.clearAuthentication(true) //인증정보 제거
				.deleteCookies("JSESSIONID") // 세션 식별에 사용되는 JSESSIONID 쿠키 삭제
				.logoutSuccessUrl("http://localhost:5173/"));

		// 설정이 완료된 SecurityFilterChain 반환
		return http.build();
	}
}