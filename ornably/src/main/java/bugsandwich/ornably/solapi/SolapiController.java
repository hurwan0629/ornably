package bugsandwich.ornably.solapi;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.account.AccountDTO;

@RestController
@RequestMapping("/api")
public class SolapiController {

	@Autowired
	private SolapiService solapiService;

	// 전화번호 인증 코드 발송 코드
	@PreAuthorize("isAnonymous() or hasRole('ONBOARD')")
	@PostMapping({"/guest/phone-verifications/send-code",
		"/onboard/phone-verifications/send-code"})
	public ResponseEntity<?> sendSolapiCodeLocal(@RequestBody AccountDTO accountDTO) {
		
		
		
		String accountKey = java.util.UUID.randomUUID().toString();
		
		// 솔라피 토큰 안쓰고 무조건 허용 코드
//		if (true) {
//			return ResponseEntity.ok(
//					Map.of( "success", true,
//							"accountKey", accountKey));
//		}
		
		// 사용자의 인증번호 확인 요청에 대한 본인 확인을 위한 accountKey와 함께 생성하여 응답해주기.
		if (this.solapiService.sendCodeMessage(accountDTO.getAccountPhone(), accountKey)) {
			return ResponseEntity.ok(
					Map.of( "success", true,
							"accountKey", accountKey));
		}
		else {
			return ResponseEntity.internalServerError().body(Map.of( "success", false));
		}
	}
	
	// 로컬 회원 인증번호 확인 요청
	@PreAuthorize("isAnonymous() or hasRole('ONBOARD')")
	@PostMapping({"/guest/phone-verifications/check-code",
				"/onboard/phone-verifications/check-code"})
	public ResponseEntity<?> checkSolapiCodeLocal(
			@RequestBody AccountDTO accountDTO
			) {
		
		// 솔라피 토큰 안쓰고 무조건 허용 코드
//		if(true) {
//			return ResponseEntity.ok(
//					Map.of("success", true));
//		}
//		
		if(this.solapiService.validateCode(accountDTO.getAccountVerificationCode(), accountDTO.getAccountKey())) {
			return ResponseEntity.ok(
					Map.of("success", true));
		}
		else {
			return ResponseEntity.ok(
					Map.of("success", false));
		}
	}
}
