package bugsandwich.ornably.review.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import bugsandwich.ornably.review.ReviewDTO;

public interface ReviewService {
	
	// 조회 기능
	List<ReviewDTO> getReviewByAccountPk(Integer accountPk);
	List<ReviewDTO> getReviewByItemPk(ReviewDTO ReviewDTO);
	List<ReviewDTO> getReviewDatasByReviewPkAdmin(Integer itemPk);
	ReviewDTO getReviewDataByReviewPk(Integer reviewPk);
	ReviewDTO getReviewMaxPageByItemPkAndDataCount(ReviewDTO reviewDTO);
	
	// 작성 기능
	boolean registReview(ReviewDTO reviewDTO);
	boolean updateReview(ReviewDTO reviewDTO);
	
	// 삭제 기능
	boolean deleteReviewByReviewPk(Integer reviewPk);
	
	// util
	boolean checkFileSize(MultipartFile file);
	boolean checkFileExtention(MultipartFile file);
	public String saveImageAndGetUrl(String resourcePath, String prefix, MultipartFile file) throws IOException;
	
	
	// getter
	public Set<String> getAllowedExtentionSet();
	public Long getAllowedImageMaxBytes();
	
}
