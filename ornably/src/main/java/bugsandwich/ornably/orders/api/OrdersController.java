package bugsandwich.ornably.orders.api;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.cart.service.CartService;
import bugsandwich.ornably.orders.OrdersDTO;
import bugsandwich.ornably.orders.service.OrdersService;
import bugsandwich.ornably.portone.PortOneClient;
import bugsandwich.ornably.portone.PortOnePaymentDTO;
import bugsandwich.ornably.security.OrnablyUser;
import tools.jackson.databind.JsonNode;


@RestController
@RequestMapping("/api")
public class OrdersController {
	@Autowired
	private OrdersService ordersService;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private PortOneClient portOneClient;
	
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
		
		// 필요한 요청값이 안들어 왔을 떄
		if(ordersDTO.getAddressPk() == null || ordersDTO.getOrdersImportUid() == null) {
	       return ResponseEntity.status(400).body(Map.of(
	               "code", "DATA_NULL",
	              "message", "요청 값이 올바르지 않습니다."
	              ));
		}
		// 요청사항 비었을 시
		if(ordersDTO.getOrdersMessage() == null) {
			ordersDTO.setOrdersMessage("요청사항 없음");
		}
		
		// 결제 고유번호 맞는지 조회 -> 주문내역 없어서 구현 x
		
	    // 3) PortOne 결제 조회
		PortOnePaymentDTO payment = portOneClient.getPayment(ordersDTO.getOrdersImportUid());
		System.out.println("[PortOne 결제 검증 결과] : " + payment); //  toString 

		// 결제 검증 결과가 없을 시에 에러
		if (payment == null || payment.getStatus() == null) {
		    return ResponseEntity.status(500).body(Map.of(
		        "code", "PORTONE_INVALID_RESPONSE",
		        "message", "결제 조회 응답이 올바르지 않습니다."
		    ));
		}
		
		// 결제 완료 상태가 아니면 !"PAID"
		String status = payment.getStatus();
		if (!"PAID".equalsIgnoreCase(status)) {
		    return ResponseEntity.status(404)
		            .body(Map.of("code","PAYMENT_FAILED","message","결제가 완료 상태가 아닙니다."));
		}
		

		
		// 총 결제 금액 검증 -> 주문내역 없어서 구현 x
				
		// 결제 수단 조회
		String easyProvider = (payment.getEasyPay() != null) ? payment.getEasyPay().getProvider() : null;
		ordersDTO.setOrdersPaymentType(easyProvider); // 예: KAKAOPAY / NAVERPAY
		
		ordersDTO.setAccountPk(ornablyUser.getAccountPk());
		
		// 트랜잭션 실행
		boolean ok = ordersService.paymentComplete(ordersDTO);
		
	    if(!ok) {
	    	return ResponseEntity.status(404)
	    			.body(Map.of("code", "PAYMENT_FAILED", "message", "결제가 실패되었습니다."));
	    }
	    return ResponseEntity.ok(Map.of("message", ok));
	}
}
