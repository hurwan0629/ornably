package bugsandwich.ornably.orders.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.orders.OrdersDTO;
import bugsandwich.ornably.orders.service.OrdersService;
import bugsandwich.ornably.security.OrnablyUser;

@RestController
public class OrdersController {
	@Autowired
	private OrdersService ordersService;
	
//  ===================== 주문 내역 목록 보기  =====================
	@PreAuthorize("hasRole('USER')")
	@GetMapping("/api/user/orders/me")
	public ResponseEntity<Map<String, Object>> getOrdersList(
			@AuthenticationPrincipal OrnablyUser ornablyUser,
			OrdersDTO ordersDTO
			){

		ordersDTO.setAccountPk(ornablyUser.getAccountPk());
		ordersDTO.setCondition("SELECT_ALL_ORDERS");
		
		List<OrdersDTO> list = ordersService.getOrdersList(ordersDTO);
		
		return ResponseEntity.ok(Map.of("ordersDatas", list));
	}
	
//  (재고감소 -> 주무내역 생성 -> 주문내역 생성 -> 장바구니 삭제 )
//  ===================== 결제 시 트랜잭션  =====================
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/api/user/orders/cart-payment")
	public ResponseEntity<Map<String, Object>> paySuccess(
			@RequestBody OrdersDTO ordersDTO,
			@AuthenticationPrincipal OrnablyUser ornablyUser
			){
		ordersDTO.setAccountPk(ornablyUser.getAccountPk());
		
	    boolean ok = ordersService.paymentComplete(ordersDTO);

	    return ResponseEntity.ok(Map.of("result", ok));
	}
	
	
	
	
}
