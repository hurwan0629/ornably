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
		    // 로그인 사용자 ACCOUNT 정보 미리 계산
		    "WITH acct AS ( " +
		    "  SELECT " +
		    "    a.ACCOUNT_PK AS accountPk, " +
		    "    DATE(a.ACCOUNT_DATE) AS joinedDate, " +
		    "    a.ACCOUNT_ROLE AS accountRole, " +
		    "    IFNULL(SUM(oi.ORDERS_ITEM_PRICE * oi.ORDERS_ITEM_COUNT), 0) AS totalAmount " +
		    "  FROM ACCOUNT a " +
		    "  LEFT JOIN ORDERS o ON o.ACCOUNT_PK = a.ACCOUNT_PK " +
		    "  LEFT JOIN ORDERS_ITEM oi ON oi.ORDERS_PK = o.ORDERS_PK " +
		    "  WHERE a.ACCOUNT_PK = ? " +   // (1) 로그인 사용자 PK
		    "  GROUP BY a.ACCOUNT_PK, DATE(a.ACCOUNT_DATE), a.ACCOUNT_ROLE " +
		    "), " +

		    // 위시리스트 기반으로 ITEM 목록 뽑기 (여기가 핵심!)
		    "item_base AS ( " +
		    "  SELECT " +
		    "    w.WISHLIST_PK AS wishlistPk, " +
		    "    i.ITEM_PK AS itemPk, " +
		    "    i.ITEM_NAME AS itemName, " +
		    "    i.ITEM_PRICE AS itemPrice, " +
		    "    i.ITEM_IMAGE_URL AS itemImageUrl, " +
		    "    i.ITEM_CATEGORY AS itemCategory " +
		    "  FROM WISHLIST w " +
		    "  JOIN ITEM i ON i.ITEM_PK = w.ITEM_PK " +
		    "  WHERE w.ACCOUNT_PK = ? " +   // (2) 찜 목록 주인 PK
		    ") " +

		    "SELECT " +
		    "  ib.itemPk, " + 
		    "  ib.itemImageUrl, " +
		    "  ib.itemName, " +
		    "  ib.itemPrice, " +

		    // 이벤트 최대 할인율
		    "  IFNULL(MAX(e.EVENT_DISCOUNT_RATE), 0) AS itemDiscountRate, " +

		    // 할인가 (최대 할인율 기준)
		    "  CASE " +
		    "    WHEN MAX(e.EVENT_DISCOUNT_RATE) IS NOT NULL " +
		    "    THEN ROUND(ib.itemPrice * (1 - MAX(e.EVENT_DISCOUNT_RATE)/100), 0) " +
		    "    ELSE ib.itemPrice " +
		    "  END AS itemDiscountPrice " +

		    "FROM item_base ib " +

		    // ACCOUNT join (이벤트 조건 계산용)
		    "LEFT JOIN acct a ON 1=1 " +

		    // 이벤트 join
		    "LEFT JOIN EVENT e " +
		    "  ON JSON_CONTAINS(e.EVENT_TARGET_CATEGORY, JSON_QUOTE(ib.itemCategory)) " +
		    " AND CURRENT_DATE BETWEEN e.EVENT_START_DATE AND e.EVENT_END_DATE " +
		    " AND ( " +
		    "       ( ? IS NULL AND e.EVENT_TARGET_ACCOUNT->>'$.type' = 'ALL' ) " + // (3) 비로그인
		    "    OR ( ? IS NOT NULL AND ( " +                                      // (4) 로그인
		    "         e.EVENT_TARGET_ACCOUNT->>'$.type' = 'ALL' " +
		    "      OR (e.EVENT_TARGET_ACCOUNT->>'$.type' = 'AMOUNT' " +
		    "          AND a.totalAmount >= CAST(e.EVENT_TARGET_ACCOUNT->>'$.amount' AS UNSIGNED)) " +
		    "      OR (e.EVENT_TARGET_ACCOUNT->>'$.type' = 'JOINED' " +
		    "          AND a.joinedDate BETWEEN " +
		    "              STR_TO_DATE(e.EVENT_TARGET_ACCOUNT->>'$.startDate','%Y-%m-%d') " +
		    "              AND STR_TO_DATE(e.EVENT_TARGET_ACCOUNT->>'$.endDate','%Y-%m-%d')) " +
		    "      OR (e.EVENT_TARGET_ACCOUNT->>'$.type' = 'MEMBER_TYPE' " +
		    "          AND JSON_CONTAINS(JSON_EXTRACT(e.EVENT_TARGET_ACCOUNT,'$.memberType'), JSON_QUOTE(a.accountRole))) " +
		    "    )) " +
		    " ) " +

		    // 집계 때문에 GROUP BY
		    "GROUP BY ib.wishlistPk, ib.itemPk, ib.itemImageUrl, ib.itemName, ib.itemPrice " +

		    // 찜한 순서 유지
		    "ORDER BY ib.wishlistPk ASC";


	/*
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
	*/
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
	            wishlistDTO.getAccountPk(),
	            wishlistDTO.getAccountPk(),
	            wishlistDTO.getAccountPk(),
	            wishlistDTO.getAccountPk()
			);
		}
	    else {
		    System.out.println("[로그][경고] WishListRepository_selectAll_condition 없음");
	    }
	    return List.of();
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
	
	
	public boolean update(WishlistDTO wishlistDTO) {
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