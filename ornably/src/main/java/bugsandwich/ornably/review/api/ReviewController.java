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
	
	// 내 리뷰 전체 보기
	@PreAuthorize("hasRole('USER')")
	@GetMapping("/user/review/me")
	public ResponseEntity<?> getReviewDatasByAccountPk(
			@AuthenticationPrincipal OrnablyUser ornablyUser
			) {
		List<ReviewDTO> reviewDatas = this.reviewService.getReviewByAccountPk(ornablyUser.getAccountPk());
		
		return ResponseEntity.ok(
				Map.of("reviewDatas", reviewDatas));
	}
	
	
	// 상품 상세페이지 리뷰 보기
	@GetMapping("/all/review/item-detail-page")
	public ResponseEntity<?> getItemDetailPageReview(@ModelAttribute ReviewDTO reviewDTO) {
		if (reviewDTO.getItemPk() == null || reviewDTO.getItemPk() == 0) {
			return ResponseEntity.status(400).body(Map.of("code", "INVALID_ITEM_PK", "message", "요청 값이 올바르지 않습니다."));
		}

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
		// reviewPk - Integer
		// reviewTitle - String
		// reviewContent - String
		// reviewImage - MultipartFile
		// reviewStar - Integer
		ReviewDTO reviewDTO = reviewService.getReviewDataByReviewPk(reviewPk);

		return ResponseEntity.ok(Map.of("reviewPk", reviewDTO.getReviewPk(), "reviewTitle", reviewDTO.getReviewTitle(),
				"reviewContent", reviewDTO.getReviewContent(), "reviewImageUrl", reviewDTO.getReviewImageUrl(),
				"reviewStar", reviewDTO.getReviewStar()));
	}
	
	// 관리자 상품 관리시 상품 리뷰 조회
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping(value = "/admin/item/{itemPk}/review")
	public ResponseEntity<?> getUserReviewDatasByItemPkAdmin(@PathVariable("itemPk") Integer itemPk){
		List<ReviewDTO> reviewDatas = reviewService.getReviewDatasByReviewPkAdmin(itemPk);

		return ResponseEntity.ok(Map.of("reviewDatas", reviewDatas));
	}

	// 상품 리뷰 등록하기 (최초)
	@PreAuthorize("hasRole('USER')")
	@PostMapping(value = "/user/review/{itemPk}", consumes = "multipart/form-data")
	public ResponseEntity<?> registReview(@PathVariable("itemPk") Integer itemPk, @ModelAttribute ReviewDTO reviewDTO,
			@AuthenticationPrincipal OrnablyUser ornablyUser) {

		// 이미지가 리뷰에 포함되어 있다면
		if (reviewDTO.getReviewImage() != null) {
			// 파일 크기 검사
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

		reviewDTO.setItemPk(itemPk);
		reviewDTO.setAccountPk(ornablyUser.getAccountPk());

		// 들어온 데이터에 이상이 없다면 등록 시도해주기
		boolean success = this.reviewService.registReview(reviewDTO);
		if (success) {
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// reviewTitle, reviewContent를 받아서 수정해주기
	@PreAuthorize("hasRole('USER')")
	@PatchMapping(value = "/user/review/{reviewPk}")
	public ResponseEntity<?> updateReviewByUser(
			@PathVariable("reviewPk") Integer reviewPk,
			@RequestBody ReviewDTO reviewDTO) {
		reviewDTO.setReviewPk(reviewPk);
		if (this.reviewService.updateReview(reviewDTO)) {
			return ResponseEntity.status(HttpStatus.OK).build();
		} else {
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

	// 관리자 리뷰 삭제
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
