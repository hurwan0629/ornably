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
	//회원 권한 정보 가져오기
	@GetMapping("all/auth/info") 
	public ResponseEntity<Map<String, Object>> getUserAuthInfoData(@AuthenticationPrincipal OrnablyUser loginUser){ //현재 로그인한 사용자 정보
		//응답 줄 데이터 담을 맵 생성
		Map<String, Object> data = new HashMap<>();
		
		//로그인 여부체크
		//로그인 안했으면 null
		boolean authenticated = loginUser != null;
		
		//로그인 상태라면 사용자 롤담기 로그인 안했으면 null
		String role = 
				authenticated 
				? loginUser.getAccountRole()
				: null;
		
		//로그인 상태라면 권한목록을 문자열 리스트로 담고 아니면 빈리스트
		 //GrantedAuthority 객체 목록 -> 문자열로 변환(getAuthority)
		 // - "ROLE_USER" 같은 형태면 "ROLE_" 접두사 제거 -> "USER"
		List<String> authorities = //권한,인가
				authenticated // 인증
				? loginUser.getAuthorities().stream().map(GrantedAuthority::getAuthority)
						.map(auth -> auth.replace("ROLE_", "")).toList()
				: List.of();
		//최종응답 json 형태
		data.put("authenticated", authenticated);//인증여부 true,false
		data.put("role", role); //로그인시 롤 아니면 null
		data.put("authorities", authorities); //로그인시 권한리스트 아니면 빈리스트
		
		return ResponseEntity.ok(data);//상태코드 200 반환
	}
}
