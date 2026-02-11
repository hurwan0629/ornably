package bugsandwich.ornably.wishlist;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class WishlistRepository {
	@Autowired // 의존주입
	private JdbcTemplate jdbcTemplate;
	
	// 회원의 좋아요 목록 전체 조회
    private static final String SELECT_ALL_WISHLIST_BY_ACCOUNT_PK =
            "SELECT " +
            "  w.ITEM_PK AS itemPk, " +
            "  i.ITEM_IMAGE_URL AS itemImageUrl, " +
            "  i.ITEM_NAME AS itemName, " +
            "  i.ITEM_PRICE AS itemPrice, " +
            "  i.ITEM_DISCOUNT_RATE AS itemDiscountRate, " +
            "  ROUND(i.ITEM_PRICE * (100 - i.ITEM_DISCOUNT_RATE) / 100) AS itemDiscountPrice " +
            "FROM WISHLIST w " +
            "JOIN ITEM i ON w.ITEM_PK = i.ITEM_PK " +
            "WHERE w.ACCOUNT_PK = ? " +
            "ORDER BY w.WISHLIST_PK ASC";

	// 좋아요 존재 여부 확인
	private static final String SELECT_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK =
	    "SELECT " +
	    "  WISHLIST_PK AS wishlistPk, " +
	    "  ACCOUNT_PK  AS accountPk, " +
	    "  ITEM_PK     AS itemPk " +
	    "FROM WISHLIST " +
	    "WHERE ACCOUNT_PK = ? " +
	    "AND ITEM_PK = ?";

	// 좋아요 존재 시 삭제
	private static final String DELETE_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK =
	    "DELETE FROM WISHLIST " +
	    "WHERE ACCOUNT_PK = ? " +
	    "AND ITEM_PK = ?";

	// 좋아요 전체 삭제
	private static final String DELETE_ALL_WISHLIST_BY_ACCOUNT_PK =
	    "DELETE FROM WISHLIST " +
	    "WHERE ACCOUNT_PK = ?";

	// 좋아요 추가
	private static final String INSERT_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK =
	    "INSERT INTO WISHLIST (ACCOUNT_PK, ITEM_PK) " +
	    "VALUES (?, ?)";
	
	
	
	
	public List<WishlistDTO> selectAll(WishlistDTO wishlistDTO) {
		System.out.println("[로그] WishListRepository의 selectAll 시작");
		
	    if ("SELECT_ALL_WISHLIST_BY_ACCOUNT_PK".equals(wishlistDTO.getCondition())) {
	        System.out.println("[로그] selectAll의 SELECT_ALL_WISHLIST_BY_ACCOUNT_PK");

	        return jdbcTemplate.query(
	            SELECT_ALL_WISHLIST_BY_ACCOUNT_PK,
	            new BeanPropertyRowMapper<>(WishlistDTO.class),
	            wishlistDTO.getAccountPk()
			);
		}
	    System.out.println("[로그][경고] WishListRepository_selectAll_condition 없음");
	    // 조건이 없으면 빈 리스트 반환
	    return java.util.Collections.emptyList();
	}
	
	
	public WishlistDTO selectOne(WishlistDTO wishlistDTO) {
		System.out.println("[로그] WishListRepository의 selectOne 시작");
		
		//회원고유번호가져와서 해당 회원이 좋아요 누른 상품고유번호가 존재하는지 확인
		if ("SELECT_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK".equals(wishlistDTO.getCondition())) {
	        System.out.println("[로그] selectOne의 SELECT_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK");
	        List<WishlistDTO> list = jdbcTemplate.query(
	            SELECT_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK,
	            new BeanPropertyRowMapper<>(WishlistDTO.class),
	            wishlistDTO.getAccountPk(),
	            wishlistDTO.getItemPk()
	        );
	        return list.isEmpty() ? null : list.get(0);
	    }
	    else {
		    System.out.println("[로그][경고] WishListRepository_selectOne_condition 없음");
	    }
	    return null;
	}
	

	public boolean insert(WishlistDTO wishlistDTO) {
		System.out.println("[로그] WishListRepository의 insert 시작");
		int result = 0;
		
		//회원이 좋아요 누른 아이템 생성하기
		if ("INSERT_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK".equals(wishlistDTO.getCondition())) {
			System.out.println("[로그] insert의 INSERT_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK");			
			result = jdbcTemplate.update(
				INSERT_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK,
				wishlistDTO.getAccountPk(),
				wishlistDTO.getItemPk());
		}
	    else {
		    System.out.println("[로그][경고] WishListRepository_insert_condition 없음");
	    }
		return result > 0;
		
	}
	
	
	private boolean update(WishlistDTO wishlistDTO) {
		return false;
	}
	
	
	public boolean delete(WishlistDTO wishlistDTO) {
		System.out.println("[로그] WishListRepository의 delete 시작");
		int result = 0;
		
		//해당아이템에 회원의 좋아요가 있을시 삭제하기
		if("DELETE_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK".equals(wishlistDTO.getCondition())) {
			System.out.println("[로그] delete의 DELETE_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK");
			result = jdbcTemplate.update(
				DELETE_WISHLIST_BY_ACCOUNT_PK_AND_ITEM_PK,
				wishlistDTO.getAccountPk(),
				wishlistDTO.getItemPk()
			);
		}
		
		//회원고유번호에 대한 모든 좋아요 삭제하기
		else if("DELETE_ALL_WISHLIST_BY_ACCOUNT_PK".equals(wishlistDTO.getCondition())) {
			System.out.println("[로그] delete의 DELETE_ALL_WISHLIST_BY_ACCOUNT_PK");
			result = jdbcTemplate.update(
				DELETE_ALL_WISHLIST_BY_ACCOUNT_PK,
				wishlistDTO.getAccountPk()
			);
		}
		else {
		    System.out.println("[로그][경고] WishListRepository_delete_condition 없음");
	    }
		return result > 0;
	}
}


