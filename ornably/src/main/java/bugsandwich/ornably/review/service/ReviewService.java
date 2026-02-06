package bugsandwich.ornably.review.service;

import java.util.List;

import bugsandwich.ornably.review.ReviewDTO;

public interface ReviewService {
	public List<ReviewDTO> getReviewByAccountPk(Integer accountPk);
}
