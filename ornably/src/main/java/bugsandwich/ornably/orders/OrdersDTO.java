package bugsandwich.ornably.orders;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 				// getter/setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드를 받는 생성자
public class OrdersDTO {
	
	// [ 테이블 컬럼 ]
	private Integer ordersPk;				// 주문 고유 번호 (Pk)
	private Integer accountPk;				// 회원 고유 번호 (Fk)
	private LocalDate ordersDate;			// 주문 날짜
	private String ordersStatus;			// 주문 상태
	private String addressName;				// 배송지 명
	private String ordersPaymentType;		// 결제 방식
	private String ordersImportUid;			// 결제 고유 ID : 외부 연동용
	private String ordersMessage;			// 주문 요청 사항
	
	// [ 추가 멤버 변수 ]
	private String condition; 				// 분기점	
	private Integer addressPk; 				// 배송지 고유 번호 (FK)
	private Integer ordersTotalAmount; 		// 주문 총 가격
	private String ordersSignatureItemName; // 주문내역 대표 아이템 이름
	private String itemImageUrl;			// 대표 상품 이미지 경로
	private Integer ordersItemCount;		// 총 상품 개수
	private Integer itemCount;
	private Integer itemPrice;
	private Integer itemPk;
}