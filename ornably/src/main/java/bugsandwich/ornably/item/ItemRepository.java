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
	// 상품 목록 조회 (이벤트 최대할인율 + 리뷰 평균별점 + 카테고리/검색/정렬/페이징)
	private static final String SELECT_ALL_ITEM =
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
	  + "event_max AS ( "
	  + "  SELECT "
	  + "    i.ITEM_PK AS itemPk, "
	  + "    MAX(IFNULL(e.EVENT_DISCOUNT_RATE, 0)) AS maxDiscountRate "
	  + "  FROM item i "
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
	  + "  GROUP BY i.ITEM_PK "
	  + "), "
	  + "review_avg AS ( "
	  + "  SELECT "
	  + "    r.ITEM_PK AS itemPk, "
	  + "    IFNULL(ROUND(AVG(r.REVIEW_STAR), 2), 0) AS itemAvgStar "
	  + "  FROM review r "
	  + "  GROUP BY r.ITEM_PK "
	  + ") "
	  + "SELECT "
	  + "  i.ITEM_PK AS itemPk, "
	  + "  i.ITEM_NAME AS itemName, "
	  + "  i.ITEM_PRICE AS itemPrice, "
	  + "  i.ITEM_IMAGE_URL AS itemImageUrl, "
	  + "  i.ITEM_CATEGORY AS itemCategory, "
	  + "  IFNULL(em.maxDiscountRate, 0) AS itemDiscountRate, "
	  + "  CASE "
	  + "    WHEN IFNULL(em.maxDiscountRate, 0) > 0 "
	  + "      THEN ROUND(i.ITEM_PRICE * (1 - IFNULL(em.maxDiscountRate, 0) / 100), 0) "
	  + "    ELSE i.ITEM_PRICE "
	  + "  END AS itemDiscountPrice, "
	  + "  IFNULL(ra.itemAvgStar, 0) AS itemAvgStar "
	  + "FROM item i "
	  + "LEFT JOIN event_max em ON em.itemPk = i.ITEM_PK "
	  + "LEFT JOIN review_avg ra ON ra.itemPk = i.ITEM_PK "
	  + "WHERE "
	  + "  ( ? = 'ALL' OR i.ITEM_CATEGORY = ? ) "
	  + "  AND ( ? IS NULL OR ? = '' OR i.ITEM_NAME LIKE CONCAT('%', ?, '%') ) "
	  + "ORDER BY "
	  + "  CASE WHEN ? = 'popular' THEN IFNULL(ra.itemAvgStar, 0) END DESC, "
	  + "  CASE WHEN ? = 'discount' THEN IFNULL(em.maxDiscountRate, 0) END DESC, "
	  + "  CASE WHEN ? = 'new-reverse' THEN i.ITEM_PK END ASC, "
	  + "  CASE WHEN ? = 'default' THEN i.ITEM_PK END DESC, "
	  + "  i.ITEM_PK DESC "
	  + "LIMIT ? OFFSET ? ";

			
