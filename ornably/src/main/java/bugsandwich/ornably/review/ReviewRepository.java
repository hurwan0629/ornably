package bugsandwich.ornably.review;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewRepository {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	// 리뷰 작성
	private static final String INSERT_REVIEW_WRITE =
	    "INSERT INTO REVIEW " +
	    "(ACCOUNT_PK, ITEM_PK, REVIEW_TITLE, REVIEW_CONTENT, REVIEW_STAR, REVIEW_IMAGE_URL) " +
	    "VALUES (?, ?, ?, ?, ?, ?)";

	// 상품별 리뷰 총개수 조회
	private static final String SELECT_ITEM_REVIEW_COUNT =
	    "SELECT COUNT(*) AS reviewTotalCount " +
	    "FROM REVIEW " +
	    "WHERE ITEM_PK = ?";

	// 리뷰수정 전 기존 리뷰 정보 가져오기
	private static final String SELECT_UPDATE_REVIEW_DATA =
	    "SELECT " +
	    "   R.REVIEW_PK		AS reviewPk, " +
	    "   A.ACCOUNT_NAME	AS accountName, " +
	    "   R.REVIEW_TITLE	AS reviewTitle, " +
	    "   R.REVIEW_STAR	AS reviewStar, " +
	    "   R.REVIEW_CONTENT AS reviewContent " +
	    "FROM REVIEW R " +
	    "JOIN ACCOUNT A ON R.ACCOUNT_PK = A.ACCOUNT_PK " +
	    "WHERE R.REVIEW_PK = ? " +
	    "AND R.ACCOUNT_PK = ?";

	
	// 내가 쓴 리뷰목록
	private static final String SELECT_MY_REVIEW_LIST =
	    "SELECT " +
	    "   R.REVIEW_PK		AS reviewPk, " +
	    "   I.ITEM_PK       AS itemPk, " +
	    "   R.REVIEW_TITLE  AS reviewTitle, " +
	    "   R.REVIEW_STAR   AS reviewStar, " +
	    "   R.REVIEW_CONTENT 	AS reviewContent, " +
	    "   I.ITEM_NAME			AS itemName, " +
	    "   I.ITEM_IMAGE_URL	AS itemImageUrl, " +
	    "   I.ITEM_PRICE		AS itemPrice " +
	    "FROM REVIEW R " +
	    "INNER JOIN ITEM I ON R.ITEM_PK = I.ITEM_PK " +
	    "WHERE R.ACCOUNT_PK = ?";
	
	
	// 상품리뷰 존재확인
	private static final String SELECT_EXIST_REVIEW_BY_ACCOUNT_ITEM =
	    "SELECT COUNT(*) AS reviewTotalCount " +
	    "FROM REVIEW " +
	    "WHERE ACCOUNT_PK = ? " +
	    "AND ITEM_PK = ?";

	
	// 상품별 별점
	private static final String SELECT_ALL_REVIEW_STAR_BY_ITEM_PK =
	    "SELECT REVIEW_STAR AS reviewStar " +
	    "FROM REVIEW " +
	    "WHERE ITEM_PK = ? " +
	    "ORDER BY REVIEW_PK DESC";

	// 리뷰 페이지네이션
	private static final String SELECT_ALL_REVIEW_PAGENATION_BY_ITEM_PK =
	    "SELECT reviewPk, itemPk, reviewTitle, reviewContent, reviewStar, accountName " +
	    "FROM ( " +
	    "   SELECT " +
	    "       R.REVIEW_PK        AS reviewPk, " +
	    "       R.ITEM_PK          AS itemPk, " +
	    "       R.REVIEW_TITLE     AS reviewTitle, " +
	    "       R.REVIEW_CONTENT   AS reviewContent, " +
	    "       R.REVIEW_STAR      AS reviewStar, " +
	    "       A.ACCOUNT_NAME     AS accountName, " +
	    "       ROW_NUMBER() OVER (ORDER BY R.REVIEW_PK DESC) AS rn " +
	    "   FROM REVIEW R " +
	    "   JOIN ACCOUNT A ON R.ACCOUNT_PK = A.ACCOUNT_PK " +
	    "   WHERE R.ITEM_PK = ? " +
	    ") T " +
	    "WHERE rn BETWEEN ? AND ?";


	// 회원 고유번호의 모든 리뷰 삭제
	private static final String DELETE_ALL_REVIEW_BY_ACCOUNT_PK =
	    "DELETE FROM REVIEW WHERE ACCOUNT_PK = ?";

	// 회원 고유번호의 리뷰 개별 삭제
	private static final String DELETE_BY_REVIEW_PK =
	    "DELETE FROM REVIEW WHERE REVIEW_PK = ?";

	// 리뷰 수정
	private static final String REVIEW_WRITE_EDIT =
	    "UPDATE REVIEW SET " +
	    "    REVIEW_TITLE   = ?, " +
	    "    REVIEW_CONTENT = ? " +
	    "WHERE REVIEW_PK = ?";

	
	// 사용자 리뷰 수정 전 기존 리뷰 정보 가져오기
	private static final String SELECT_REVIEW_DATA_BY_REVIEW_PK =
	    "SELECT " +
	    "    REVIEW_PK        AS reviewPk, " +
	    "    REVIEW_TITLE     AS reviewTitle, " +
	    "    REVIEW_CONTENT   AS reviewContent, " +
	    "    REVIEW_IMAGE_URL AS reviewImageUrl, " +
	    "    REVIEW_STAR      AS reviewStar " +
	    "FROM REVIEW " +
	    "WHERE REVIEW_PK = ?";
	
    // ==============
 	//   관리자 쿼리문
 	// ==============
 	
	// 특정 회원이 작성한 리뷰 전체 조회
	private static final String SELECT_ALL_REVIEW_BY_ACCOUNT_PK =
	    "SELECT " +
	    "    r.REVIEW_PK        AS reviewPk, " +
	    "    r.REVIEW_IMAGE_URL AS reviewImageUrl, " +
	    "    r.REVIEW_DATE      AS reviewDate, " +
	    "    r.REVIEW_TITLE     AS reviewTitle, " +
	    "    r.REVIEW_CONTENT   AS reviewContent, " +
	    "    r.REVIEW_STAR      AS reviewStar " +
	    "FROM REVIEW r " +
	    "WHERE r.ACCOUNT_PK = ? " +
	    "ORDER BY r.REVIEW_DATE DESC";

	
	// 특정 상품에 달린 리뷰 전부 조회
	private static final String SELECT_ALL_REVIEW_DATAS_BY_ITEM_PK_ADMIN_VIEW =
	    "SELECT " +
	    "    R.REVIEW_PK        AS reviewPk, " +
	    "    R.REVIEW_IMAGE_URL AS reviewImageUrl, " +
	    "    R.REVIEW_TITLE     AS reviewTitle, " +
	    "    R.REVIEW_CONTENT   AS reviewContent, " +
	    "    R.REVIEW_STAR      AS reviewStar, " +
	    "    R.ACCOUNT_PK       AS reviewAccountPk, " +
	    "    A.ACCOUNT_NAME     AS reviewAccountName, " +
	    "    DATE(R.REVIEW_DATE) AS reviewDate " +
	    "FROM REVIEW R " +
	    "JOIN ACCOUNT A ON R.ACCOUNT_PK = A.ACCOUNT_PK " +
	    "WHERE R.ITEM_PK = ? " +
	    "ORDER BY R.REVIEW_DATE DESC";

	
	
	
	
	public List<ReviewDTO> selectAll(ReviewDTO reviewDTO) {
		System.out.println("[로그] ReviewRepository의 selectAll 시작");
		
		// 아이템 고유번호 가져와서 상품별 별점 보여주기
		if ("SELECT_ALL_REVIEW_STAR_BY_ITEM_PK".equals(reviewDTO.getCondition())) {
			System.out.println("[로그] selectAll의 SELECT_ALL_REVIEW_STAR_BY_ITEM_PK");
			return jdbcTemplate.query(
				SELECT_ALL_REVIEW_STAR_BY_ITEM_PK,
				new BeanPropertyRowMapper<>(ReviewDTO.class),
				reviewDTO.getItemPk()
			);
		}
		
		// 리뷰 페이지 페이지 넘기기 (페이지 네이션)
		else if ("SELECT_ALL_REVIEW_PAGENATION_BY_ITEM_PK".equals(reviewDTO.getCondition())) {
			System.out.println("[로그] selectAll의 SELECT_ALL_REVIEW_PAGENATION_BY_ITEM_PK");
		    return jdbcTemplate.query(
		    		SELECT_ALL_REVIEW_PAGENATION_BY_ITEM_PK,
		            (rs, rowNum) -> {
		                ReviewDTO data = new ReviewDTO();
		                data.setReviewPk(rs.getInt("reviewPk"));
		                data.setItemPk(rs.getInt("itemPk"));
		                data.setReviewTitle(rs.getString("reviewTitle"));
		                data.setReviewContent(rs.getString("reviewContent"));
		                data.setReviewStar(rs.getInt("reviewStar"));
		                data.setReviewAccountName(rs.getString("accountName"));
		                return data;
		            },
		            reviewDTO.getItemPk(),
		            reviewDTO.getStartReviewNum(),
		            reviewDTO.getEndReviewNum()
		        );
		    }
		
		// 내가 작성한 리뷰 목록보기
		else if ("SELECT_MY_REVIEW_LIST".equals(reviewDTO.getCondition())) {
			System.out.println("[로그] selectAll의 SELECT_MY_REVIEW_LIST");
			return jdbcTemplate.query(
				SELECT_MY_REVIEW_LIST,
				(rs, rowNum) -> {
					ReviewDTO data = new ReviewDTO();
					data.setReviewPk(rs.getInt("reviewPk"));
					data.setItemPk(rs.getInt("itemPk"));
					data.setReviewTitle(rs.getString("reviewTitle"));
					data.setReviewContent(rs.getString("reviewContent"));
					data.setReviewStar(rs.getInt("reviewStar"));
					data.setItemName(rs.getString("itemName"));
					data.setItemImageUrl(rs.getString("itemImageUrl"));
					data.setItemPrice(rs.getInt("itemPrice"));
					return data;
				},
				reviewDTO.getAccountPk()
			);
		}
		
		// 관리자용 : 특정 회원이 작성한 리뷰 전체 조회
		else if("SELECT_ALL_REVIEW_BY_ACCOUNT_PK".equals(reviewDTO.getCondition())) {
			System.out.println("[로그] selectAll의 SELECT_ALL_REVIEW_BY_ACCOUNT_PK");
			return jdbcTemplate.query(
				SELECT_ALL_REVIEW_BY_ACCOUNT_PK,
				(rs, rowNum) -> {
					ReviewDTO data = new ReviewDTO();
		            data.setReviewPk(rs.getInt("reviewPk"));
		            data.setReviewImageUrl(rs.getString("reviewImageUrl"));
		            data.setReviewDate(rs.getDate("reviewDate").toLocalDate());
		            data.setReviewTitle(rs.getString("reviewTitle"));
		            data.setReviewContent(rs.getString("reviewContent"));
		            data.setReviewStar(rs.getInt("reviewStar"));
					return data;
				},
				reviewDTO.getAccountPk()
			);
		}
		
		// 관리자용 : 특정 상품에 달린 리뷰 전부 조회
		else if("SELECT_ALL_REVIEW_DATAS_BY_ITEM_PK_ADMIN_VIEW".equals(reviewDTO.getCondition())) {
			System.out.println("[로그] selectAll의 SELECT_ALL_REVIEW_DATAS_BY_ITEM_PK_ADMIN_VIEW");
			return jdbcTemplate.query(
				SELECT_ALL_REVIEW_DATAS_BY_ITEM_PK_ADMIN_VIEW,
				(rs, rowNum) -> {
			        ReviewDTO dto = new ReviewDTO();
			        dto.setReviewPk(rs.getInt("reviewPk"));
			        dto.setReviewImageUrl(rs.getString("reviewImageUrl"));
			        dto.setReviewTitle(rs.getString("reviewTitle"));
			        dto.setReviewContent(rs.getString("reviewContent"));
			        dto.setReviewStar(rs.getInt("reviewStar"));
			        dto.setReviewAccountPk(rs.getInt("reviewAccountPk"));
			        dto.setReviewAccountName(rs.getString("reviewAccountName"));
			        dto.setReviewDate(rs.getDate("reviewDate").toLocalDate());
			        return dto;
			    },
			    reviewDTO.getItemPk()
			);
		}
		System.out.println("[로그][경고] ReviewRepository의 selectAll_condition 없음");
		return null;
	}

	
	
	public ReviewDTO selectOne(ReviewDTO reviewDTO) {
		System.out.println("[로그] ReviewRepository의 selectOne 시작");
		
		// 상품별 리뷰 목록 가져오기
		if ("SELECT_ITEM_REVIEW_COUNT".equals(reviewDTO.getCondition())) {
			System.out.println("[로그] selectOne의 SELECT_ITEM_REVIEW_COUNT");
			return jdbcTemplate.queryForObject(
				SELECT_ITEM_REVIEW_COUNT,
				(rs, rowNum) -> {
					ReviewDTO data = new ReviewDTO();
					data.setReviewTotalCount(rs.getInt("reviewTotalCount"));
					return data;
				},
				reviewDTO.getItemPk()
			);
		}
	
		// 리뷰수정전 기존 리뷰 정보 가져오기
		else if ("SELECT_UPDATE_REVIEW_DATA".equals(reviewDTO.getCondition())) {
			System.out.println("[로그] selectOne의 SELECT_UPDATE_REVIEW_DATA");
			return jdbcTemplate.queryForObject(
				SELECT_UPDATE_REVIEW_DATA,
				(rs, rowNum) -> {
					ReviewDTO data = new ReviewDTO();
					data.setReviewAccountName(rs.getString("accountName"));
					data.setReviewTitle(rs.getString("reviewTitle"));
					data.setReviewStar(rs.getInt("reviewStar"));
					data.setReviewContent(rs.getString("reviewContent"));
					return data;
				},
				reviewDTO.getReviewPk(),
				reviewDTO.getAccountPk()
			);
		}

		// 상품 리뷰 존재 확인
		else if ("SELECT_EXIST_REVIEW_BY_ACCOUNT_ITEM".equals(reviewDTO.getCondition())) {
	        System.out.println("[로그] ReviewRepository의 SELECT_EXIST_REVIEW_BY_ACCOUNT_ITEM");
	        return jdbcTemplate.queryForObject(
        	    SELECT_EXIST_REVIEW_BY_ACCOUNT_ITEM,
        	    (rs, rowNum) -> {
        	        ReviewDTO data = new ReviewDTO();
        	        data.setReviewTotalCount(rs.getInt("reviewTotalCount"));
        	        return data;
        	    },
        	    reviewDTO.getAccountPk(),
        	    reviewDTO.getItemPk()
        	);
	    }
		
		else if("SELECT_REVIEW_DATA_BY_REVIEW_PK".equals(reviewDTO.getCondition())) {
			 System.out.println("[로그] ReviewRepository의 SELECT_REVIEW_DATA_BY_REVIEW_PK");
			 return jdbcTemplate.queryForObject(
		        SELECT_REVIEW_DATA_BY_REVIEW_PK,
		        (rs, rowNum) -> {
		            ReviewDTO dto = new ReviewDTO();
		            dto.setReviewPk(rs.getInt("reviewPk"));
		            dto.setReviewTitle(rs.getString("reviewTitle"));
		            dto.setReviewContent(rs.getString("reviewContent"));
		            dto.setReviewImageUrl(rs.getString("reviewImageUrl"));
		            dto.setReviewStar(rs.getInt("reviewStar"));
		            return dto;
		        },
		        reviewDTO.getReviewPk()
		    );
		}
		System.out.println("[로그][경고] ReviewRepository의 selectOne_condition 없음");
		return null;
	}
	

	public boolean insert(ReviewDTO reviewDTO) {
		System.out.println("[로그] ReviewRepository의 insert 시작");
		int result = 0;
		
		// 리뷰 작성하기 (등록)
		if ("INSERT_REVIEW_WRITE".equals(reviewDTO.getCondition())) {
			result = jdbcTemplate.update(
				INSERT_REVIEW_WRITE,
				reviewDTO.getAccountPk(),
				reviewDTO.getItemPk(),
				reviewDTO.getReviewTitle(),
				reviewDTO.getReviewContent(),
				reviewDTO.getReviewStar(),
				reviewDTO.getReviewImageUrl()
			);
		}
		else {
			System.out.println("[로그][경고] ReviewRepository_insert_condition 없음");
		}
		return result > 0;
	}

	
	public boolean update(ReviewDTO reviewDTO) {
		System.out.println("[로그] ReviewRepository의 update 시작");
		int result = 0;
		
		// 리뷰 수정하기
		if ("REVIEW_WRITE_EDIT".equals(reviewDTO.getCondition())) {
			result = jdbcTemplate.update(
				REVIEW_WRITE_EDIT,
				reviewDTO.getReviewTitle(),
				reviewDTO.getReviewContent(),
				reviewDTO.getReviewPk()
			);
		}		
		else {
			System.out.println("[로그][경고] ReviewRepository_update_condition 없음");
		}
		return result > 0;
	}

	
	public boolean delete(ReviewDTO reviewDTO) {
		System.out.println("[로그] ReviewRepository의 delete 시작");
		int result = 0;
		
		// 회원고유번호에 대한 리뷰 모두삭제
		if ("DELETE_ALL_REVIEW_BY_ACCOUNT_PK".equals(reviewDTO.getCondition())) {
			System.out.println("[로그] delete의 DELETE_ALL_REVIEW_BY_ACCOUNT_PK");
			result = jdbcTemplate.update(
				DELETE_ALL_REVIEW_BY_ACCOUNT_PK,
				reviewDTO.getAccountPk()
			);
		}
		
		// 회원고유번호에 대한 리뷰 개별삭제
		else if ("DELETE_BY_REVIEW_PK".equals(reviewDTO.getCondition())) {
			System.out.println("[로그] delete의 DELETE_BY_REVIEW_PK");
			result = jdbcTemplate.update(
				DELETE_BY_REVIEW_PK,
				reviewDTO.getReviewPk()
			);
		}
		else {
			System.out.println("[로그][경고] ReviewRepository_delete_condition 없음");
		}
		return result > 0;
	}
}


