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
	private static final String SELECT_ALL_CART =
		    "SELECT " +
		    "  C.CART_PK           AS cartPk, " +
		    "  I.ITEM_PK           AS itemPk, " +
		    "  I.ITEM_IMAGE_URL    AS itemImageUrl, " +
		    "  I.ITEM_NAME         AS itemName, " +
		    "  I.ITEM_PRICE        AS itemPrice, " +
		    "  IFNULL(MAX(E.EVENT_DISCOUNT_RATE), 0) AS itemDiscountRate, " +
		    "  CASE " +
		    "    WHEN IFNULL(MAX(E.EVENT_DISCOUNT_RATE), 0) > 0 " +
		    "    THEN ROUND(I.ITEM_PRICE * (IFNULL(MAX(E.EVENT_DISCOUNT_RATE), 0) / 100), 0) " +
		    "    ELSE 0 " +
		    "  END AS itemDiscountPrice, " +
		    "  C.CART_COUNT        AS cartCount " +
		    "FROM CART C " +
		    "INNER JOIN ITEM I ON C.ITEM_PK = I.ITEM_PK " +
		    "LEFT JOIN EVENT E " +
		    "  ON JSON_CONTAINS(E.EVENT_TARGET_CATEGORY, JSON_QUOTE(I.ITEM_CATEGORY)) " +
		    "  AND CURRENT_DATE BETWEEN E.EVENT_START_DATE AND E.EVENT_END_DATE " +
		    "WHERE C.ACCOUNT_PK = ? " +
		    "GROUP BY C.CART_PK, I.ITEM_PK, I.ITEM_IMAGE_URL, I.ITEM_NAME, I.ITEM_PRICE, C.CART_COUNT " +
		    "ORDER BY C.CART_PK ASC";
	
	
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
		System.out.println("[로그] CartRepository의 selectAll 시작");
		
		// 사용자의 장바구니 목록 조회
	    if ("SELECT_ALL_CART".equals(cartDTO.getCondition())) {
			System.out.println("[로그] selectAll의 SELECT_ALL_ACCOUNT_CART");
			return jdbcTemplate.query(
				SELECT_ALL_CART,
				new BeanPropertyRowMapper<>(CartDTO.class),
				cartDTO.getAccountPk()
			);
		}
		System.out.println("[로그][경고] CartRepository의 selectAll_condition 없음");
		// 조건이 없으면 빈 리스트 반환
	    return java.util.Collections.emptyList();
	}
	
	
	private CartDTO selectOne(CartDTO cartDTO) {
		return null;
	}
	
	
	public boolean insert(CartDTO cartDTO) {
		System.out.println("[로그] CartRepository의 insert 시작");
		int result = 0;
		
		// 장바구니 추가 (이미 같은 상품 존재 시 개수만 증가)
		if ("INSERT_CART_OR_UPDATE".equals(cartDTO.getCondition())) {
			System.out.println("[로그] insert의 ADD_CART_ITEM");
			result = jdbcTemplate.update(
				INSERT_CART_OR_UPDATE, 
				cartDTO.getAccountPk(),
		        cartDTO.getItemPk(),
		        cartDTO.getCartCount()
		    );
		}
		else {
        	System.out.println("[로그][경고] CartRepository_insert_condition 없음");
        }
		return result > 0;
	}
	
	
	public boolean update(CartDTO cartDTO) {
	    System.out.println("[로그] CartRepository의 update 시작");
	    int result = 0;

	    // 장바구니 상품 개수 직접 변경
	    if ("UPDATE_CART_ITEM_COUNT".equals(cartDTO.getCondition())) {
			System.out.println("[로그] update의 UPDATE_CART_ITEM_COUNT");
	        result = jdbcTemplate.update(
	            UPDATE_CART_ITEM_COUNT,
	            cartDTO.getCartNewCount(),
	            cartDTO.getCartPk(),
	            cartDTO.getAccountPk()
	        );
	    }
	    
	    // 장바구니 상품 개수 증가
	    else if ("ADD_CART_ITEM_COUNT".equals(cartDTO.getCondition())) {
			System.out.println("[로그] update의 ADD_CART_ITEM_COUNT");
	        result = jdbcTemplate.update(
	            ADD_CART_ITEM_COUNT,
	            cartDTO.getCartCount(),
	            cartDTO.getCartPk(),
	            cartDTO.getAccountPk()
	        );
	    }
		else {
        	System.out.println("[로그][경고] CartRepository_update_condition 없음");
        }
	    return result > 0;
	}
	
	public boolean delete(CartDTO cartDTO) {
		System.out.println("[로그] CartRepository의 delete 시작");
		System.out.println("[로그] cartPk=" + cartDTO.getCartPk() + ", accountPk=" + cartDTO.getAccountPk());
		int result = 0;

		// 회원 장바구니 전체 삭제
		if ("DELETE_CART_BY_ACCOUNT_PK".equals(cartDTO.getCondition())) {
			System.out.println("[로그] delete의 DELETE_CART_BY_ACCOUNT_PK");
			result = jdbcTemplate.update(
				DELETE_CART_BY_ACCOUNT_PK,
				cartDTO.getAccountPk()
			);
		}
		
		// 장바구니 항목 1개만 삭제 : X 버튼 클릭 시
		else if ("DELETE_BY_CART_PK".equals(cartDTO.getCondition())) {
			System.out.println("[로그] delete의 DELETE_BY_CART_PK");
			result = jdbcTemplate.update(
				DELETE_CART_ITEM,
				cartDTO.getCartPk(),
				cartDTO.getAccountPk()
			);
		}
		else {
        	System.out.println("[로그][경고] CartRepository_delete_condition 없음");
        }
		return result > 0;
	}
}

