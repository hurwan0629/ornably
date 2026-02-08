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
	private int wishlistPk;
	private int accountPk;
	private int itemPk;
	private String condition;
}
