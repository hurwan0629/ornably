package bugsandwich.ornably.account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data 				// getter/setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드를 받는 생성자
public class AccountDTO {
	
	// [ 테이블 컬럼 ]
	private Integer accountPk;			// 회원 고유 번호 (Pk)
	private String accountId;			// 회원 아이디
	private String accountPasswordHash;	// 비밀번호 해시 값 (DB 저장용)
	private String accountName;			// 회원 이름
	private String accountEmail;		// 회원 이메일
	private String accountPhone;		// 회원 전화번호
	private LocalDate accountDate;		// 회원 가입일
	private String accountRole;			// 회원 권한 : "ADMIN"/"USER" 같은 값
	private Boolean accountEventOptIn;	// 회원 이벤트수신 동의 여부
	
	// [ 추가 멤버 변수 ]
	private String condition; 				// 분기점
	private String accountProvider; 		// "LOCAL"/"GOOGLE"...
	private String accountPassword;			// 회원 비밀번호
	private String accountKey;				// 전화번호 인증시 임시로 내려주는 키
	private String accountVerificationCode;	// 전화번호 인증 키(요청 받을때 사용)
	
	// [ 관리자용 멤버 변수 ]
	private Integer accountTotalAmountMin;
	private Integer accountTotalAmountMax;
	private Integer accountTotalAmount;
	private LocalDate accountJoinEndDate;
	private LocalDate accountJoinStartDate;
}