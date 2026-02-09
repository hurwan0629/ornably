package bugsandwich.ornably.security;

import java.io.IOException;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//로그인실패시 응답을 json 형식으로 통일 하기 위한 핸들러
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler  {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		//기본 401 = 아이디 비번 불일치
		
		int status = HttpServletResponse.SC_UNAUTHORIZED; // 상태코드 401
		String code = "LOGIN_FAILED";
		String message = "아이디 또는 비밀번호가 올바르지 않습니다.";
		
		
		/* ✅ 계정이 비활성/잠김 상태면 403으로 분기한다.
        * - DisabledException: 계정 비활성(사용 불가)
        * - LockedException  : 계정 잠김
        * */
		
		if (exception instanceof DisabledException || exception instanceof LockedException) {
			status = HttpServletResponse.SC_FORBIDDEN;
			code = "ACCOUNT_DISABLED";
			message = "사용할 수 없는 계정입니다.";
		}
		
		//response.setStatus(404)
		//400대에러 프론트에서 볼수있음
		
		// ✅ JSON 응답 내려주기
        response.setStatus(status);//상태코드
        response.setContentType("application/json; charset=UTF-8"); // json타입
        response.getWriter().write("{\"message\":\"login failed\"}"); //에러 실패내용
	}

}
