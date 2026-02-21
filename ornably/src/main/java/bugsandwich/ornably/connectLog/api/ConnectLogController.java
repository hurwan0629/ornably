package bugsandwich.ornably.connectLog.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.connectLog.service.ConnectLogService;
import bugsandwich.ornably.security.OrnablyUser;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class ConnectLogController {
	/*
	@Autowired
	private ConnectLogService connectLogService;
	
	//프론트가 로그 남겨줘 라고 호출 하는 API
	//접속기록 생성
	@PostMapping
	public ResponseEntity<Void> insertConnectLog(
			@AuthenticationPrincipal OrnablyUser ornablyUser, //현재 로그인한 사용자 정보 가져오기
			HttpServletRequest request
			){
		//
		//로그인 사용자 pk 가져오기
		Integer accountPk = ornablyUser.getAccountPk();
		
		//IP와 User-Agent는 서버가 직접 수집하는게 안전하다(조작방지)
		connectLogService.insertConnectLog(accountPk,request);
		
		//응답 반환 상태코드 200
		return ResponseEntity.ok().build();
	}
	
	//내 접속기록 전체조회
	@GetMapping
	public ResponseEntity<?> selectMyLogs(
			@AuthenticationPrincipal OrnablyUser ornablyUser) { //현재 로그인한 사용자 정보 가져오기
		//로그인 사용자 pk가져오기
		Integer accountPk = ornablyUser.getAccountPk();
		//응답반환
		return ResponseEntity.ok(connectLogService.getAllMyLogs(accountPk));
	}
	
	//내 최신 접속 1건 조회
	@GetMapping//("/latest")
	public ResponseEntity<?> selectMyLastLogs(
			@AuthenticationPrincipal OrnablyUser ornablyUser) {//현재 로그인한 사용자 정보 가져오기
		//로그인 사용자 pk가져오기
		Integer accountPk = ornablyUser.getAccountPk();
		//응답반환
		return ResponseEntity.ok(connectLogService.getMyLog(accountPk));
	}
	@DeleteMapping
	public ResponseEntity<?> deleteMyLogs(
			@AuthenticationPrincipal OrnablyUser ornablyUser) {//현재 로그인한 사용자 정보 가져오기
		//로그인 사용자 pk가져오기
		Integer accountPk = ornablyUser.getAccountPk();
		//접속기록 삭제
		if(connectLogService.deleteMyLogs(accountPk)) {			
			//응답반환
			return ResponseEntity.ok().build();
		}
		else {
			return ResponseEntity.internalServerError().build();
		}
	}*/
}

