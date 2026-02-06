package bugsandwich.ornably.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class HttpUtil {
	
	private static HashMap<String, ResponseEntity<Map<String, Object>>> factory;
	
	public HttpUtil() {
		this.factory = new HashMap<>();
		
		this.factory.put("INTERNAL_SERVER_ERROR", ResponseEntity.status(500).body(Map.of("message", "서버 측 문제가 발생했습니다.", "code", "INTERNAL_SERVER_ERROR")));
		
	}
	
	public static ResponseEntity<?> buildByCode(String code){
		
		ResponseEntity res = factory.get(code);
		if(res != null) {
			return res;
		}
		else {
			return ResponseEntity.status(500).body(Map.of("message", "등록되어있지 않은 오류가 발생했습니다.", "code", "UNKNOWN_ERROR"));
		}
	}
	
	public static ResponseEntity<?> httpResponseBuilder(
	        int status,
	        String code,
	        String message,
	        Map<String, Object> body
	) {
	    Map<String, Object> response = new HashMap<>();
	    response.put("code", code);
	    response.put("message", message);

	    if (body != null && !body.isEmpty()) {
	        response.putAll(body);
	    }

	    return ResponseEntity.status(status).body(response);
	}
}
