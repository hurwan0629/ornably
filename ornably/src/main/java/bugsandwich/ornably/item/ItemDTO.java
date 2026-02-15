package bugsandwich.ornably.item;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 				// getter/setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드를 받는 생성자
public class ItemDTO {
	
	// [ 테이블 컬럼 ] 
	private Integer itemPk;				// 상품 고유 번호 (Pk)
	private String itemName;			// 상품명
	private Integer itemPrice;			// 상품 가격
	private Integer itemStock;			// 상품 재고
	private String itemDescription;		// 상품 설명
	private String itemImageUrl;		// 상품 이미지 경로
	private String itemCategory;		// 상품 카테고리
	private LocalDate itemRegistDate;	// 상품 등록일
	
	// [ 추가 멤버 변수 ]
	private String condition; 				// 분기점
	private Integer accountPk;				// 회원 PK
	private Boolean itemWishlistToggle; 	// 찜 여부
	private Integer cartCount;				// 장바구니에 담긴 상품 총 수량
	
	// [ 관리자용 멤버 변수 ]
	private Integer itemPriceMin;           // 최소 가격
	private Integer itemPriceMax;           // 최대 가격
	private LocalDate itemRegistDateStart;  // 등록일 시작
	private LocalDate itemRegistDateEnd;    // 등록일 종료
	private Integer itemSoldCount;      	// 판매량
	private Integer itemReviewCount;    	// 리뷰 수
	private Integer itemWishlistCount;  	// 찜 수
	private Integer salesAmount;			// 총 판매 금액
	private Integer salesCount;				// 총 판매 개수
	private Integer days;					// n일간 판매 수량
	private LocalDate date;					// 특정 날짜의 판매금액 총합
	
	// [ 요청용 - API 엔트포인트 ]
	private String search;				// 검색어
	private String category;			// all·tree·light
	private String sort;				// default·popular·new·new-reverse·discount
	private Integer page;				// 1부터
	private Integer dataCount;			// 페이지당 개수
	
	
	// [ 응답용 - join ]
	private Integer itemDiscountRate;   // 이벤트 할인율
    private Double itemDiscountPrice; // 할인 적용된 가격
	private Double itemAvgStar; 		// 상품 평점
	
	
	// [ 페이지 네이션 ]
	private Integer itemOffset;   		// OFFSET (시작 위치)
	private Integer itemLimit;    		// LIMIT (가져올 개수)
	private Integer itemSizeCount;		// 한 페이지에 보여줄 개수
	private Integer itemTotalCount;		// 총 상품 개수
	
	
	// [ json 응답 ]
	private List<ItemDTO> itemDatas;
	private Integer maxPages;
}