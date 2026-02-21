package bugsandwich.ornably.orders.api;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import bugsandwich.ornably.security.api.AuthController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.cart.service.CartService;
import bugsandwich.ornably.orders.OrdersDTO;
import bugsandwich.ornably.orders.service.OrdersService;
import bugsandwich.ornably.portone.PortOneClient;
import bugsandwich.ornably.portone.PortOnePaymentDTO;
import bugsandwich.ornably.security.OrnablyUser;

@RestController
@RequestMapping("/api")
public class OrdersController {

	private final AuthController authController;
	@Autowired
	private OrdersService ordersService;

	@Autowired
	private CartService cartService;

	@Autowired
	private PortOneClient portOneClient;

	OrdersController(AuthController authController) {
		this.authController = authController;
	}

//  ===================== 주문 내역 목록 보기  =====================
	@PreAuthorize("hasRole('USER')")
	@GetMapping("/user/orders/me")
	public ResponseEntity<Map<String, Object>> getOrdersList(@AuthenticationPrincipal OrnablyUser ornablyUser,
			OrdersDTO ordersDTO) {

		ordersDTO.setAccountPk(ornablyUser.getAccountPk());
		ordersDTO.setCondition("SELECT_ALL_ORDERS_BY_ACCOUNT_PK");

		List<OrdersDTO> list = ordersService.getOrdersList(ordersDTO);

		return ResponseEntity.ok(Map.of("ordersDatas", list));
	}

//  (재고감소 -> 주무내역 생성 -> 주문내역 생성 -> 장바구니 삭제 )
//  ===================== 장바구니 결제 시 트랜잭션  =====================
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/user/orders/cart-payment")
	public ResponseEntity<Map<String, Object>> paySuccess(@RequestBody OrdersDTO ordersDTO,
			@AuthenticationPrincipal OrnablyUser ornablyUser) {
		return processPayment(ordersDTO, ornablyUser, dto -> ordersService.paymentComplete(dto));
	}

//  (재고감소 -> 주무내역 생성 -> 주문내역 생성)
//  ===================== 바로 결제시 트랜잭션  =====================
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/user/orders/instance-payment")
	public ResponseEntity<Map<String, Object>> buyNowSuccess(@RequestBody OrdersDTO ordersDTO,
			@AuthenticationPrincipal OrnablyUser ornablyUser) {
		return processPayment(ordersDTO, ornablyUser, dto -> ordersService.buyNowPaymentComplete(dto));
	}

	// 결제 검증 및 DB 저장 로직
	private ResponseEntity<Map<String, Object>> processPayment(OrdersDTO ordersDTO, OrnablyUser ornablyUser,
			Function<OrdersDTO, Boolean> transactional) {
		// 1) 필수값 검증
		if (ordersDTO.getAddressPk() == null || ordersDTO.getOrdersImportUid() == null) {
			return ResponseEntity.status(400).body(Map.of("code", "DATA_NULL", "message", "요청 값이 올바르지 않습니다."));
		}

		// 2) 요청사항 기본값
		if (ordersDTO.getOrdersMessage() == null || ordersDTO.getOrdersMessage().isBlank()) {
			ordersDTO.setOrdersMessage("요청사항 없음");
		}

		// 결제 고유번호 맞는지 조회 -> 주문내역 없어서 구현 x

		// 3) PortOne 결제 조회
		PortOnePaymentDTO payment = portOneClient.getPayment(ordersDTO.getOrdersImportUid());

		if (payment == null || payment.getStatus() == null) {
			return ResponseEntity.status(500)
					.body(Map.of("code", "PORTONE_INVALID_RESPONSE", "message", "결제 조회 응답이 올바르지 않습니다."));
		}

		// 4) 결제 상태 검증
		if (!"PAID".equalsIgnoreCase(payment.getStatus())) {
			return ResponseEntity.status(404).body(Map.of("code", "PAYMENT_FAILED", "message", "결제가 완료 상태가 아닙니다."));
		}

		// 5) 결제 수단 세팅 + accountPk 세팅
		ordersDTO.setOrdersPaymentType(payment.resolveOrdersPaymentType());
		ordersDTO.setAccountPk(ornablyUser.getAccountPk());

		// 총 결제 금액 검증 -> 주문내역 없어서 구현 x

		// 6) 트랜잭션 실행(여기만 달라짐)
		boolean ok = transactional.apply(ordersDTO);

		if (!ok) {
			return ResponseEntity.status(422).body(Map.of("code", "PAYMENT_FAILED", "message", "상품의 재고가 부족합니다."));
		}
		return ResponseEntity.ok(Map.of("message", ok));
	}
}
