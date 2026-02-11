package bugsandwich.ornably.review.service;

import java.util.List;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import bugsandwich.ornably.review.ReviewDTO;

public interface ReviewService {
	
	// 조회 기능
	List<ReviewDTO> getReviewByAccountPk(Integer accountPk); //사용자pk로 리뷰조회
	List<ReviewDTO> getReviewByItemPk(ReviewDTO ReviewDTO); // 상품pk로 리뷰 조회
	List<ReviewDTO> getReviewDatasByReviewPkAdmin(Integer itemPk); //관리자가 상품pk로 리뷰 조회
	ReviewDTO getReviewDataByReviewPk(Integer reviewPk);//리뷰pk로 리뷰 데이터 조회
	// 작성 기능
	boolean registReview(ReviewDTO reviewDTO); //리뷰작성
	boolean updateReview(ReviewDTO reviewDTO); //리뷰수정
	
	// 삭제 기능
	boolean deleteReviewByReviewPk(Integer reviewPk);
	
	// util
	boolean checkFileSize(MultipartFile file); //리뷰에 사용할 이미지 사이즈
	boolean checkFileExtention(MultipartFile file); // 리뷰에 사용할 파일 확장자명
	
	// getter
	public Set<String> getAllowedExtentionSet();
	public Long getAllowedImageMaxBytes();
}
