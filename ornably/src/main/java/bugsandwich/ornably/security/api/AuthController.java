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
//	# 회원 권한 정보 가져오기
//	GET /api/all/auth/info
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





