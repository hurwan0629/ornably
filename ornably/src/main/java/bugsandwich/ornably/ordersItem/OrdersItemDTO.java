package bugsandwich.ornably.ordersItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 				// getter/setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드를 받는 생성자
public class OrdersItemDTO {
	// 멤버 변수
	private int ordersItemPk;		// 주문 상세 Pk
	private int ordersPk;			// 주문 Fk
	private int itemPk;				// 상품 Fk
	private int ordersItemCount;	// 주문 수량
	private int ordersItemPrice;	// 주문 시 가격
	private String condition; 		// 분기점
}