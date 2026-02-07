package bugsandwich.ornably.cart.service;

import java.util.List;

import bugsandwich.ornably.cart.CartDTO;



public interface CartService {
	boolean insertCart(CartDTO cartDTO);
	boolean updateCart(CartDTO cartDTO);
	boolean deleteCart(CartDTO cartDTO);
	
	CartDTO getCartData(CartDTO boardDTO);
	List<CartDTO> getCartList(CartDTO boardDTO);
}
