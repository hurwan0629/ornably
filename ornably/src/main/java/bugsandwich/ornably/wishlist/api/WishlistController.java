package bugsandwich.ornably.wishlist.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.security.OrnablyUser;
import bugsandwich.ornably.wishlist.WishlistDTO;
import bugsandwich.ornably.wishlist.service.WishlistService;

@RestController
@RequestMapping("api/user/wishlist") // 찜관련 요청 처리
public class WishlistController {

	@Autowired
	private WishlistService wishlistService;

	// =======찜 전체목록========
	@GetMapping // api/user/wishlist/
	public ResponseEntity<?> getWishlist(
			// 현재 로그인 한 사용자 꺼내오기
			@AuthenticationPrincipal OrnablyUser ornablyUser) {
		// 로그인 여부 체크(예외상황 방지)
		if (ornablyUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		// 현재 로그인 한 사용자pK 가져오기
		WishlistDTO wishlistDTO = new WishlistDTO();
		wishlistDTO.setAccountPk(ornablyUser.getAccountPk());
		wishlistDTO.setCondition("SELECT_ALL_WISHLIST_BY_ACCOUNT_PK");

		// 내 위시리스트 목록 가져오기
		List<WishlistDTO> list = wishlistService.getWishlistList(wishlistDTO);

		return ResponseEntity.ok(Map.of("wishlistDatas", list));
	}

	// ========찜목록 삭제=========
	@DeleteMapping("/{itemPk}")
	public ResponseEntity<?> deleteWishlist(
			@PathVariable Integer itemPk,
			@AuthenticationPrincipal OrnablyUser ornablyUser
			){
		// 로그인 체크
		if(ornablyUser==null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		WishlistDTO wishlistDTO = new WishlistDTO();
		wishlistDTO.setItemPk(itemPk);
		wishlistDTO.setAccountPk(ornablyUser.getAccountPk());
		wishlistDTO.setCondition("DELETE_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK");
		//찜삭제하기
		if(wishlistService.deleteWishlist(wishlistDTO)) {
			return ResponseEntity.noContent().build();
		}
		else {
			return ResponseEntity.internalServerError().build();
		}
		
	}
	
	//=========찜 생성 ==========
	@PostMapping("/{itemPk}") 
	public ResponseEntity<?> insertWishlist(
			@PathVariable Integer itemPk,
			@AuthenticationPrincipal OrnablyUser ornablyUser
			){
		// 로그인 체크
		if(ornablyUser==null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		WishlistDTO wishlistDTO = new WishlistDTO();
		wishlistDTO.setAccountPk(ornablyUser.getAccountPk());
		wishlistDTO.setItemPk(itemPk);
		wishlistDTO.setCondition("SELECT_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK");
		
		if(wishlistService.getWishlist(wishlistDTO) != null) {
			return ResponseEntity.status(HttpStatus.OK).build();
		}
		
		wishlistDTO.setAccountPk(ornablyUser.getAccountPk());
		wishlistDTO.setItemPk(itemPk);
		wishlistDTO.setCondition("INSERT_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK");
		
		//찜 목록 생성
		if(wishlistService.insertWishlist(wishlistDTO)) {
			return ResponseEntity.status(HttpStatus.CREATED).build();			
		}
		else {
			return ResponseEntity.internalServerError().build();
		}
	}
}