/*
	private static final String SELECT_ALL_ITEM =

	    // 1. 회원 정보 + 누적 구매 금액 계산
	    "WITH acct AS ( " +
	    "  SELECT " +
	    "    a.ACCOUNT_PK AS accountPk, " +                  // 회원 PK
	    "    DATE(a.ACCOUNT_DATE) AS joinedDate, " +         // 가입일
	    "    a.ACCOUNT_ROLE AS accountRole, " +              // 회원 등급
	    "    IFNULL(SUM(oi.ORDERS_ITEM_PRICE * oi.ORDERS_ITEM_COUNT), 0) AS totalAmount " + // 총 구매 금액
	    "  FROM ACCOUNT a " +
	    "  LEFT JOIN ORDERS o ON o.ACCOUNT_PK = a.ACCOUNT_PK " +
	    "  LEFT JOIN ORDERS_ITEM oi ON oi.ORDERS_PK = o.ORDERS_PK " +
	    "  WHERE a.ACCOUNT_PK = ? " +                        // 조회 대상 회원
	    "  GROUP BY a.ACCOUNT_PK, DATE(a.ACCOUNT_DATE), a.ACCOUNT_ROLE " +
	    "), " +

	    // 2️. 상품별 최대 이벤트 할인율 계산
	    "event_max AS ( " +
	    "  SELECT " +
	    "    i.ITEM_PK AS itemPk, " +
	    "    MAX(IFNULL(e.EVENT_DISCOUNT_RATE, 0)) AS maxDiscountRate " +
	    "  FROM ITEM i " +
	    "  JOIN EVENT e " +
	    "    ON JSON_CONTAINS(e.EVENT_TARGET_CATEGORY, JSON_QUOTE(i.ITEM_CATEGORY)) " + // 이벤트 대상 카테고리 매칭
	    "   AND CURRENT_DATE BETWEEN e.EVENT_START_DATE AND e.EVENT_END_DATE " +        // 이벤트 기간 체크
	    "  LEFT JOIN acct a ON 1 = 1 " +

	    "  WHERE ( " +
	    // 비회원 or 전체 이벤트
	    "    (a.accountPk IS NULL AND (e.EVENT_TARGET_ACCOUNT->>'$.type') = 'ALL') " +

	    "    OR ( " +
	    "      a.accountPk IS NOT NULL AND ( " +

	    // 전체 회원 대상 이벤트
	    "        (e.EVENT_TARGET_ACCOUNT->>'$.type') = 'ALL' " +

	    // 구매 금액 기준 이벤트
	    "        OR ( " +
	    "          (e.EVENT_TARGET_ACCOUNT->>'$.type') = 'AMOUNT' " +
	    "          AND a.totalAmount >= CAST(e.EVENT_TARGET_ACCOUNT->>'$.amount' AS UNSIGNED) " +
	    "        ) " +

	    // 가입 기간 기준 이벤트
	    "        OR ( " +
	    "          (e.EVENT_TARGET_ACCOUNT->>'$.type') = 'JOINED' " +
	    "          AND a.joinedDate BETWEEN " +
	    "              STR_TO_DATE(e.EVENT_TARGET_ACCOUNT->>'$.startDate', '%Y-%m-%d') " +
	    "              AND STR_TO_DATE(e.EVENT_TARGET_ACCOUNT->>'$.endDate', '%Y-%m-%d') " +
	    "        ) " +

	    // 회원 등급 기준 이벤트
	    "        OR ( " +
	    "          (e.EVENT_TARGET_ACCOUNT->>'$.type') = 'MEMBER_TYPE' " +
	    "          AND JSON_CONTAINS( " +
	    "                JSON_EXTRACT(e.EVENT_TARGET_ACCOUNT, '$.memberType'), " +
	    "                JSON_QUOTE(a.accountRole) " +
	    "              ) " +
	    "        ) " +

	    "      ) " +
	    "    ) " +
	    "  ) " +
	    "  GROUP BY i.ITEM_PK " +
	    "), " +

	    // 3️. 상품별 리뷰 평균 평점 계산
	    "review_avg AS ( " +
	    "  SELECT " +
	    "    r.ITEM_PK AS itemPk, " +
	    "    IFNULL(ROUND(AVG(r.REVIEW_STAR), 2), 0) AS itemAvgStar " +
	    "  FROM REVIEW r " +
	    "  GROUP BY r.ITEM_PK " +
	    ") " +

	    // 4️. 최종 상품 조회
	    "SELECT " +
	    "  i.ITEM_PK AS itemPk, " +
	    "  i.ITEM_NAME AS itemName, " +
	    "  i.ITEM_PRICE AS itemPrice, " +
	    "  i.ITEM_IMAGE_URL AS itemImageUrl, " +
	    "  i.ITEM_CATEGORY AS itemCategory, " +
	    "  IFNULL(em.maxDiscountRate, 0) AS itemDiscountRate, " +

	    // 할인 적용 가격 계산
	    "  CASE " +
	    "    WHEN IFNULL(em.maxDiscountRate, 0) > 0 " +
	    "      THEN ROUND(i.ITEM_PRICE * (1 - IFNULL(em.maxDiscountRate, 0) / 100), 0) " +
	    "    ELSE i.ITEM_PRICE " +
	    "  END AS itemDiscountPrice, " +

	    "  IFNULL(ra.itemAvgStar, 0) AS itemAvgStar " +

	    "FROM ITEM i " +
	    "LEFT JOIN event_max em ON em.itemPk = i.ITEM_PK " +
	    "LEFT JOIN review_avg ra ON ra.itemPk = i.ITEM_PK " +

	    // 5️. 필터 조건
	    "WHERE ( ? = 'ALL' OR i.ITEM_CATEGORY = ? ) " +  // 카테고리 필터
	    "  AND ( ? IS NULL OR ? = '' OR i.ITEM_NAME LIKE CONCAT('%', ?, '%') ) " + // 검색어 필터

	    // 6️⃣ 정렬 조건
	    "ORDER BY " +
	    "  CASE WHEN ? = 'popular' THEN IFNULL(ra.itemAvgStar, 0) END DESC, " +
	    "  CASE WHEN ? = 'discount' THEN IFNULL(em.maxDiscountRate, 0) END DESC, " +
	    "  CASE WHEN ? = 'new-reverse' THEN i.ITEM_PK END ASC, " +
	    "  CASE WHEN ? = 'default' THEN i.ITEM_PK END DESC, " +
	    "  i.ITEM_PK DESC " +

	    // 7️. 페이징
	    "LIMIT ? OFFSET ? ";
 	*/
	

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

	
	// 상품 존재 여부 확인
	private static final String SELECT_ONE_CHECK_ITEM_EXISTS =
			"SELECT ITEM_PK AS itemPk " +
			"FROM ITEM WHERE ITEM_PK = ?";
	

	// 상품 상세 보기(사용자용)
	// 사용자 화면 기준 : 상품 기본 정보, 할인율, 할인가, 평균 별점, 내 위시 여부 
	private static final String SELECT_ONE_ITEM_DETAIL =
	        // CTE 사용: 로그인 사용자 ACCOUNT 정보 미리 계산
	        "WITH acct AS ( " +
	        "  SELECT " +
	        "    a.ACCOUNT_PK AS accountPk, " +                   // 회원 PK
	        "    DATE(a.ACCOUNT_DATE) AS joinedDate, " +         // 가입일
	        "    a.ACCOUNT_ROLE AS accountRole, " +              // 회원 등급
	        "    IFNULL(SUM(oi.ORDERS_ITEM_PRICE * oi.ORDERS_ITEM_COUNT), 0) AS totalAmount " + // 총 구매 금액
	        "  FROM ACCOUNT a " +
	        "  LEFT JOIN ORDERS o ON o.ACCOUNT_PK = a.ACCOUNT_PK " +
	        "  LEFT JOIN ORDERS_ITEM oi ON oi.ORDERS_PK = o.ORDERS_PK " +
	        "  WHERE a.ACCOUNT_PK = ? " +                        // 조회 대상 회원 PK (파라미터)
	        "  GROUP BY a.ACCOUNT_PK, DATE(a.ACCOUNT_DATE), a.ACCOUNT_ROLE " +
	        "), " +

	        // ITEM 기본 정보 조회
	        "item_base AS ( " +
	        "  SELECT " +
	        "    i.ITEM_PK AS itemPk, " +                       // 상품 PK
	        "    i.ITEM_NAME AS itemName, " +                   // 상품 이름
	        "    i.ITEM_PRICE AS itemPrice, " +                 // 상품 원가
	        "    DATE_FORMAT(i.ITEM_REGIST_DATE, '%Y-%m-%d') AS itemRegistDate, " + // 등록일
	        "    i.ITEM_IMAGE_URL AS itemImageUrl, " +          // 상품 이미지 URL
	        "    i.ITEM_CATEGORY AS itemCategory, " +            // 상품 카테고리
	        "    i.ITEM_DESCRIPTION AS itemDescription " +         // 상품 설명
	        "  FROM ITEM i " +
	        "  WHERE i.ITEM_PK = ? " +                          // 특정 상품 조회 (파라미터)
	        ") " +

	        // 최종 SELECT
	        "SELECT " +
	        "  ib.itemPk, " +
	        "  ib.itemName, " +
	        "  ib.itemPrice, " +
	        "  ib.itemRegistDate, " +
	        "  ib.itemImageUrl, " +
	        "  ib.itemCategory, " +
	        "  ib.itemDescription, " +

	        // 이벤트 할인율 (현재 적용 가능한 이벤트 중 최대 할인율)
	        "  IFNULL(MAX(e.EVENT_DISCOUNT_RATE), 0) AS itemDiscountRate, " +

	        // 할인 적용 가격 (최대 할인율 기준)
	        "  CASE " +
	        "    WHEN MAX(e.EVENT_DISCOUNT_RATE) IS NOT NULL " +
	        "    THEN ROUND(ib.itemPrice * (1 - MAX(e.EVENT_DISCOUNT_RATE)/100), 0) " +
	        "    ELSE ib.itemPrice " +
	        "  END AS itemDiscountPrice, " +

	        // 리뷰 평균 별점 (리뷰 없으면 0)
	        "  IFNULL(ROUND(AVG(r.REVIEW_STAR), 2), 0) AS itemAvgStar, " +

	        // 위시리스트 등록 여부 (로그인 사용자 기준)
	        "  CASE WHEN w.WISHLIST_PK IS NULL THEN FALSE ELSE TRUE END AS itemWishlistToggle " +

	        "FROM item_base ib " +

	        // 리뷰 LEFT JOIN: 리뷰가 없어도 상품 출력
	        "LEFT JOIN REVIEW r ON ib.itemPk = r.ITEM_PK " +
	        
	        // ACCOUNT JOIN: 이벤트 조건 계산용
	        "LEFT JOIN acct a ON 1=1 " +  // CTE 사용: a 컬럼 안전하게 참조 가능

	        // 이벤트 LEFT JOIN
	        "LEFT JOIN EVENT e " +
	        "  ON JSON_CONTAINS(e.EVENT_TARGET_CATEGORY, JSON_QUOTE(ib.itemCategory)) " +
	        " AND CURRENT_DATE BETWEEN e.EVENT_START_DATE AND e.EVENT_END_DATE " +

	        // 이벤트 대상 조건
	        " AND ( " +
	        "       ( ? IS NULL AND e.EVENT_TARGET_ACCOUNT->>'$.type' = 'ALL' ) " + // 비로그인: ALL 타입 이벤트
	        "    OR ( ? IS NOT NULL AND ( " +                                      	// 로그인: 조건 만족 이벤트
	        "         e.EVENT_TARGET_ACCOUNT->>'$.type' = 'ALL' " +                	// 전체 대상
	        "      OR (e.EVENT_TARGET_ACCOUNT->>'$.type' = 'AMOUNT' " +            	// 구매금액 조건
	        "          AND a.totalAmount >= CAST(e.EVENT_TARGET_ACCOUNT->>'$.amount' AS UNSIGNED)) " +
	        "      OR (e.EVENT_TARGET_ACCOUNT->>'$.type' = 'JOINED' " +           	// 가입기간 조건
	        "          AND a.joinedDate BETWEEN " +
	        "              STR_TO_DATE(e.EVENT_TARGET_ACCOUNT->>'$.startDate','%Y-%m-%d') " +
	        "              AND STR_TO_DATE(e.EVENT_TARGET_ACCOUNT->>'$.endDate','%Y-%m-%d')) " +
	        "      OR (e.EVENT_TARGET_ACCOUNT->>'$.type' = 'MEMBER_TYPE' " +      	// 회원등급 조건
	        "          AND JSON_CONTAINS(JSON_EXTRACT(e.EVENT_TARGET_ACCOUNT,'$.memberType'), JSON_QUOTE(a.accountRole))) " +
	        "    )) " +
	        ") " +

	        // 위시리스트 LEFT JOIN: 로그인 사용자 기준
	        "LEFT JOIN WISHLIST w ON w.ITEM_PK = ib.itemPk AND w.ACCOUNT_PK = ? " +

	        // GROUP BY: 집계 함수 사용으로 인해 필요
	        "GROUP BY ib.itemPk, ib.itemName, ib.itemPrice, ib.itemRegistDate, ib.itemImageUrl, ib.itemCategory, w.WISHLIST_PK";






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
	private static final String BUY_ITEM = 
			"UPDATE ITEM SET ITEM_STOCK = ITEM_STOCK - ? WHERE ITEM_PK = ?";

	// 단일 상품 재고 복귀
	private static final String ROLLBACK_ITEM_STOCK = 
			"UPDATE ITEM SET ITEM_STOCK = ITEM_STOCK + ? WHERE ITEM_PK = ?";

	// 재고 확인 (구매 가능 여부)
	private static final String ITEM_STOCK_ENOUGH =	
			"SELECT ITEM_PK, ITEM_NAME FROM ITEM WHERE ITEM_PK = ? AND ITEM_STOCK >= ?";

	// 찜 여부
    private static final String SELECT_WISHLIST_TOGGLE = 
		    "SELECT EXISTS( " +
		    "    SELECT 1 FROM WISHLIST WHERE ACCOUNT_PK = ? AND ITEM_PK = ? " + // SELECT 1 : 결과 있다1, 없다0
		    ") AS itemWishlistToggle";
    
    
    
    // ==============
 	//   관리자 쿼리문
 	// ==============
 	
    // 대시보드 카테고리별 판매량 집계
    private static final String SELECT_ALL_DASHBOARD_CATEGORY_SALES = 
    	    "SELECT " +
    	   "  I.ITEM_CATEGORY AS category, " +
    	   "  SUM(OI.ORDERS_ITEM_PRICE * OI.ORDERS_ITEM_COUNT) AS salesAmount, " +
    	   "  SUM(OI.ORDERS_ITEM_COUNT) AS salesCount " +
    	   "FROM ORDERS_ITEM OI " +
    	   "LEFT JOIN ITEM I ON I.ITEM_PK = OI.ITEM_PK " +
    	   "GROUP BY I.ITEM_CATEGORY";
    
    // 대시보드 n일간 판매량 집계
    private static final String SELECT_ALL_DASHBOARD_DAILY_SALES =
    	    "WITH RECURSIVE date_dim AS ( " +
    	    "    SELECT DATE_SUB(CURDATE(), INTERVAL ? DAY) AS d " +
    	    "    UNION ALL " +
    	    "    SELECT DATE_ADD(d, INTERVAL 1 DAY) " +
    	    "    FROM date_dim " +
    	    "    WHERE d < DATE_SUB(CURDATE(), INTERVAL 1 DAY) " +
    	    "), " +
    	    "sales AS ( " +
    	    "    SELECT " +
    	    "        DATE(o.ORDERS_DATE) AS d, " +
    	    "        SUM(oi.ORDERS_ITEM_COUNT * oi.ORDERS_ITEM_PRICE) AS salesAmount, " +
    	    "        SUM(oi.ORDERS_ITEM_COUNT) AS salesCount " +
    	    "    FROM ORDERS o " +
    	    "    JOIN ORDERS_ITEM oi ON oi.ORDERS_PK = o.ORDERS_PK " +
    	    "    WHERE o.ORDERS_DATE >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
    	    "      AND o.ORDERS_DATE < CURDATE() " +
    	    "    GROUP BY DATE(o.ORDERS_DATE) " +
    	    ") " +
    	    "SELECT " +
    	    "    DATE_FORMAT(dd.d, '%Y-%m-%d') AS date, " +
    	    "    COALESCE(s.salesAmount, 0) AS salesAmount, " +
    	    "    COALESCE(s.salesCount, 0) AS salesCount " +
    	    "FROM date_dim dd " +
    	    "LEFT JOIN sales s ON s.d = dd.d " +
    	    "ORDER BY dd.d";

    
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
		"    AND ( ? IS NULL OR ? = 'ALL' OR i.ITEM_CATEGORY = ? ) " +       // itemCategory 검색
		"    AND ( ? IS NULL OR i.ITEM_PRICE >= ? ) " +         // itemPriceMin
		"    AND ( ? IS NULL OR i.ITEM_PRICE <= ? ) " +         // itemPriceMax
		"    AND ( ? IS NULL OR i.ITEM_REGIST_DATE >= ? ) " +   // itemRegistDateStart
		"    AND ( ? IS NULL OR i.ITEM_REGIST_DATE < ? + INTERVAL 1 DAY ) " +   // itemRegistDateEnd
		"ORDER BY i.ITEM_PK DESC";                              // 기본 정렬: 최근 등록 순
    
 	// 상품 삭제
 	private static final String ADMIN_DELETE_ITEM = 
 		"DELETE FROM ITEM WHERE ITEM_PK = ?";
 	
 	// 상품 등록
 	private static final String ADMIN_INSERT_ITEM =
 		    "INSERT INTO ITEM (" +
 		    "ITEM_NAME, ITEM_PRICE, ITEM_STOCK, ITEM_IMAGE_URL, ITEM_DESCRIPTION, ITEM_CATEGORY, ITEM_REGIST_DATE" +
 		    ") VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE)";

 	// 상품 상세 보기 (관리자용)
 	// 관리자가 특정 상품 1개의 상세정보, 판매량, 리뷰수, 찜수를 조회
 	private static final String ADMIN_SELECT_ONE_ITEM =
 	        "SELECT " +
 	        "i.ITEM_PK AS itemPk, " +
 	        "i.ITEM_NAME AS itemName, " +
 	        "i.ITEM_PRICE AS itemPrice, " +
 	        "i.ITEM_DESCRIPTION AS itemDescription, " +
 	        "i.ITEM_CATEGORY AS itemCategory, " +
 	        "i.ITEM_IMAGE_URL AS itemImageUrl, " +
 	        "i.ITEM_STOCK AS itemStock, " +
 	        "DATE_FORMAT(i.ITEM_REGIST_DATE, '%Y-%m-%d') AS itemRegistDate, " +

 	        // 총 판매량
 	        "(SELECT IFNULL(SUM(oi.ORDERS_ITEM_COUNT), 0) " +
 	        "   FROM ORDERS_ITEM oi " +
 	        "  WHERE oi.ITEM_PK = i.ITEM_PK) AS itemSoldCount, " +

 	        // 리뷰 수
 	        "(SELECT COUNT(*) " +
 	        "   FROM REVIEW r " +
 	        "  WHERE r.ITEM_PK = i.ITEM_PK) AS itemReviewCount, " +

 	        // 위시리스트 수
 	        "(SELECT COUNT(*) " +
 	        "   FROM WISHLIST w " +
 	        "  WHERE w.ITEM_PK = i.ITEM_PK) AS itemWishlistCount " +

 	        "FROM ITEM i " +
 	        "WHERE i.ITEM_PK = ?";

		 	
 	
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
		
		
		// 상품 전체 보기 (pk 순으로)
		if ("SELECT_ALL_ITEM".equals(itemDTO.getCondition())) {
			
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
			
			return jdbcTemplate.query(
				SELECT_ALL_WISHLIST_ITEM,
				new BeanPropertyRowMapper<>(ItemDTO.class),
				itemDTO.getAccountPk()
			);
		}
		
		// 관리자 상품 검색
		else if("ADMIN_SEARCH_ITEM".equals(itemDTO.getCondition())) {
			
			
			return jdbcTemplate.query(
		        ADMIN_SEARCH_ITEM,
		        new BeanPropertyRowMapper<>(ItemDTO.class),
		        
		        itemDTO.getItemPk(), itemDTO.getItemPk(),
		        itemDTO.getItemName(), itemDTO.getItemName(),
		        itemDTO.getItemCategory(), itemDTO.getItemCategory(), itemDTO.getItemCategory(),
		        itemDTO.getItemPriceMin(), itemDTO.getItemPriceMin(),
		        itemDTO.getItemPriceMax(), itemDTO.getItemPriceMax(),
		        itemDTO.getItemRegistDateStart(), itemDTO.getItemRegistDateStart(),
		        itemDTO.getItemRegistDateEnd(), itemDTO.getItemRegistDateEnd()
		    );
		}
		
		// 대시보드 카테고리별 판매량
		else if("SELECT_ALL_DASHBOARD_CATEGORY_SALES".equals(itemDTO.getCondition())) {
			return jdbcTemplate.query(
					SELECT_ALL_DASHBOARD_CATEGORY_SALES,
			        new BeanPropertyRowMapper<>(ItemDTO.class)
			    );
		}
		
		else if("SELECT_ALL_DASHBOARD_DAILY_SALES".equals(itemDTO.getCondition())) {
			return jdbcTemplate.query(
					SELECT_ALL_DASHBOARD_DAILY_SALES,
			        new BeanPropertyRowMapper<>(ItemDTO.class),
			        itemDTO.getDays(), itemDTO.getDays()
			    );
		}
		
		
		return null;
	}
	
	public ItemDTO selectOne(ItemDTO itemDTO) {
	    
	    // 재고 체크
	    if ("ITEM_STOCK_ENOUGH".equals(itemDTO.getCondition())) {
						
	    	List<ItemDTO> list =  jdbcTemplate.query(
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
	    	return list.isEmpty() ? null : list.get(0);
	    } 
	    
	    // 전체 상품 개수
	    else if ("TOTAL_ITEM_COUNT".equals(itemDTO.getCondition())) {
						
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
	    
	    else if ("SELECT_ONE_CHECK_ITEM_EXISTS".equals(itemDTO.getCondition())) {
	    	List<ItemDTO> list = jdbcTemplate.query(
	    			SELECT_ONE_CHECK_ITEM_EXISTS,
	                new BeanPropertyRowMapper<>(ItemDTO.class),
	                itemDTO.getItemPk()
	            );
		    	return list.isEmpty() ? null : list.get(0);
	    }
	    
        // 상품 상세 보기
	    else if ("SELECT_ONE_ITEM_DETAIL".equals(itemDTO.getCondition())) {
	    		    	
	    	List<ItemDTO> list = jdbcTemplate.query(
	    		SELECT_ONE_ITEM_DETAIL,
                new BeanPropertyRowMapper<>(ItemDTO.class),
                itemDTO.getAccountPk(),  // 1. CTE용 ACCOUNT_PK (acct CTE에서 사용)
                itemDTO.getItemPk(),		 // 5️. 조회할 ITEM_PK
                itemDTO.getAccountPk(),  // 2️. 이벤트 조건용 로그인 여부 (NULL이면 비로그인)
                itemDTO.getAccountPk(),  // 3️. 이벤트 조건용 로그인 여부 (NOT NULL이면 로그인)
                itemDTO.getAccountPk()	 // 4️, WISHLIST JOIN용 로그인 사용자 PK
            );
	    	return list.isEmpty() ? null : list.get(0);
	    }
	    
	    // 찜 여부 
	    else if("SELECT_WISHLIST_TOGGLE".equals(itemDTO.getCondition())) {
						
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
	    		    	
	    	return jdbcTemplate.queryForObject(
	    		ADMIN_SELECT_ONE_ITEM,
	    		new BeanPropertyRowMapper<>(ItemDTO.class),
	            itemDTO.getItemPk()    
	    	);
	    }
	    
	    
	    return null;
	}
	
	
	public boolean insert(ItemDTO itemDTO) {
	    
	    int result = 0;
	    
	    // 관리자용 : 상품 등록
		if("ADMIN_INSERT_ITEM".equals(itemDTO.getCondition())) {
			
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
			
		}
		return result > 0;
	}
	

	public boolean update(ItemDTO itemDTO) {
	    
	    int result = 0;
	    
	    // 상품 구매
	    if ("BUY_ITEM".equals(itemDTO.getCondition())) {
	    	
	        result = jdbcTemplate.update
	        		(BUY_ITEM, 
	        				itemDTO.getItemStock(), 
	        				itemDTO.getItemPk());
	    } 
	    
	    // 장바구니에 담긴 수량만큼 재고 차감
	    else if ("DECREASE_ITEM_STOCK_BY_CART".equals(itemDTO.getCondition())) {
	    	
	        result = jdbcTemplate.update(DECREASE_ITEM_STOCK_BY_CART, itemDTO.getAccountPk(), itemDTO.getAccountPk());
	    }
	    
	    // 상품 재고 복구
	    else if ("ROLLBACK_ITEM_STOCK".equals(itemDTO.getCondition())) {
	    	
	        result = jdbcTemplate.update(ROLLBACK_ITEM_STOCK, itemDTO.getItemStock(), itemDTO.getItemPk());
	    }
	    
	    // 관리자용 : 상품 이름 수정
	    else if("ADMIN_UPDATE_NAME_ITEM".equals(itemDTO.getCondition())) {
	    	
	    	result = jdbcTemplate.update(ADMIN_UPDATE_NAME_ITEM, itemDTO.getItemName(), itemDTO.getItemPk());
	    }
	    
	    // 관리자용 : 상품 가격 수정
	    else if("ADMIN_UPDATE_PRICE_ITEM".equals(itemDTO.getCondition())) {
	    	
	    	result = jdbcTemplate.update(ADMIN_UPDATE_PRICE_ITEM, itemDTO.getItemPrice(), itemDTO.getItemPk());
	    }
	    
	    // 관리자용 : 상품 재고 수정
	    else if("ADMIN_UPDATE_STOCK_ITEM".equals(itemDTO.getCondition())) {
	    	
	    	result = jdbcTemplate.update(ADMIN_UPDATE_STOCK_ITEM, itemDTO.getItemStock(), itemDTO.getItemPk());
	    }
	    
	    // 관리자용 : 상품 설명 수정
	    else if("ADMIN_UPDATE_DESCRIPTION_ITEM".equals(itemDTO.getCondition())) {
	    	
	    	result = jdbcTemplate.update(ADMIN_UPDATE_DESCRIPTION_ITEM, 
	    			itemDTO.getItemDescription(), itemDTO.getItemPk());
	    }
	    
	    // 관리자용 : 상품 이미지 수정 
	    else if("ADMIN_UPDATE_IMAGE_ITEM".equals(itemDTO.getCondition())) {
	    	
	    	result = jdbcTemplate.update(ADMIN_UPDATE_IMAGE_ITEM, itemDTO.getItemImageUrl(), itemDTO.getItemPk());
	    }
	    
	    else {
		    
	    }
	    return result > 0;
	}
	

	public boolean delete(ItemDTO itemDTO) {
		
	    int result = 0;
	    
		// 관리자용 상품 삭제
		if("ADMIN_DELETE_ITEM".equals(itemDTO.getCondition())) {
	    	
			result = jdbcTemplate.update(
				ADMIN_DELETE_ITEM,
				itemDTO.getItemPk()
			);
		}
		else {
			
		}
		return result > 0;
	}
}
