package bugsandwich.ornably.orders;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrdersRepository {
	@Autowired // 의존주입
	private JdbcTemplate jdbcTemplate;
	
	// 회원 주문 전체 조회
	private static final String SELECT_ALL_ORDERS_BY_ACCOUNT_PK =
        "SELECT " +
        "    O.ORDERS_PK AS ordersPk, " +
        "    O.ORDERS_STATUS AS ordersStatus, " +
        "    I.ITEM_IMAGE_URL AS itemImageUrl, " +
        "    I.ITEM_NAME AS ordersSignatureItemName, " +
        "    SUM(OI.ORDERS_ITEM_COUNT) AS ordersItemCount, " +
        "    O.ADDRESS_NAME AS addressName, " +
        "    DATE_FORMAT(O.ORDERS_DATE, '%Y-%m-%d') AS ordersDate, " +
        "    SUM(OI.ORDERS_ITEM_COUNT * OI.ORDERS_ITEM_PRICE) AS ordersTotalAmount " +
        "FROM ORDERS O " +
        "JOIN ORDERS_ITEM OI ON O.ORDERS_PK = OI.ORDERS_PK " +
        "JOIN ITEM I ON OI.ITEM_PK = I.ITEM_PK " +
        "WHERE O.ACCOUNT_PK = ? " +
        "GROUP BY O.ORDERS_PK " +
        "ORDER BY O.ORDERS_DATE DESC";
	

	// 주문 추가
    private static final String INSERT_ORDERS =
            "INSERT INTO ORDERS " +
            "    (ACCOUNT_PK, ORDERS_DATE, ADDRESS_NAME, ORDERS_PAYMENT_TYPE, ORDERS_IMPORT_UID, ORDERS_MESSAGE, ORDERS_STATUS) " +
            "SELECT ?, NOW(), ADDRESS_NAME, ?, ?, ?, '상품 준비중' " +
            "FROM ADDRESS " +
            "WHERE ADDRESS_PK = ?";
    
    
	// 회원 최근 주문 조회 
	private final static String SELECT_ORDERS_PK =
	    "SELECT ORDERS_PK AS ordersPk " +
	    "FROM ORDERS " +
	    "WHERE ACCOUNT_PK = ? " +
	    "ORDER BY ORDERS_PK DESC " +
	    "LIMIT 1";

	// 회원 주문 삭제
	private final static String DELETE_ONE_ORDERS =
	    "DELETE FROM ORDERS WHERE ACCOUNT_PK = ?";

	// 방금 생성된 주문 번호 조회
	private final static String SELECT_ONE_ORDERS_PK = 
		"SELECT LAST_INSERT_ID() AS ordersPk";
	
	// 주문 상세 페이지의 주문내역 데이터
	private static final String SELEC_ONE_ORDERS_PAGE_DATA =
	      "SELECT " +
	      "    O.ORDERS_PK 			AS ordersPk, " +
	      "    OI.ORDERS_ITEM_PK 	AS ordersItemPk, " +
	      "    I.ITEM_NAME 			AS itemName, " +
	      "    I.ITEM_PK 			AS itemPk, " +
	      "    OI.ORDERS_ITEM_COUNT AS ordersItemCount, " +
	      "    OI.ORDERS_ITEM_PRICE AS ordersItemPrice, " +
	      "    I.ITEM_IMAGE_URL 	AS itemImageUrl, " +
	      "    CASE WHEN EXISTS ( " +
	      "        SELECT 1 " +
	      "        FROM REVIEW R " +
	      "        WHERE R.ITEM_PK = I.ITEM_PK " +
	      "          AND R.ACCOUNT_PK = ? " +
	      "          AND R.ORDERS_PK = O.ORDERS_PK " +
	      "    ) THEN 1 ELSE 0 END AS isReviewed " +
	      "FROM ORDERS O " +
	      "JOIN ORDERS_ITEM OI ON O.ORDERS_PK = OI.ORDERS_PK " +
	      "JOIN ITEM I ON OI.ITEM_PK = I.ITEM_PK " +
	      "WHERE O.ACCOUNT_PK = ? " +       // 로그인 사용자
	      "  AND O.ORDERS_PK = ? " +        // 조회할 주문
	      "ORDER BY OI.ORDERS_ITEM_PK";

	
	
	
	
	public List<OrdersDTO> selectAll(OrdersDTO orderDTO){
		System.out.println("[로그] OrdersRepository의 selectAll 시작");

		// 마이 페이지 들어갔을 때 주문내역 전체 출력
		if("SELECT_ALL_ORDERS_BY_ACCOUNT_PK".equals(orderDTO.getCondition())) {
			System.out.println("[로그] OrdersRepository의 SELECT_ALL_ORDERS_BY_ACCOUNT_PK");
			return jdbcTemplate.query(
				SELECT_ALL_ORDERS_BY_ACCOUNT_PK,
				new BeanPropertyRowMapper<>(OrdersDTO.class),
				orderDTO.getAccountPk()
			);
		}
		System.out.println("[로그][경고] OrdersRepository의 selectAll_condition 없음");
		// 조건이 없으면 빈 리스트 반환
	    return java.util.Collections.emptyList();
	}
	
	
	public OrdersDTO selectOne(OrdersDTO orderDTO) {
		System.out.println("[로그] OrdersRepository의 selectOne 시작");

		// 주문내역 생성 후 해당 주문내역의 주문상세 생성을 위한 주문내역 PK 보내줌
		if("SELECT_ONE_ORDERS_PK".equals(orderDTO.getCondition())) {
			System.out.println("[로그] OrdersRepository의 SELECT_ONE_ORDERS_PK");
			return jdbcTemplate.queryForObject(
				SELECT_ONE_ORDERS_PK,
				(rs, rowNum) -> {
					OrdersDTO data = new OrdersDTO();
					data.setOrdersPk(rs.getInt("ordersPk"));
					return data;
				}
			);
		}
		
		// 주문 상세 페이지의 주문 내역 데이터
		else if("SELEC_ONE_ORDERS_PAGE_DATA".equals(orderDTO.getCondition())) {
			System.out.println("[로그] OrdersRepository의 SELEC_ONE_ORDERS_PAGE_DATA");
			return jdbcTemplate.queryForObject(
				SELEC_ONE_ORDERS_PAGE_DATA,
				(rs, rowNum) -> {
					OrdersDTO data = new OrdersDTO();
					data.setOrdersPk(rs.getInt("ordersPk"));
		            data.setOrdersItemCount(rs.getInt("ordersItemCount"));
		            data.setOrdersItemPrice(rs.getInt("ordersItemPrice"));
		            data.setItemImageUrl(rs.getString("itemImageUrl"));
		            data.setOrdersSignatureItemName(rs.getString("itemName")); // 대표 아이템 이름 대신
		            data.setReviewExists(rs.getInt("isReviewed") == 1);        // 1 → true, 0 → false
		            return data;
				},
				orderDTO.getAccountPk(),  // 첫 번째 ? : 로그인 사용자
		        orderDTO.getAccountPk(),  // 두 번째 ? : 로그인 사용자
		        orderDTO.getOrdersPk()    // 세 번째 ? : 조회할 주문
			);
		}
		
		System.out.println("[로그][경고] OrdersRepository의 selectOne_condition 없음");
		return null;
	}
	
	
	private boolean update(OrdersDTO orderDTO) {
		return false;
	}
	
	
	public boolean insert(OrdersDTO orderDTO) {
		System.out.println("[로그] OrdersRepository의 insert 시작");
		int result = 0;

		// 주문내역 생성
		if("PREPARING".equals(orderDTO.getCondition())) {
			System.out.println("[로그] OrdersRepository의 PREPARING");
			result = jdbcTemplate.update(
				INSERT_ORDERS,
				orderDTO.getAccountPk(),
				orderDTO.getOrdersPaymentType(),
				orderDTO.getOrdersImportUid(),
				orderDTO.getOrdersMessage(),
				orderDTO.getAddressPk()
			);
		}
		else {
			System.out.println("[로그][경고] OrdersRepository의 insert_condition 없음");
		}
		return result > 0;
	}
	
	
	public boolean delete(OrdersDTO orderDTO) {
		System.out.println("[로그] OrdersRepository의 delete 시작");
		int result = 0;
		
		// 회원 탈퇴 시 해당 회원 주문내역 전부 삭제
		if("DELETE_ALL_ORDER_BY_ACCOUNT_PK".equals(orderDTO.getCondition())) {
			System.out.println("[로그] OrdersRepository의 DELETE_ALL_ORDER_BY_ACCOUNT_PK");
			result = jdbcTemplate.update(
				DELETE_ONE_ORDERS,
				orderDTO.getAccountPk()
			);
		} 
		else {
			System.out.println("[로그][경고] OrdersRepository의 delete_condition 없음");
		}
		return result > 0;	
	}
}


