package bugsandwich.ornably.wishlist.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.security.OrnablyUser;
import bugsandwich.ornably.wishlist.WishlistDTO;
import bugsandwich.ornably.wishlist.service.WishlistService;

@RestController
@RequestMapping("api/user/wishlist/")// 찜관련 요청 처리 
public class WishlistController {
	
	@Autowired
	private WishlistService wishlistService;
	
	//=======찜 전체목록========
	@GetMapping//api/user/wishlist/
	public ResponseEntity<List<WishlistDTO.ItemResponse>> getWishlist(
			// 현재 로그인 한 사용자 꺼내오기
			@AuthenticationPrincipal OrnablyUser ornablyUser
			){
		//로그인 여부 체크(예외상황 방지)
		if(ornablyUser==null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		//현재 로그인 한 사용자pK 가져오기
		Integer accountPk = ornablyUser.getAccountPk();
		
		//내 위시리스트 목록 가져오기
		List<WishlistDTO> list = wishlistService.getWishlistList(accountPk);
		
		return ResponseEntity.ok(list);
	}
	
	//========찜목록 삭제=========
	@DeleteMapping("/{itemPk}")
	public ResponseEntity<void> deleteWishlist(
			@PathVariable Integer itemPk,
			AuthenticationPrincipal OrnablyUser ornablyUser
			){
		// 로그인 체크
		if(ornablyUser==null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		//찜삭제하기
		wishlistSevice.delete(ornablyUser.getAccountPk(),itemPk);
		return ResponseEntity.noContent().build();
		
	}
	
	//=========찜 생성 ==========
	@PostMapping("/{itemPk}")
	
	public ResponseEntity<void> insertWishlist(
			@PathVariable Integer itemPk,
			AuthenticationPrincipal OrnablyUser ornablyUser
			){
		// 로그인 체크
		if(ornablyUser==null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		//찜 목록 생성
		wishlistSevice.insert(ornablyUser.getAccountPk(),itemPk);
		return ResponseEntity.status(HttpStatus.CREATED).bild();
		
	}
	
}
	
	
	

