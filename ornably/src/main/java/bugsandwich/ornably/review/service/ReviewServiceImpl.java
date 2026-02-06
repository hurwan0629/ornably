package bugsandwich.ornably.review.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import bugsandwich.ornably.review.ReviewDTO;
import bugsandwich.ornably.review.ReviewRepository;

public class ReviewServiceImpl implements ReviewService{

	@Autowired
	private ReviewRepository reviewRepository;
	
	@Override
	public List<ReviewDTO> getReviewByAccountPk(Integer accountPk) {
		ReviewDTO reviewDTO = new ReviewDTO();
		reviewDTO.setCondition("SELECT_ALL_REVIEW_BY_ACCOUNT_PK");
		reviewDTO.setAccountPk(accountPk);
		
		return reviewRepository.selectAll(reviewDTO);;
	}
}
