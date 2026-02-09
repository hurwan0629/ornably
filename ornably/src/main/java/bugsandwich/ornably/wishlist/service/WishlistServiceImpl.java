package bugsandwich.ornably.wishlist.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bugsandwich.ornably.wishlist.WishlistDTO;
import bugsandwich.ornably.wishlist.WishlistRepository;

@Service
public class WishlistServiceImpl implements WishlistService {
	
	@Autowired
	private WishlistRepository wishlistRepository;
	
	@Override
	public boolean insertWishlist(WishlistDTO wishlistDTO) {
		return wishlistRepository.insert(wishlistDTO);
	}

	@Override
	public boolean updateWishlist(WishlistDTO wishlistDTO) {
		return wishlistRepository.update(wishlistDTO);
	}

	@Override
	public boolean deleteWishlist(WishlistDTO wishlistDTO) {
		return wishlistRepository.delete(wishlistDTO);
	}

	@Override
	public WishlistDTO getWishlist(WishlistDTO wishlistDTO) {
		return wishlistRepository.selectOne(wishlistDTO);
	}

	@Override
	public List<WishlistDTO> getWishlistList(WishlistDTO wishlistDTO) {
		return wishlistRepository.selectAll(wishlistDTO);
	}

		
}
