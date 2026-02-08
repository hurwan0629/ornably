package bugsandwich.ornably.security;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component // @컴포넌트로 객체생성
//AuthenticationSuccessHandler를 상속받은 LoginSuccessHandler를 사용하여
//(일반 폼/sns)로그인 성공 직후에 해야할일을 처리한다
public class LoginSuccessHandler implements AuthenticationSuccessHandler{

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		response.setStatus(200);
		//현재 로그인한 사용자의 권한 목록을 문자열로 변환한다
		//["USER"] ["ONBOARD"] ["ADMIN"]
		// Set<String> authorities 권한 문자열들을 모아둠
		/*
		Set<String> roles = authentication.getAuthorities().stream()
				.map(GrantedAuthority:: getAuthority) // GrantedAuthority 객체에서 문자열만 꺼냄
				.collect(Collectors.toSet());
		
		//권한에 따라 프론트(뷰)가 이동할 다음 경로를 정한다
		//우선순위 : ADMIN -> USER -> ONBOARD
		//String next;
		
		if(roles.contains("ROLE_ADMIN")) { //관리자권한이 있으면 true 없으면 false
			response.sendRedirect("http://localhost:5173/admin");//관리자페이지로 이동
			return;
		}else if (roles.contains("ROLE_USER")) {
			response.sendRedirect("http://localhost:5173/");//권한이 유저면 메인페이지로 이동
			return;
		}else if (roles.contains("ROLE_ONBOARD")){
			response.sendRedirect("http://localhost:5173/onboard"); //권한이 온보드면 온보드 페이지로 이동
			return;
		}else { // 권한이 비어있거나 예상 밖일때 로그인 페이지로 이동
			response.sendRedirect("http://localhost:5173/login?error=role");
			return;
		}*/
	}
}
