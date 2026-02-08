package bugsandwich.ornably.security.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.security.OrnablyUser;



@RestController
@RequestMapping("/api")
public class AuthController {
	
	/*
	# 회원 권한 정보 가져오기
	GET /api/all/auth/info
	
	auth: (없음)
	body: (없음)
	response:
	200 OK
	- authenticated: boolean             # securityContext의 Authentication.principal.authenticated
	- role: "LOCAL" | "ADMIN" | "GOOGLE" | "KAKAO" | ...
	- authorities: [] | ["ONBOARD"] | ["USER"] | ["ADMIN"]    # 원래 리스트이지만 1개만 사용할 예정
	errors:
	- 400 BAD_REQUEST
		- code: VALIDATION_ERROR
		- message: "요청 값이 올바르지 않습니다."
	- 401 UNAUTHORIZED
		- code: UNAUTHORIZED
		- message: "인증 정보를 확인할 수 없습니다."
	- 403 FORBIDDEN
		- code: ACCESS_DENIED
		- message: "해당 요청에 대한 접근 권한이 없습니다."
	- 500 INTERNAL_SERVER_ERROR
		- code: INTERNAL_SERVER_ERROR
		- message: "인증 정보 조회 중 오류가 발생했습니다."
	 */
	@GetMapping("all/auth/info")
	public ResponseEntity<Map<String, Object>> getUserAuthInfoData(@AuthenticationPrincipal OrnablyUser loginUser){
		Map<String, Object> data = new HashMap<>();
		
		boolean authenticated = loginUser != null;
		
		String role = 
				authenticated 
				? loginUser.getAccountRole()
				: null;
		
		List<String> authorities =
				authenticated
				? loginUser.getAuthorities().stream().map(GrantedAuthority::getAuthority)
						.map(auth -> auth.replace("ROLE_", "")).toList()
				: List.of();
		
		data.put("authenticated", authenticated);
		data.put("role", role);
		data.put("authorities", authorities);
		
		return ResponseEntity.ok(data);
	}
}
