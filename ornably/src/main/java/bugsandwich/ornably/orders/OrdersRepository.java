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
		"WITH ranked_items AS ( "
		+ "  SELECT "
		+ "    OI.ORDERS_PK, "
		+ "    OI.ITEM_PK, "
		+ "    (OI.ORDERS_ITEM_PRICE * OI.ORDERS_ITEM_COUNT) AS lineAmount, "
		+ "    ROW_NUMBER() OVER ( "
		+ "      PARTITION BY OI.ORDERS_PK "
		+ "      ORDER BY (OI.ORDERS_ITEM_PRICE * OI.ORDERS_ITEM_COUNT) DESC, OI.ITEM_PK ASC "
		+ "    ) AS rn "
		+ "  FROM ORDERS_ITEM OI "
		+ "), "
		+ "order_sum AS (\r\n"
		+ "  SELECT\r\n"
		+ "    OI.ORDERS_PK,\r\n"
		+ "    SUM(OI.ORDERS_ITEM_COUNT) AS ordersItemCount,\r\n"
		+ "    SUM(OI.ORDERS_ITEM_PRICE * OI.ORDERS_ITEM_COUNT) AS ordersTotalAmount\r\n"
		+ "  FROM ORDERS_ITEM OI\r\n"
		+ "  GROUP BY OI.ORDERS_PK\r\n"
		+ ")\r\n"
		+ "SELECT\r\n"
		+ "  O.ORDERS_PK AS ordersPk,\r\n"
		+ "  O.ORDERS_STATUS AS ordersStatus,\r\n"
		+ "  I.ITEM_IMAGE_URL AS itemImageUrl,\r\n"
		+ "  I.ITEM_NAME AS ordersSignatureItemName,\r\n"
		+ "  S.ordersItemCount AS ordersItemCount,\r\n"
		+ "  O.ADDRESS_NAME AS addressName,\r\n"
		+ "  DATE_FORMAT(O.ORDERS_DATE, '%Y-%m-%d') AS ordersDate,\r\n"
		+ "  S.ordersTotalAmount AS ordersTotalAmount\r\n"
		+ "FROM ORDERS O\r\n"
		+ "JOIN order_sum S\r\n"
		+ "  ON S.ORDERS_PK = O.ORDERS_PK\r\n"
		+ "JOIN ranked_items R\r\n"
		+ "  ON R.ORDERS_PK = O.ORDERS_PK AND R.rn = 1\r\n"
		+ "JOIN ITEM I\r\n"
		+ "  ON I.ITEM_PK = R.ITEM_PK\r\n"
		+ "WHERE O.ACCOUNT_PK = ?\r\n"
		+ "ORDER BY O.ORDERS_DATE DESC";
	/*
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
        "JOIN ORDERS_ITEM OI " +
        "ON O.ORDERS_PK = OI.ORDERS_PK " +
        "JOIN ITEM I " +
        "ON OI.ITEM_PK = I.ITEM_PK " +
        "WHERE O.ACCOUNT_PK = ? " +
        "GROUP BY O.ORDERS_PK " +
        "ORDER BY O.ORDERS_DATE DESC";
	*/

	// 주문 추가
    private static final String INSERT_ORDERS =
            "INSERT INTO ORDERS " +
            "    (ACCOUNT_PK, ORDERS_DATE, ADDRESS_NAME, ORDERS_PAYMENT_TYPE, ORDERS_IMPORT_UID, ORDERS_MESSAGE, ORDERS_STATUS) " +
            "SELECT ?, NOW(), ADDRESS_NAME, ?, ?, ?, '상품 준비중' " +
            "FROM ADDRESS " +
            "WHERE ADDRESS_PK = ?";
    
    
    // 주문 상세 페이지 주문 정보 조회
    private final static String SELECT_ONE_ORDERS_PAGE_DATA =
    		"SELECT \r\n"
    		+ "ORDERS_IMPORT_UID ordersImportUid,"
    		+ "ORDERS_DATE ordersDate,"
    		+ "ORDERS_STATUS ordersStatus,"
    		+ "ORDERS_PAYMENT_TYPE ordersPaymentType,"
    		+ "ORDERS_MESSAGE ordersMessage, "
    		+ "ADDRESS_NAME addressName "
    		+ "FROM ORDERS O "
    		+ "WHERE ORDERS_PK = ?";
    
	// 회원 최근 주문 조회 
	private final static String SELECT_ORDERS_PK_BY_UID =
			"SELECT ORDERS_PK AS ordersPk " + 
			"FROM ORDERS " +
			"WHERE ORDERS_IMPORT_UID = ?";
			/*
	    "SELECT ORDERS_PK AS ordersPk " +
	    "FROM ORDERS " +
	    "WHERE ACCOUNT_PK = ? " +
	    "ORDER BY ORDERS_PK DESC " +
	    "LIMIT 1";
*/
	// 회원 주문 삭제
	private final static String DELETE_ONE_ORDERS =
	    "DELETE FROM ORDERS WHERE ACCOUNT_PK = ?";

	
	
	
	public List<OrdersDTO> selectAll(OrdersDTO orderDTO){
		

		// 마이 페이지 들어갔을 때 주문내역 전체 출력
		if("SELECT_ALL_ORDERS_BY_ACCOUNT_PK".equals(orderDTO.getCondition())) {
			
			return jdbcTemplate.query(
					SELECT_ALL_ORDERS_BY_ACCOUNT_PK,
				new BeanPropertyRowMapper<>(OrdersDTO.class),
				orderDTO.getAccountPk()
			);
		}
		
		return null;
	}
	
	
	public OrdersDTO selectOne(OrdersDTO orderDTO) {
		

		// 주문내역 생성 후 해당 주문내역의 주문상세 생성을 위한 주문내역 PK 보내줌
		if("SELECT_ONE_ORDERS_PK_BY_UID".equals(orderDTO.getCondition())) {
			
			return jdbcTemplate.queryForObject(
				SELECT_ORDERS_PK_BY_UID,
				new BeanPropertyRowMapper<>(OrdersDTO.class),
				orderDTO.getOrdersImportUid()
			);
		}
		else if("SELECT_ONE_ORDERS_PAGE_DATA".equals(orderDTO.getCondition())) {
			
			return jdbcTemplate.queryForObject(
				SELECT_ONE_ORDERS_PAGE_DATA,
				new BeanPropertyRowMapper<>(OrdersDTO.class),
				orderDTO.getOrdersPk()
			);
		}
		
		return null;
	}
	
	
	private boolean update(OrdersDTO orderDTO) {
		return false;
	}
	
	
	public boolean insert(OrdersDTO orderDTO) {
		
		int result = 0;

		// 주문내역 생성
		if("INSERT_ORDERS".equals(orderDTO.getCondition())) {
			
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
			
		}
		return result > 0;
	}
	
	
	public boolean delete(OrdersDTO orderDTO) {
		
		int result = 0;
		
		// 회원 탈퇴 시 해당 회원 주문내역 전부 삭제
		if("DELETE_ALL_ORDER_BY_ACCOUNT_PK".equals(orderDTO.getCondition())) {
			
			result = jdbcTemplate.update(
				DELETE_ONE_ORDERS,
				orderDTO.getAccountPk()
			);
		} 
		else {
			
		}
		return result > 0;	}
}


