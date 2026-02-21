package bugsandwich.ornably.wishlist;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import bugsandwich.ornably.review.ReviewDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 				// getter/setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드를 받는 생성자
public class WishlistDTO {
	
	// [ 테이블 컬럼 ]
	private Integer wishlistPk;	// 위시리스트 고유 번호 (Pk)
	private Integer accountPk;	// 회원 고유 번호 (Fk)
	private Integer itemPk;		// 상품 고유 번호 (Fk)
	
	// [ 추가 멤버 변수 ]
	private String condition;	// 분기점

	private String itemName;
	private String itemImageUrl;
	private String itemCategory;
	private Integer itemPrice;
	private Integer itemDiscountRate;
	private Integer itemDiscountPrice; 
	private Double itemAvgStar;
	private Boolean itemWishlistToggle;
}
