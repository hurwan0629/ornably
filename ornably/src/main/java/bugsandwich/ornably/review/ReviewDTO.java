package bugsandwich.ornably.review;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 				// getter/setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드를 받는 생성자
public class ReviewDTO {
	
	// [ 테이블 컬럼 ]
	private Integer reviewPk;		// 리뷰 고유 번호 (Pk)
	private Integer accountPk;		// 회원 고유 번호 (Fk)
	private Integer itemPk;			// 상품 고유 번호 (Fk)
	private String reviewTitle;		// 리뷰 제목
	private String reviewContent;	// 리뷰 내용
	private String reviewDate;	// 리뷰 작성일
	private Integer reviewStar;		// 리뷰 평점
	private String reviewImageUrl;	// 리뷰 이미지 경로
	
	// [ 추가 멤버 변수 ]
	private String condition;			// 분기점
	private Boolean reviewExists;		// 리뷰 존재 여부 확인
	private Integer itemPrice;			// 리뷰 할 상품 가격
	private Integer totalCount; 		// 리뷰 할 상품의 전체 가격
	private String itemName; 			// 리뷰 할 상품 이름
	private String itemImageUrl;		// 리뷰 할 상품 이미지
	private Integer reviewTotalCount;	// 리뷰 개수
	private MultipartFile reviewImage;
	
	// [ 페이지네이션 ]
	private Integer startReviewNum;		//리뷰 페이지네이션 시작 
	private Integer endReviewNum;		//리뷰 페이지네이션 끝
	private String reviewAccountName;	//리뷰 페이지네이션시 사용할 사용자이름
	private Integer page;
	private Integer dataCount;
	private Integer maxPages;
}