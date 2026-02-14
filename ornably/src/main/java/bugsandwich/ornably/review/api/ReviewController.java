package bugsandwich.ornably.review.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.review.ReviewDTO;
import bugsandwich.ornably.review.service.ReviewService;
import bugsandwich.ornably.security.OrnablyUser;

@RestController
@RequestMapping("/api")
public class ReviewController {

	@Autowired
	private ReviewService reviewService;

	//내 리뷰 전체 보기
	@PreAuthorize("hasRole('USER')") // 롤이 유저인 사용자만 접근 가능
	@GetMapping("/user/review/me") //요청 Url
	public ResponseEntity<?> getReviewDatasByAccountPk(
			//ResponseEntity<?>
			//컨트롤러가 HTTP 응답 전체(상태코드/헤더/바디) 를 직접 만들어서 반환하는데, 
			//바디 타입은 아직 정하지 않겠다(아무거나 올 수 있다)”
			//<?> = 제네릭타입에서 와일드 카드라고 부름 = 바디타입을 정하지 않았다
			@AuthenticationPrincipal OrnablyUser ornablyUser // 스프링시큐리티에서가 주는 현재로그인한 사용자
			) {
		List<ReviewDTO> reviewDatas = this.reviewService.getReviewByAccountPk(ornablyUser.getAccountPk());// 회원pk에 해당하는 리뷰데이터 가져오기 

		//리뷰 데이터 반환
		return ResponseEntity.ok(
				Map.of("reviewDatas", reviewDatas));
	}


	// 상품 상세페이지 리뷰 보기
	@GetMapping("/all/review/item-detail-page")
	public ResponseEntity<?> getItemDetailPageReview(@ModelAttribute ReviewDTO reviewDTO) { 
		//@ModelAttribute
		// - GET 쿼리 파라미터(예: ?page=1&size=10)가 있으면 AddressDTO 필드에 자동 바인딩
        // - 파라미터가 없어도 스프링이 AddressDTO 객체를 자동 생성해 줌(new 없이 사용 가능)
		if (reviewDTO.getItemPk() == null || reviewDTO.getItemPk() == 0) { //리뷰의 상품pk가 널이거나 0라면
			return ResponseEntity.status(400).body(Map.of("code", "INVALID_ITEM_PK", "message", "요청 값이 올바르지 않습니다."));//에러상태 반환
		}
		//리뷰데이터 가져오기
		reviewDTO.setCondition("SELECT_ITEM_REVIEW_COUNT");
		List<ReviewDTO> reviewDatas = this.reviewService.getReviewByItemPk(reviewDTO); // itemPk, dataCount, page
		Integer itemPk = reviewDTO.getItemPk();
		
		reviewDTO = this.reviewService.getReviewMaxPageByItemPkAndDataCount(reviewDTO);
		return ResponseEntity.status(200).body(
				Map.of("reviewDatas", reviewDatas, 
					 "itemPk", itemPk,
					 "maxPages", reviewDTO.getMaxPages()));
	}

	// 리뷰 수정 시 데이터 조회
	@PreAuthorize("hasRole('USER')")
	@GetMapping(value = "/user/review/{reviewPk}")
	public ResponseEntity<?> getUserReviewByReviewPk(@PathVariable("reviewPk") Integer reviewPk) {
		//@PathVariable url 경로에 있는 값을 꺼내서 컨트롤러 메서드 파라이터에 넣어 주는 어노테이션
		// reviewPk - Integer
		// reviewTitle - String
		// reviewContent - String
		// reviewImage - MultipartFile
		// reviewStar - Integer
		ReviewDTO reviewDTO = reviewService.getReviewDataByReviewPk(reviewPk); // 리뷰pk가져와서 해당리뷰 수정 실행
		
		//리뷰pk,리뷰 제목,리뷰내용,리뷰 이미지,별점 반환하기
		return ResponseEntity.ok(Map.of("reviewPk", reviewDTO.getReviewPk(), "reviewTitle", reviewDTO.getReviewTitle(),
				"reviewContent", reviewDTO.getReviewContent(), "reviewImageUrl", reviewDTO.getReviewImageUrl(),
				"reviewStar", reviewDTO.getReviewStar()));
	}
	// 관리자 상품 관리시 상품 리뷰 조회
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping(value = "/admin/item/{itemPk}/review")
	public ResponseEntity<?> getUserReviewDatasByItemPkAdmin(@PathVariable("itemPk") Integer itemPk){
		List<ReviewDTO> reviewDatas = reviewService.getReviewDatasByReviewPkAdmin(itemPk);
		//조회한 리뷰 데이터 반환
		return ResponseEntity.ok(Map.of("reviewDatas", reviewDatas));
	}

