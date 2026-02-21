package bugsandwich.ornably.connectLog.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bugsandwich.ornably.connectLog.ConnectLogDTO;
import bugsandwich.ornably.connectLog.ConnectLogRepository;
import jakarta.servlet.http.HttpServletRequest;


@Service
public class ConnectLogServiceImpl implements ConnectLogService {
	
	@Autowired
	private ConnectLogRepository connectLogRepository;
	
	//접속기록 생성
	@Override
	public Boolean insertConnectLog(Integer accountPk, HttpServletRequest request) {
		//소셜 신규유저면 온보딩완료후 롤 변경 되기때문에 회원pk가 0일수 있음
		if(accountPk <=0) {
			return false; // 접속기록 저장안함
		}
		//조작방지를 위해 서버에서 직접 수집
		String ip = getclientIp(request); // 접속한 IP
		//로그인요청을 보낸 클라이언트 접속환경 (브라우저/os 대략적 기기정보)를 알려준다
		String device = request.getHeader("User-Agent");
		
		//빈값 방어
		if(ip == null || ip.isBlank())ip="unknown"; // ip가 널일때
		if(device == null || ip.isBlank())device="unknown"; // 클라이언트 접속환경이 널일때
		
		ConnectLogDTO connectLogDTO = new ConnectLogDTO();
		connectLogDTO.setAccountPk(accountPk);
		connectLogDTO.setConnectIp(ip);
		connectLogDTO.setConnectDevice(device);
		
		//DB저장
		return connectLogRepository.insert(connectLogDTO);
	}
	//접속기록 전체조회
	@Override
	public List<ConnectLogDTO> getAllMyLogs(Integer accountPk) {
		ConnectLogDTO connectLogDTO = new ConnectLogDTO();
		connectLogDTO.setAccountPk(accountPk);
		return connectLogRepository.selectAll(connectLogDTO);
	}
	//최근 접속기록 1개 조회
	@Override
	public ConnectLogDTO getMyLog(Integer accountPk) {
		ConnectLogDTO connectLogDTO = new ConnectLogDTO();
		connectLogDTO.setAccountPk(accountPk);
		return connectLogRepository.selectOne(connectLogDTO);
	}
	//접속기록 삭제
	@Override
	public Boolean deleteMyLogs(Integer accountPk) {
		ConnectLogDTO connectLogDTO = new ConnectLogDTO();
		connectLogDTO.setAccountPk(accountPk);
		return connectLogRepository.delete(connectLogDTO);
	}

	//공통 유틸 : 클라이언트 ip추출 메서드
	private String getclientIp(HttpServletRequest request) {
		/*프록시/로드밸런서 뒤에 있을때
		 * 실제 사용자 IP는 request.getRemoteAddr에 있는게 아니라
		 * X-Forwarded-For 헤더에 담겨오는 경우가 많음
		 * 예) "203.0.113.10, 10.0.0.1, 10.0.0.2"
		 * (맨 앞이 보통 실제 사용자 IP, 뒤는 중간 프록시 IP들)
		 */
		String xff = request.getHeader("X-Forwarded-For");
		//X-Forwarded-For 값이 존재하고 공백이 아니라면
		if(xff !=null && !xff.isBlank()) {
			/*X-Forwarded-For가 "IP","IP","IP"처럼 여러개가 올수있으니
			 * 쉼표로 자른뒤 첫번째값을 사용한다
			 * split(",") -> 맨앞 IP(대부분 실제 클라이언트 ip)
			 * trim() -> 앞뒤 공백제거
			 * */
			return xff.split(",")[0].trim();
		}
		//프록시를 거치지 않았거나 X-Forwarded-For가 없으면 
		//request.getRemoteAddr()로 접속한 IP를 가져온다
		//(이경우 보통 직접 접속한 클라이언트 IP가 된다)
		return request.getRemoteAddr();
		
	}
	
}
