package bugsandwich.ornably.cart.api;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.cart.CartDTO;
import bugsandwich.ornably.cart.service.CartService;
import bugsandwich.ornably.security.OrnablyUser;

@RestController
@RequestMapping("/api")
public class CartController {
	
	
	@Autowired	
	private CartService cartService;
	
//  ===================== 장바구니 목록 보기  =====================
	@PreAuthorize("hasRole('USER')")
	@GetMapping("/user/cart/payment")
	public ResponseEntity<Map<String, Object>> getCartList (CartDTO cartDTO, @AuthenticationPrincipal OrnablyUser ornablyUser){
		
		cartDTO.setAccountPk(ornablyUser.getAccountPk());
		cartDTO.setCondition("SELECT_ALL_CART"); // 원래 SELECT_ALL_ORDERS 였는데 일단 바꿈
		
		List<CartDTO> list = cartService.getCartList(cartDTO);
		
		return ResponseEntity.ok(Map.of("cartDatas", list));
	}
	
	
	
//  ===================== 장바구니 아이템 추가  =====================
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/user/cart")
	public ResponseEntity<Map<String, Object>> insertCartItem (
			@RequestBody CartDTO cartDTO,  
			@AuthenticationPrincipal OrnablyUser ornablyUser
		){
		
		// 정보가 전달되지 않았을 경우
		if(cartDTO.getItemPk() == null || cartDTO.getCartCount() <= 0 || cartDTO.getCartCount() == null ) {
			return ResponseEntity.status(400).body(Map.of(
					"code", "INVALID_COUNT",
					"message", "요청 값이 올바르지 않습니다."
					));
		}
		
		
		cartDTO.setAccountPk(ornablyUser.getAccountPk());
		cartDTO.setCondition("INSERT_CART_OR_UPDATE");
		
		if(!cartService.insertCart(cartDTO)) {
			return ResponseEntity.status(404).body(Map.of(
					"code", "ITEM_NOT_FOUND",
					"message", "해당 상품 정보를 찾을 수 없습니다."
					));
		}	
		
		return ResponseEntity.ok().body(Map.of(
				"code", "sucess",
				"message", "장바구니 추가 성공"
				));
	}
	
	
//  ===================== 장바구니 담긴 수량 변경 =====================
	@PreAuthorize("hasRole('USER')")
	@PatchMapping("/user/cart/{cartPk}")
	public ResponseEntity<Map<String, Object>> updateCartItemCount (
			@PathVariable Integer cartPk,
			@RequestBody CartDTO cartDTO, 
			@AuthenticationPrincipal OrnablyUser ornablyUser
			) {
		
		cartDTO.setAccountPk(ornablyUser.getAccountPk());
		cartDTO.setCartPk(cartPk);
		cartDTO.setCondition("UPDATE_CART_ITEM_COUNT");
		if(!cartService.updateCart(cartDTO)) {
    		return ResponseEntity.status(404).body(Map.of(
        			"code", "ITEM_NOT_FOUND",
        			"message", "해당 상품 정보를 찾을 수 없습니다."
    				));
		}
		
		return ResponseEntity.ok().body(Map.of(
				"code", "sucess",
				"message", "장바구니 상품 수량 변경 성공"
				));
	}
	
	
	
//  ===================== 장바구니 담긴 단일 상품 삭제 =====================
	@PreAuthorize("hasRole('USER')")
	@DeleteMapping("/user/cart/{cartPk}")
	public ResponseEntity<Map<String, Object>> updateDeleteOneCartItem (
			@PathVariable Integer cartPk,
			CartDTO cartDTO, 
			@AuthenticationPrincipal OrnablyUser ornablyUser) {
		
		cartDTO.setAccountPk(ornablyUser.getAccountPk());
		cartDTO.setCartPk(cartPk);
		cartDTO.setCondition("DELETE_CART_ITEM");
		
		if(!cartService.deleteCart(cartDTO)) {
    		return ResponseEntity.status(404).body(Map.of(
        			"code", "ITEM_NOT_FOUND",
        			"message", "해당 상품 정보를 찾을 수 없습니다."
    				));
		}
		
		return ResponseEntity.ok().body(Map.of(
				"code", "sucess",
				"message", "장바구니 담긴 단일 상품 삭제 성공"
				));
	}
}
