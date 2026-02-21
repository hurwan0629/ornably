package bugsandwich.ornably.cart;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CartRepository {
	@Autowired // 의존 주입
	private JdbcTemplate jdbcTemplate;
	
	// 회원 장바구니 목록 조회 (상품 정보 JOIN)
	// 장바구니 조회 (사용자 대상 이벤트 조건 반영 + 아이템별 최대할인율 적용)
	private static final String SELECT_ALL_CART =
	    "WITH "
	  + "acct AS ( "
	  + "  SELECT "
	  + "    a.ACCOUNT_PK AS accountPk, "
	  + "    DATE(a.ACCOUNT_DATE) AS joinedDate, "
	  + "    a.ACCOUNT_ROLE AS accountRole, "
	  + "    IFNULL(SUM(oi.ORDERS_ITEM_PRICE * oi.ORDERS_ITEM_COUNT), 0) AS totalAmount "
	  + "  FROM account a "
	  + "  LEFT JOIN orders o ON o.ACCOUNT_PK = a.ACCOUNT_PK "
	  + "  LEFT JOIN orders_item oi ON oi.ORDERS_PK = o.ORDERS_PK "
	  + "  WHERE a.ACCOUNT_PK = ? "
	  + "  GROUP BY a.ACCOUNT_PK, DATE(a.ACCOUNT_DATE), a.ACCOUNT_ROLE "
	  + "), "
	  + "cart_items AS ( "
	  + "  SELECT "
	  + "    c.CART_PK AS cartPk, "
	  + "    c.ITEM_PK AS itemPk, "
	  + "    c.CART_COUNT AS cartCount "
	  + "  FROM cart c "
	  + "  WHERE c.ACCOUNT_PK = ? "
	  + "), "
	  + "event_max AS ( "
	  + "  SELECT "
	  + "    ci.itemPk AS itemPk, "
	  + "    MAX(IFNULL(e.EVENT_DISCOUNT_RATE, 0)) AS maxDiscountRate "
	  + "  FROM cart_items ci "
	  + "  JOIN item i ON i.ITEM_PK = ci.itemPk "
	  + "  JOIN event e "
	  + "    ON JSON_CONTAINS(e.EVENT_TARGET_CATEGORY, JSON_QUOTE(i.ITEM_CATEGORY)) "
	  + "   AND CURRENT_DATE BETWEEN e.EVENT_START_DATE AND e.EVENT_END_DATE "
	  + "  LEFT JOIN acct a ON 1 = 1 "
	  + "  WHERE ( "
	  + "    (a.accountPk IS NULL AND (e.EVENT_TARGET_ACCOUNT->>'$.type') = 'ALL') "
	  + "    OR "
	  + "    (a.accountPk IS NOT NULL AND ( "
	  + "         (e.EVENT_TARGET_ACCOUNT->>'$.type') = 'ALL' "
	  + "      OR ( "
	  + "         (e.EVENT_TARGET_ACCOUNT->>'$.type') = 'AMOUNT' "
	  + "         AND a.totalAmount >= CAST(e.EVENT_TARGET_ACCOUNT->>'$.amount' AS UNSIGNED) "
	  + "      ) "
	  + "      OR ( "
	  + "         (e.EVENT_TARGET_ACCOUNT->>'$.type') = 'JOINED' "
	  + "         AND a.joinedDate BETWEEN "
	  + "             STR_TO_DATE(e.EVENT_TARGET_ACCOUNT->>'$.startDate', '%Y-%m-%d') "
	  + "             AND STR_TO_DATE(e.EVENT_TARGET_ACCOUNT->>'$.endDate', '%Y-%m-%d') "
	  + "      ) "
	  + "      OR ( "
	  + "         (e.EVENT_TARGET_ACCOUNT->>'$.type') = 'MEMBER_TYPE' "
	  + "         AND JSON_CONTAINS( "
	  + "               JSON_EXTRACT(e.EVENT_TARGET_ACCOUNT, '$.memberType'), "
	  + "               JSON_QUOTE(a.accountRole) "
	  + "             ) "
	  + "      ) "
	  + "    )) "
	  + "  ) "
	  + "  GROUP BY ci.itemPk "
	  + ") "
	  + "SELECT "
	  + "  ci.cartPk AS cartPk, "
	  + "  i.ITEM_PK AS itemPk, "
	  + "  i.ITEM_IMAGE_URL AS itemImageUrl, "
	  + "  i.ITEM_NAME AS itemName, "
	  + "  i.ITEM_PRICE AS itemPrice, "
	  + "  IFNULL(em.maxDiscountRate, 0) AS itemDiscountRate, "
	  + "  CASE "
	  + "    WHEN IFNULL(em.maxDiscountRate, 0) > 0 "
	  + "      THEN ROUND(i.ITEM_PRICE * (1 - IFNULL(em.maxDiscountRate, 0) / 100), 0) "
	  + "    ELSE i.ITEM_PRICE "
	  + "  END AS itemDiscountPrice, "
	  + "  ci.cartCount AS cartCount "
	  + "FROM cart_items ci "
	  + "JOIN item i ON i.ITEM_PK = ci.itemPk "
	  + "LEFT JOIN event_max em ON em.itemPk = i.ITEM_PK "
	  + "ORDER BY ci.cartPk DESC ";

		    
	
	
	// 장바구니 상품 추가 (중복 시 수량 증가, 최대 99 제한)
	private static final String INSERT_CART_OR_UPDATE =
		    "INSERT INTO CART (ACCOUNT_PK, ITEM_PK, CART_COUNT) " +
		    "VALUES (?, ?, ?) " +
		    "ON DUPLICATE KEY UPDATE " +
		    "CART_COUNT = LEAST(CART_COUNT + VALUES(CART_COUNT), 99)";

	
	// 장바구니 상품 개수 직접 변경
	private static final String UPDATE_CART_ITEM_COUNT =
	    "UPDATE CART " +
	    "SET CART_COUNT = LEAST(?, 99) " +
	    "WHERE CART_PK = ? AND ACCOUNT_PK = ?";


	// 장바구니 상품 개수 증가
	private static final String ADD_CART_ITEM_COUNT =
	    "UPDATE CART " +
	    "SET CART_COUNT = LEAST(CART_COUNT + ?, 99) " +
	    "WHERE CART_PK = ? AND ACCOUNT_PK = ?";

	
	// 장바구니 상품 1개 삭제
	private static final String DELETE_CART_ITEM =
		    "DELETE FROM CART " +
		    "WHERE CART_PK = ? AND ACCOUNT_PK = ?";
	
	
	// 결제 완료 시 회원 장바구니 전체 삭제
	private static final String DELETE_CART_BY_ACCOUNT_PK =
		    "DELETE FROM CART " +
		    "WHERE ACCOUNT_PK = ?";
	

	
	
	public List<CartDTO> selectAll(CartDTO cartDTO){
		
		
		// 사용자의 장바구니 목록 조회
	    if ("SELECT_ALL_CART".equals(cartDTO.getCondition())) {
			
			return jdbcTemplate.query(
				SELECT_ALL_CART,
				new BeanPropertyRowMapper<>(CartDTO.class),
				cartDTO.getAccountPk(),
				cartDTO.getAccountPk()
			);
		}
		
		return null;
	}
	
	
	private CartDTO selectOne(CartDTO cartDTO) {
		return null;
	}
	
	
	public boolean insert(CartDTO cartDTO) {
		
		int result = 0;
		
		// 장바구니 추가 (이미 같은 상품 존재 시 개수만 증가)
		if ("INSERT_CART_OR_UPDATE".equals(cartDTO.getCondition())) {
			
			result = jdbcTemplate.update(
				INSERT_CART_OR_UPDATE, 
				cartDTO.getAccountPk(),
		        cartDTO.getItemPk(),
		        cartDTO.getCartCount()
		    );
		}
		else {
        	
        }
		return result > 0;
	}
	
	
	public boolean update(CartDTO cartDTO) {
	    
	    int result = 0;

	    // 장바구니 상품 개수 직접 변경
	    if ("UPDATE_CART_ITEM_COUNT".equals(cartDTO.getCondition())) {
			
	        result = jdbcTemplate.update(
	            UPDATE_CART_ITEM_COUNT,
	            cartDTO.getCartNewCount(),
	            cartDTO.getCartPk(),
	            cartDTO.getAccountPk()
	        );
	    }
	    
	    // 장바구니 상품 개수 증가
	    else if ("ADD_CART_ITEM_COUNT".equals(cartDTO.getCondition())) {
			
	        result = jdbcTemplate.update(
	            ADD_CART_ITEM_COUNT,
	            cartDTO.getCartCount(),
	            cartDTO.getCartPk(),
	            cartDTO.getAccountPk()
	        );
	    }
		else {
        	
        }
	    return result > 0;
	}
	
	public boolean delete(CartDTO cartDTO) {
		
		
		int result = 0;

		// 회원 장바구니 전체 삭제
		if ("DELETE_CART_BY_ACCOUNT_PK".equals(cartDTO.getCondition())) {
			
			result = jdbcTemplate.update(
				DELETE_CART_BY_ACCOUNT_PK,
				cartDTO.getAccountPk()
			);
		}
		
		// 장바구니 항목 1개만 삭제 : X 버튼 클릭 시
		else if ("DELETE_CART_ITEM".equals(cartDTO.getCondition())) {
			
			result = jdbcTemplate.update(
				DELETE_CART_ITEM,
				cartDTO.getCartPk(),
				cartDTO.getAccountPk()
			);
		}
		else {
        	
        }
		return result > 0;
	}
}

