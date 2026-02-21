package bugsandwich.ornably.connectLog;

import java.time.LocalDate;

import org.apache.ibatis.type.Alias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 				// getter/setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드를 받는 생성자
//@Alias("connectLogDTO")
public class ConnectLogDTO {
	
	// [ 테이블 컬럼 ]
	private Integer connectLogPk;	// 로그 고유 번호 (Pk)
	private Integer accountPk;		// 회원 고유 번호 (Fk) : 접속한 사용자 ID
	private String connectIp;		// 접속한 IP 주소
	private String connectDevice;	// 접속 환경 : 기기/브라우저
	private LocalDate connectDate;	// 접속 날짜·시간
	
	// [ 추가 멤버 변수 ]
	private String condition;		// 분기점
}