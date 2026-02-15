package bugsandwich.ornably.ordersItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 				// getter/setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드를 받는 생성자
public class OrdersItemDTO {
	
	// [ 테이블 컬럼 ]
	private Integer ordersItemPk;		// 주문 상세 고유 번호 (Pk)
	private Integer ordersPk;			// 주문 고유 번호 (Fk)
	private Integer itemPk;				// 상품 고유 번호 (Fk)
	private Integer ordersItemCount;	// 주문 수량
	private Integer ordersItemPrice;	// 주문 시 가격
	
	// [ 추가 멤버 변수 ]
	private String condition; 			// 분기점
	private Integer accountPk; 			// 회원 고유 번호 (FK)
	private String itemName;			// 상품 이름
	private String itemImageUrl; 		// 상품 이미지 경로
	private Boolean isReviewed; 		// 사용자 리뷰 작성 유무
	private String ordersStatus;
}