package bugsandwich.ornably.cart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bugsandwich.ornably.cart.CartDTO;
import bugsandwich.ornably.cart.CartRepository;

@Service
public class CartServiceImpl implements CartService{
	
	@Autowired
	private CartRepository cartRepository;

	@Override
	public boolean insertCart(CartDTO cartDTO) {
		return cartRepository.insert(cartDTO);
	}

	@Override
	public boolean updateCart(CartDTO cartDTO) {
		return cartRepository.update(cartDTO);
	}

	@Override
	public boolean deleteCart(CartDTO cartDTO) {
		return cartRepository.delete(cartDTO);
	}

	@Override
	public CartDTO getCartData(CartDTO cartDTO) {
		return null;
	}

	@Override
	public List<CartDTO> getCartList(CartDTO cartDTO) {
		return cartRepository.selectAll(cartDTO);
	}


	

	
}
