package bugsandwich.ornably.wishlist.service;

import java.util.List;
import bugsandwich.ornably.wishlist.WishlistDTO;

public interface WishlistService {

	boolean insertWishlist(WishlistDTO wishlistDTO);
	boolean updateWishlist(WishlistDTO wishlistDTO);
	boolean deleteWishlist(WishlistDTO wishlistDTO);
	
	WishlistDTO getWishlist(WishlistDTO wishlistDTO);
	List<WishlistDTO> getWishlistList(WishlistDTO wishlistDTO);
}


