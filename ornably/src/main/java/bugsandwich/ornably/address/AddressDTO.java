package bugsandwich.ornably.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 				// getter/setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드를 받는 생성자
public class AddressDTO {
	
	// [ 테이블 컬럼 ]
	private Integer addressPk;			// 주소 고유 번호 (Pk)
	private Integer accountPk;			// 회원 고유 번호 (Fk)
	private String addressName;			// 배송지명
	private Boolean addressIsDefault;	// 기본 배송지 여부
	private String addressPostalCode;	// 우편번호
	private String addressRegion;		// 기본 주소
	private String addressDetail;		// 상세 주소
	
	// [ 추가 멤버 변수 ]
	private String condition; 			// 분기점
}