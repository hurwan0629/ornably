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