	// 상품 리뷰 등록하기 (최초)
	@PreAuthorize("hasRole('USER')")
	@PostMapping(value = "/user/review/{itemPk}", consumes = "multipart/form-data")
	// /user/review/{itemPk}에 들어온 요청중에 콘텐트타입이 "multipart/form-data"인 부분만 받겠다
	//주로 파일업로드 + 폼데이터 같이 받을때 사용함
	public ResponseEntity<?> registReview(
			@PathVariable("itemPk") Integer itemPk, //요청url의 상품pk
			@ModelAttribute ReviewDTO reviewDTO, //multipart/form-data의 폼 필드 + 파일을 DTO로 바인딩
			@AuthenticationPrincipal OrnablyUser ornablyUser) { //스프링 시큐리티가 주는 현재 로그인 사용자 정보

		// 이미지가 리뷰에 포함되어 있다면
		if (reviewDTO.getReviewImage() != null) {
			// 파일 크기 검사
			//조건을 통과하지 못하면 400에러 발생시김
			if (!this.reviewService.checkFileSize(reviewDTO.getReviewImage())) {
				return ResponseEntity.badRequest()
						.body(Map.of("code", "IMAGE_SIZE_TOO_LARGE", "message", "이미지 크기는 10MB이하여야 합니다."));
			}

			// 파일 확장자 명 검사
			if (!this.reviewService.checkFileExtention(reviewDTO.getReviewImage())) {
				return ResponseEntity.badRequest().body(
						Map.of("code", "IMAGE_EXTENTION_TYPE_ERROR", "message",
								"확장자는 " + this.reviewService.getAllowedExtentionSet() + "만 가능합니다."));
			}
		}
		//어떤상품의 리뷰인지 리뷰 DTO에 넣어주기
		reviewDTO.setItemPk(itemPk);
		//어떤사용자가 작성한 리뷰인지 리뷰DTO에 넣어주기
		reviewDTO.setAccountPk(ornablyUser.getAccountPk());

		// 들어온 데이터에 이상이 없다면 등록 시도해주기
		boolean success = this.reviewService.registReview(reviewDTO);
		if (success) {//성공시 201 코드 생성
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} else {//실패시 500에러 발생
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	//리뷰 수정하기
	// reviewTitle, reviewContent를 받아서 수정해주기
	@PreAuthorize("hasRole('USER')")
	@PatchMapping(value = "/user/review/{reviewPk}")
	public ResponseEntity<?> updateReviewByUser(
			@PathVariable("reviewPk") Integer reviewPk, //요청url에서 리뷰pk가져오기
			@RequestBody ReviewDTO reviewDTO) {
		reviewDTO.setReviewPk(reviewPk);// 리뷰DTO에 수정할 리뷰 PK넣어주기
		if (this.reviewService.updateReview(reviewDTO)) { // 리뷰수정성공시 200 반환
			return ResponseEntity.status(HttpStatus.OK).build();
		} else {//실패시 500에러 발생
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// 관리자 리뷰 삭제
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping(value = "/admin/review/{reviewPk}")
	public ResponseEntity<?> deleteUserReviewByAdmin(@PathVariable("reviewPk") Integer reviewPk) {
		if (this.reviewService.deleteReviewByReviewPk(reviewPk)) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.internalServerError().build();
		}
	}

	//사용자 리뷰 삭제
	@PreAuthorize("hasRole('USER')")
	@DeleteMapping(value = "/user/review/{reviewPk}")
	public ResponseEntity<?> deleteUserReviewByUser(@PathVariable("reviewPk") Integer reviewPk) {
		if (this.reviewService.deleteReviewByReviewPk(reviewPk)) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.internalServerError().build();
		}
	}

}
