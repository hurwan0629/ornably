package bugsandwich.ornably.connectLog.service;

import java.util.List;

import bugsandwich.ornably.connectLog.ConnectLogDTO;
import jakarta.servlet.http.HttpServletRequest;


public interface ConnectLogService {
	
	//접속기록 생성
	Boolean insertConnectLog(Integer accountPk, HttpServletRequest request);
	
	//특정 사용자 로그 전체 조회
	List<ConnectLogDTO> getAllMyLogs(Integer accountPk);
	
	//특정사용자 최신 로그 1건
	ConnectLogDTO getMyLog(Integer accountPk);
	
	//사용자 로그 전체 삭제
	Boolean deleteMyLogs(Integer accountPk);

}
