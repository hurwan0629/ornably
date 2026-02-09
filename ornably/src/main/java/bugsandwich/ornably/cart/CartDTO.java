package bugsandwich.ornably.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 				// getter/setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드를 받는 생성자
public class CartDTO {
	
	// [ 테이블 컬럼 ]
	private Integer cartPk;			// 정바구니 고유 번호 (Pk)
	private Integer accountPk;		// 회원 고유 번호 (Fk)
	private Integer itemPk;			// 상품 고유 번호 (Fk)
	private Integer cartCount;		// 장바구니에 담긴 상품 수량
	
	// [ 추가 멤버 변수 ]
	private String condition;			// 분기점
	private Integer cartNewCount;		// 새로 담은 상품 개수
	private Integer cartTotalCount; 	// 사용자 장바구니 총 상품수량
	private Integer cartTotalPrice; 	// 장바구니에 담긴 상품 별 총 가격
	private Integer cartTotalAmount;	// 사용자 장바구니 총 금액
	private Integer itemDiscountRate;   // 할인율
	private Integer itemDiscountPrice;  // 할인 금액
	
	// [ 상품 정보 : JOIN 결과용 ]
	private String itemName;        // 상품명
	private Integer itemPrice;      // 상품 가격
	private String itemImageUrl;    // 상품 이미지
}