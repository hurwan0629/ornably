package bugsandwich.ornably.item;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ItemRepository {
	@Autowired // 의존주입
	private JdbcTemplate jdbcTemplate;
	

	// 상품 목록 조회 (카테고리 + 검색 + 페이징 + 정렬)
	private static final String SELECT_ALL_ITEM =
	    "SELECT " +
	    "  i.ITEM_PK        AS itemPk, " +
	    "  i.ITEM_NAME      AS itemName, " +
	    "  i.ITEM_PRICE     AS itemPrice, " +
	    "  i.ITEM_IMAGE_URL AS itemImageUrl, " +
	    "  i.ITEM_CATEGORY  AS itemCategory, " +

	    // accountPk 기준 EVENT 중 최고 할인율
	    "  IFNULL(MAX(e.EVENT_DISCOUNT_RATE), 0) AS itemDiscountRate, " +

	    // 최고 할인율이 적용된 가격
	    "  CASE " +
	    "    WHEN MAX(e.EVENT_DISCOUNT_RATE) IS NOT NULL " +
	    "    THEN ROUND(i.ITEM_PRICE * (1 - MAX(e.EVENT_DISCOUNT_RATE) / 100), 0) " +
	    "    ELSE i.ITEM_PRICE " +
	    "  END AS itemDiscountedPrice, " +

	    "  IFNULL(ROUND(AVG(r.REVIEW_STAR), 2), 0) AS itemAvgStar " +
	    "FROM ITEM i " +
	    "LEFT JOIN REVIEW r ON i.ITEM_PK = r.ITEM_PK " +

	    // accountPk가 적용되는 EVENT만 JOIN
	    "LEFT JOIN EVENT e " +
	    "  ON e.ACCOUNT_PK = ? " +
	    " AND JSON_CONTAINS(e.EVENT_TARGET_CATEGORY, JSON_QUOTE(i.ITEM_CATEGORY)) " +
	    " AND CURRENT_DATE BETWEEN e.EVENT_START_DATE AND e.EVENT_END_DATE " +

	    "WHERE ( ? = 'all' OR i.ITEM_CATEGORY = ? ) " +
	    "  AND ( ? IS NULL OR ? = '' OR i.ITEM_NAME LIKE CONCAT('%', ?, '%') ) " +

	    "GROUP BY " +
	    "  i.ITEM_PK, i.ITEM_NAME, i.ITEM_PRICE, i.ITEM_IMAGE_URL, " +
	    "  i.ITEM_CATEGORY " +

	    // 정렬
	    "ORDER BY " +
	    "  CASE WHEN ? = 'popular' THEN itemAvgStar END DESC, " +       // 인기순
	    "  CASE WHEN ? = 'new-reverse' THEN i.ITEM_PK END ASC, " +      // 오래된순
	    "  CASE WHEN ? = 'discount' THEN itemDiscountRate END DESC, " +	// 할인율순
	    "  CASE WHEN ? = 'default' THEN i.ITEM_PK END DESC " +          // 기본
	    "LIMIT ? OFFSET ?";                                             // 페이징 처리

	

	// 회원 위시리스트 조회
	private static final String SELECT_ALL_WISHLIST_ITEM =
		    "SELECT " +
		    "  I.ITEM_PK          AS itemPk, " +
		    "  I.ITEM_NAME        AS itemName, " +
		    "  I.ITEM_PRICE       AS itemPrice, " +
		    "  I.ITEM_IMAGE_URL   AS itemImageUrl, " +
		    "  I.ITEM_CATEGORY    AS itemCategory, " +
		    "  I.ITEM_STOCK       AS itemStock, " +
		    "  I.ITEM_REGIST_DATE AS itemRegistDate " +
		    "FROM ITEM I " +
		    "INNER JOIN WISHLIST W ON I.ITEM_PK = W.ITEM_PK " +
		    "WHERE W.ACCOUNT_PK = ?";


	// 상품 상세 조회
	private static final String SELECT_ONE_ITEM_DETAIL =
	    "SELECT " +
	    "  i.ITEM_PK        AS itemPk, " +
	    "  i.ITEM_NAME      AS itemName, " +
	    "  i.ITEM_PRICE     AS itemPrice, " +
	    "  i.ITEM_REGIST_DATE AS itemRegistDate, " +
	    "  i.ITEM_IMAGE_URL AS itemImageUrl, " +
	    "  i.ITEM_CATEGORY  AS itemCategory, " +

	    // accountPk 기준 최고 할인율
	    "  IFNULL(MAX(e.EVENT_DISCOUNT_RATE), 0) AS itemDiscountRate, " +

	    // 최고 할인율 적용 가격
	    "  CASE " +
	    "    WHEN MAX(e.EVENT_DISCOUNT_RATE) IS NOT NULL " +
	    "    THEN ROUND(i.ITEM_PRICE * (1 - MAX(e.EVENT_DISCOUNT_RATE) / 100), 0) " +
	    "    ELSE i.ITEM_PRICE " +
	    "  END AS itemDiscountedPrice, " +

	    // 평균 별점
	    "  IFNULL(ROUND(AVG(r.REVIEW_STAR), 2), 0) AS itemAvgStar " +

	    "FROM ITEM i " +
	    "LEFT JOIN REVIEW r ON i.ITEM_PK = r.ITEM_PK " +

	    // accountPk가 적용되는 이벤트만
	    "LEFT JOIN EVENT e " +
	    "  ON e.ACCOUNT_PK = ? " +
	    " AND JSON_CONTAINS(e.EVENT_TARGET_CATEGORY, JSON_QUOTE(i.ITEM_CATEGORY)) " +
	    " AND CURRENT_DATE BETWEEN e.EVENT_START_DATE AND e.EVENT_END_DATE " +
	    "WHERE i.ITEM_PK = ? " +
	    "GROUP BY " +
	    "  i.ITEM_PK, i.ITEM_NAME, i.ITEM_PRICE, i.ITEM_REGIST_DATE, " +
	    "  i.ITEM_IMAGE_URL, i.ITEM_CATEGORY";



	// 상품 전체 개수 (카테고리 + 검색)
	private static final String TOTAL_ITEM_COUNT =
		    "SELECT COUNT(*) AS itemTotalCount " +
		    "FROM ITEM i " +
		    "WHERE ( ? = 'all' OR i.ITEM_CATEGORY = ? ) " +
		    "  AND ( ? IS NULL OR ? = '' OR i.ITEM_NAME LIKE CONCAT('%', ?, '%') )";


	// 장바구니 기준 재고 차감
	private static final String DECREASE_ITEM_STOCK_BY_CART =
	        "UPDATE ITEM I SET ITEM_STOCK = ITEM_STOCK - (" +
	        "    SELECT CART_COUNT FROM CART C WHERE C.ITEM_PK = I.ITEM_PK AND ACCOUNT_PK = ?) " +
	        "WHERE EXISTS ( SELECT 1 FROM CART C WHERE C.ITEM_PK = I.ITEM_PK AND ACCOUNT_PK = ?)";

	// 단일 상품 재고 감소
	private static final String BUY_ITEM = "UPDATE ITEM SET ITEM_STOCK = ITEM_STOCK - ? WHERE ITEM_PK = ?";

	// 단일 상품 재고 복귀
	private static final String ROLLBACK_ITEM_STOCK = "UPDATE ITEM SET ITEM_STOCK = ITEM_STOCK + ? WHERE ITEM_PK = ?";

	// 재고 확인 (구매 가능 여부)
	private static final String ITEM_STOCK_ENOUGH =	"SELECT ITEM_PK, ITEM_NAME FROM ITEM WHERE ITEM_PK = ? AND ITEM_STOCK >= ?";

	// 찜 여부
    private static final String SELECT_WISHLIST_TOGGLE = 
		    "SELECT EXISTS( " +
		    "    SELECT 1 FROM WISHLIST WHERE ACCOUNT_PK = ? AND ITEM_PK = ? " + // SELECT 1 : 결과 있다1, 없다0
		    ") AS itemWishlistToggle";
	
    
    
    
    // ==============
 	//   관리자 쿼리문
 	// ==============
 	
 	// 상품 검색 페이지
 	private static final String ADMIN_SEARCH_ITEM =
		"SELECT " +
		"    i.ITEM_PK        AS itemPk, " +
		"    i.ITEM_NAME      AS itemName, " +
		"    i.ITEM_PRICE     AS itemPrice, " +
		"    i.ITEM_IMAGE_URL AS itemImageUrl, " +
		"    i.ITEM_REGIST_DATE AS itemRegistDate " +
		"FROM ITEM i " +
		"WHERE " +
		"    ( ? IS NULL OR i.ITEM_PK = ? ) " +                 			// itemPk 검색
		"    AND ( ? IS NULL OR i.ITEM_NAME LIKE CONCAT('%', ?, '%')) " +	// itemName 검색
		"    AND ( ? = 'all' OR i.ITEM_CATEGORY = ? ) " +       // itemCategory 검색
		"    AND ( ? IS NULL OR i.ITEM_PRICE >= ? ) " +         // itemPriceMin
		"    AND ( ? IS NULL OR i.ITEM_PRICE <= ? ) " +         // itemPriceMax
		"    AND ( ? IS NULL OR i.ITEM_REGIST_DATE >= ? ) " +   // itemRegistDateStart
		"    AND ( ? IS NULL OR i.ITEM_REGIST_DATE <= ? ) " +   // itemRegistDateEnd
		"ORDER BY i.ITEM_PK DESC";                              // 기본 정렬: 최근 등록 순
    
 	// 상품 삭제
 	private static final String ADMIN_DELETE_ITEM = 
 		"DELETE FROM ITEM WHERE ITEM_PK = ?";
 	
 	// 상품 등록
 	private static final String ADMIN_INSERT_ITEM =
 		    "INSERT INTO ITEM (" +
 		    "ITEM_NAME, ITEM_PRICE, ITEM_STOCK, ITEM_IMAGE_URL, ITEM_DESCRIPTION, ITEM_CATEGORY, ITEM_REGIST_DATE" +
 		    ") VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE)";

 	// 상품 상세 보기
 	private static final String ADMIN_SELECT_ONE_ITEM =
		"SELECT " +
         "i.ITEM_PK 		AS itemPk, " +
         "i.ITEM_NAME 		AS itemName, " +
         "i.ITEM_PRICE 		AS itemPrice, " +
         "i.ITEM_CATEGORY 	AS itemCategory, " +
         "i.ITEM_IMAGE_URL 	AS itemImageUrl, " +
         "i.ITEM_STOCK AS itemStock, " +
         "i.ITEM_REGIST_DATE AS itemRegistDate, " +
         
         // 해당 상품의 총 판매량 / 리뷰 수 / 위시리스트 추가 수
         "IFNULL(SUM(oi.ORDERS_ITEM_COUNT), 0) AS itemSoldCount, " +
         "IFNULL(COUNT(DISTINCT r.REVIEW_PK), 0) AS itemReviewCount, " +
         "IFNULL(COUNT(DISTINCT w.WISHLIST_PK), 0) AS itemWishlistCount " +
         "FROM ITEM i " +
         "LEFT JOIN ORDERS_ITEM oi ON i.ITEM_PK = oi.ITEM_PK " +
         "LEFT JOIN REVIEW r ON i.ITEM_PK = r.ITEM_PK " +
         "LEFT JOIN WISHLIST w ON i.ITEM_PK = w.ITEM_PK " +
         "WHERE i.ITEM_PK = ? " +
         "GROUP BY i.ITEM_PK, i.ITEM_NAME, i.ITEM_PRICE, i.ITEM_CATEGORY, i.ITEM_IMAGE_URL, i.ITEM_STOCK, i.ITEM_REGIST_DATE";

 	// 상품 이름 수정
 	private static final String ADMIN_UPDATE_NAME_ITEM =
 		"UPDATE ITEM SET ITEM_NAME = ? WHERE ITEM_PK = ?";
 	
 	// 상품 가격 수정
 	private static final String ADMIN_UPDATE_PRICE_ITEM =
 		"UPDATE ITEM SET ITEM_PRICE = ? WHERE ITEM_PK = ?";
 	
 	// 상품 재고 수정
 	private static final String ADMIN_UPDATE_STOCK_ITEM =
 		"UPDATE ITEM SET ITEM_STOCK = ? WHERE ITEM_PK = ?";
 	
 	// 상품 설명 수정
 	private static final String ADMIN_UPDATE_DESCRIPTION_ITEM =
 		"UPDATE ITEM SET ITEM_DESCRIPTION = ? WHERE ITEM_PK = ?";
 	
 	// 상품 이미지 수정
 	private static final String ADMIN_UPDATE_IMAGE_ITEM =
 		"UPDATE ITEM SET ITEM_IMAGE_URL = ? WHERE ITEM_PK = ?";
 	
 	
 	
 	
 	
 	
	public List<ItemDTO> selectAll(ItemDTO itemDTO) {
		System.out.println("[로그] ItemRepository의 selectAll 시작");
		
		// 상품 전체 보기 (pk 순으로)
		if ("SELECT_ALL_ITEM".equals(itemDTO.getCondition())) {
			System.out.println("[로그] ItemRepository의 SELECT_ALL_ITEM");
			return jdbcTemplate.query(
			        SELECT_ALL_ITEM,
			        new BeanPropertyRowMapper<>(ItemDTO.class),

			        itemDTO.getAccountPk(),
			        
			        // category
			        itemDTO.getCategory(),
			        itemDTO.getCategory(),

			        // search
			        itemDTO.getSearch(),
			        itemDTO.getSearch(),
			        itemDTO.getSearch(),
			        
			        // sort 
			        itemDTO.getSort(),
			        itemDTO.getSort(),
			        itemDTO.getSort(),
			        itemDTO.getSort(),
			        
			        // paging
			        itemDTO.getItemLimit(),   // LIMIT
			        itemDTO.getItemOffset()   // OFFSET
			    );
		}
		
		// 위시리스트 상품 조회
		else if ("SELECT_ALL_WISHLIST_ITEM".equals(itemDTO.getCondition())) {
			System.out.println("[로그] ItemRepository의 SELECT_ALL_WISHLIST_ITEM");
			return jdbcTemplate.query(
				SELECT_ALL_WISHLIST_ITEM,
				new BeanPropertyRowMapper<>(ItemDTO.class),
				itemDTO.getAccountPk()
			);
		}
		
		// 관리자 상품 검색
		else if("ADMIN_SEARCH_ITEM".equals(itemDTO.getCondition())) {
			System.out.println("[로그] ItemRepository의 ADMIN_SEARCH_ITEM");
			return jdbcTemplate.query(
		        ADMIN_SEARCH_ITEM,
		        new BeanPropertyRowMapper<>(ItemDTO.class),
		        
		        itemDTO.getItemPk(), itemDTO.getItemPk(),
		        itemDTO.getItemName(), itemDTO.getItemName(),
		        itemDTO.getCategory(), itemDTO.getCategory(),
		        itemDTO.getItemPriceMin(), itemDTO.getItemPriceMin(),
		        itemDTO.getItemPriceMax(), itemDTO.getItemPriceMax(),
		        itemDTO.getItemRegistDateStart(), itemDTO.getItemRegistDateStart(),
		        itemDTO.getItemRegistDateEnd(), itemDTO.getItemRegistDateEnd()
		    );
		}
		System.out.println("[로그][경고] ItemRepository_selectAll_condition 없음");
		return null;
	}
	
	public ItemDTO selectOne(ItemDTO itemDTO) {
	    System.out.println("[로그] ItemRepository의 selectOne 시작");
	    // 재고 체크
	    if ("ITEM_STOCK_ENOUGH".equals(itemDTO.getCondition())) {
			System.out.println("[로그] selectOne의 ITEM_STOCK_ENOUGH");			
	    	return jdbcTemplate.queryForObject(
                ITEM_STOCK_ENOUGH,
                (rs, rowNum) -> {
                    ItemDTO data = new ItemDTO();
                    data.setItemPk(rs.getInt("ITEM_PK"));
                    data.setItemName(rs.getString("ITEM_NAME"));
                    return data;
                },
                itemDTO.getItemPk(), 
                itemDTO.getItemStock()
            );
	    } 
	    
	    // 전체 상품 개수
	    else if ("TOTAL_ITEM_COUNT".equals(itemDTO.getCondition())) {
			System.out.println("[로그] selectOne의 TOTAL_ITEM_COUNT");			
	        return jdbcTemplate.queryForObject(
	            TOTAL_ITEM_COUNT,
	            (rs, rowNum) -> {
	                ItemDTO data = new ItemDTO();
	                data.setItemTotalCount(rs.getInt("itemTotalCount"));
	                return data;
	            },
	            // category
	            itemDTO.getCategory(),
	            itemDTO.getCategory(),

	            // search
	            itemDTO.getSearch(),
	            itemDTO.getSearch(),
	            itemDTO.getSearch()
	        );
	    }	  
	    
        // 상품 상세 보기
	    else if ("SELECT_ONE_ITEM_DETAIL".equals(itemDTO.getCondition())) {
	    	System.out.println("[로그] selectOne의 SELECT_ONE_ITEM");	    	
	    	return jdbcTemplate.queryForObject(
	    		SELECT_ONE_ITEM_DETAIL,
                new BeanPropertyRowMapper<>(ItemDTO.class),
                itemDTO.getAccountPk(),
                itemDTO.getItemPk()
            );
	    }
	    
	    // 찜 여부 
	    else if("SELECT_WISHLIST_TOGGLE".equals(itemDTO.getCondition())) {
			System.out.println("[로그] selectOne의 SELECT_WISHLIST_TOGGLE");			
			return jdbcTemplate.queryForObject(
			    SELECT_WISHLIST_TOGGLE,
			    (rs, rowNum) -> {
			        ItemDTO data = new ItemDTO();
			        data.setItemWishlistToggle(rs.getBoolean("itemWishlistToggle"));
			        return data;
			    },
			    itemDTO.getAccountPk(),
			    itemDTO.getItemPk()
			);
		}
	    
	    // 상품 상세 보기
	    else if("ADMIN_SELECT_ONE_ITEM".equals(itemDTO.getCondition())) {
	    	System.out.println("[로그] selectOne의 ADMIN_SELECT_ONE_ITEM");	    	
	    	return jdbcTemplate.queryForObject(
	    		ADMIN_SELECT_ONE_ITEM,
	    		new BeanPropertyRowMapper<>(ItemDTO.class),
	    		itemDTO.getItemPk()
	    	);
	    }
	    
	    System.out.println("[로그][경고] ItemRepository_selectOne_condition 없음");
	    return null;
	}
	
	
	public boolean insert(ItemDTO itemDTO) {
	    System.out.println("[로그] ItemRepository의 insert 시작");
	    int result = 0;
	    
	    // 관리자용 : 상품 등록
		if("ADMIN_INSERT_ITEM".equals(itemDTO.getCondition())) {
			System.out.println("[로그] insert의 ADMIN_INSERT_ITEM");
			result = jdbcTemplate.update(
				ADMIN_INSERT_ITEM,
				itemDTO.getItemName(),
				itemDTO.getItemPrice(),
				itemDTO.getItemStock(),
				itemDTO.getItemImageUrl(),
				itemDTO.getItemDescription(),
				itemDTO.getItemCategory()
			);
		}		
		else {
			System.out.println("[로그][경고] ItemRepository_insert_condition 없음");
		}
		return result > 0;
	}
	

	public boolean update(ItemDTO itemDTO) {
	    System.out.println("[로그] ItemRepository의 update 시작");
	    int result = 0;
	    
	    // 상품 구매
	    if ("BUY_ITEM".equals(itemDTO.getCondition())) {
	    	System.out.println("[로그] update의 BUY_ITEM");
	        result = jdbcTemplate.update(BUY_ITEM, itemDTO.getItemStock(), itemDTO.getItemPk());
	    } 
	    
	    // 장바구니에 담긴 수량만큼 재고 차감
	    else if ("DECREASE_ITEM_STOCK_BY_CART".equals(itemDTO.getCondition())) {
	    	System.out.println("[로그] update의 DECREASE_ITEM_STOCK_BY_CART");
	        result = jdbcTemplate.update(DECREASE_ITEM_STOCK_BY_CART, itemDTO.getAccountPk(), itemDTO.getAccountPk());
	    }
	    
	    // 상품 재고 복구
	    else if ("ROLLBACK_ITEM_STOCK".equals(itemDTO.getCondition())) {
	    	System.out.println("[로그] update의 ROLLBACK_ITEM_STOCK");
	        result = jdbcTemplate.update(ROLLBACK_ITEM_STOCK, itemDTO.getItemStock(), itemDTO.getItemPk());
	    }
	    
	    // 관리자용 : 상품 이름 수정
	    else if("ADMIN_UPDATE_NAME_ITEM".equals(itemDTO.getCondition())) {
	    	System.out.println("[로그] update의 ADMIN_UPDATE_NAME_ITEM");
	    	result = jdbcTemplate.update(ADMIN_UPDATE_NAME_ITEM, itemDTO.getItemName(), itemDTO.getItemPk());
	    }
	    
	    // 관리자용 : 상품 가격 수정
	    else if("ADMIN_UPDATE_PRICE_ITEM".equals(itemDTO.getCondition())) {
	    	System.out.println("[로그] update의 ADMIN_UPDATE_PRICE_ITEM");
	    	result = jdbcTemplate.update(ADMIN_UPDATE_PRICE_ITEM, itemDTO.getItemPrice(), itemDTO.getItemPk());
	    }
	    
	    // 관리자용 : 상품 재고 수정
	    else if("ADMIN_UPDATE_STOCK_ITEM".equals(itemDTO.getCondition())) {
	    	System.out.println("[로그] update의 ADMIN_UPDATE_STOCK_ITEM");
	    	result = jdbcTemplate.update(ADMIN_UPDATE_STOCK_ITEM, itemDTO.getItemStock(), itemDTO.getItemPk());
	    }
	    
	    // 관리자용 : 상품 설명 수정
	    else if("ADMIN_UPDATE_DESCRIPTION_ITEM".equals(itemDTO.getCondition())) {
	    	System.out.println("[로그] update의 ADMIN_UPDATE_DESCRIPTION_ITEM");
	    	result = jdbcTemplate.update(ADMIN_UPDATE_DESCRIPTION_ITEM, itemDTO.getItemDescription(), itemDTO.getItemPk());
	    }
	    
	    // 관리자용 : 상품 이미지 수정 
	    else if("ADMIN_UPDATE_IMAGE_ITEM".equals(itemDTO.getCondition())) {
	    	System.out.println("[로그] update의 ADMIN_UPDATE_IMAGE_ITEM");
	    	result = jdbcTemplate.update(ADMIN_UPDATE_IMAGE_ITEM, itemDTO.getItemImageUrl(), itemDTO.getItemPk());
	    }
	    
	    else {
		    System.out.println("[로그][경고] ItemRepository_update_condition 없음");
	    }
	    return result > 0;
	}
	

	public boolean delete(ItemDTO itemDTO) {
		System.out.println("[로그] ItemRepository의 delete 시작");
	    int result = 0;
	    
		// 관리자용 상품 삭제
		if("ADMIN_DELETE_ITEM".equals(itemDTO.getCondition())) {
	    	System.out.println("[로그] delete의 ADMIN_DELETE_ITEM");
			result = jdbcTemplate.update(
				ADMIN_DELETE_ITEM,
				itemDTO.getItemPk()
			);
		}
		else {
			System.out.println("[로그][경고] ItemRepository_delete_condition 없음");
		}
		return result > 0;
	}
}
