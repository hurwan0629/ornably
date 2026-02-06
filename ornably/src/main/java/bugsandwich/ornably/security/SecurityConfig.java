package bugsandwich.ornably.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	 private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

	public SecurityConfig( CustomOAuth2UserService customOAuth2UserService,
			OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
		this.customOAuth2UserService = customOAuth2UserService;
		this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12); // 암호화 강도
	}
	
	@Bean	
    CorsConfigurationSource corsConfigurationSource() {
	  CorsConfiguration config = new CorsConfiguration();
	  config.addAllowedOrigin("http://localhost:5173");
	  config.addAllowedMethod("*");
	  config.addAllowedHeader("*");
	  config.setAllowCredentials(true); // 세션쿠키 허용(중요)

	  UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	  source.registerCorsConfiguration("/**", config);
	  return source;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
		.cors(cors -> cors.configurationSource(corsConfigurationSource()))
		.csrf(csrf -> csrf.disable()) // MVP면 OK (나중에 보강)
			.authorizeHttpRequests(auth -> auth

			    // ✅ /api/all/** : 누구나 접근 가능
			    .requestMatchers("/api/all/**").permitAll()

			    // ✅ /api/guest/** : "비로그인"만 접근 가능 (로그인하면 접근 불가)
			    .requestMatchers("/api/guest/**").anonymous()

			    // ✅ /api/onboard/** : ONBOARD만 접근 가능
			    .requestMatchers("/api/onboard/**").hasRole("ONBOARD")

			    // ✅ /api/user/** : USER만 접근 가능
			    .requestMatchers("/api/user/**").hasRole("USER")

			    // ✅ /api/admin/** : ADMIN만 접근 가능
			    .requestMatchers("/api/admin/**").hasRole("ADMIN")

			    // 나머지는 기본적으로 인증 필요
			    .anyRequest().authenticated()
			)

	          // ✅ 폼로그인은 "리다이렉트" 대신 JSON 응답 권장
	          .formLogin(form -> form 
	              .loginProcessingUrl("/login").permitAll()
	              .successHandler((req, res, auth) -> {
	            	  		System.out.println("폼 로그인 성공! 사용자 이름:["+auth.getName()+"]");
	                  res.setStatus(200); // 200 ok -> 잘됨이라는
	              })	
	              .failureHandler((req, res, ex) -> {
	                  res.setStatus(401); // 401 unauthorized = 로그인 안됨  |  403 = 로그인은 됐지만 권한이 없어
	              })
	          )

	          // ✅ OAuth2는 브라우저 리다이렉트가 필요하니 성공 후 프론트로 보내기
	          .oauth2Login(oauth2 -> oauth2
	              .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
	              .successHandler((req, res, auth) -> {
	                   res.sendRedirect("http://localhost:5173/");
	              })
	          )

	          .logout(logout -> logout
	              .logoutUrl("/logout")
	              .logoutSuccessUrl("http://localhost:5173/")
	              .invalidateHttpSession(true)
	              .clearAuthentication(true)
	              .deleteCookies("JSESSIONID")
	          );
		return http.build();
	}
}