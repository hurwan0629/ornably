package bugsandwich.ornably.ordersItem;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrdersItemRepository {
	@Autowired	// 의존 주입
	private JdbcTemplate jdbcTemplate;
	
	// 주문 상세 조회
	private static final String SELECT_ALL_ORDERS_ITEM = 
		    "SELECT DISTINCT ORD.ORDERS_PK 	AS ordersPk, " +
		    "       ORD.ORDERS_ITEM_PK 		AS ordersItemPk, " +
		    "       I.ITEM_NAME 			AS itemName, " +
		    "       I.ITEM_PK 				AS itemPk, " +
		    "       ORD.ORDERS_ITEM_COUNT 	AS ordersItemCount, " +
		    "       ORD.ORDERS_ITEM_PRICE 	AS ordersItemPrice, " +
		    "       I.ITEM_IMAGE_URL 		AS itemImageUrl, " +
		    "       CASE WHEN EXISTS (SELECT 1 FROM REVIEW R WHERE R.ITEM_PK = I.ITEM_PK AND R.ACCOUNT_PK = ?) " +
		    "            THEN 1 ELSE 0 END AS isReviewed " +
		    "FROM ( " +
		    "    SELECT O.ORDERS_PK, OI.ORDERS_ITEM_PK, OI.ITEM_PK, OI.ORDERS_ITEM_COUNT, OI.ORDERS_ITEM_PRICE " +
		    "    FROM ORDERS O INNER JOIN ORDERS_ITEM OI ON O.ORDERS_PK = OI.ORDERS_PK " +
		    ") ORD " +
		    "INNER JOIN ITEM I ON ORD.ITEM_PK = I.ITEM_PK " +
		    "WHERE ORD.ORDERS_PK = ? " +
		    "ORDER BY ORD.ORDERS_ITEM_PK";
					
	// 주문 상세 등록
	private static final String INSERT_ORDERS_ITEM = 
		    "INSERT INTO ORDERS_ITEM (ORDERS_PK, ITEM_PK, ORDERS_ITEM_COUNT, ORDERS_ITEM_PRICE) " +
		    "VALUES (?, ?, ?, ?)";

	
	// 주문 상세 삭제
	private static final String DELETE_ORDERS_ITEM = 
		    "DELETE FROM ORDERS_ITEM " +
		    "WHERE ORDERS_PK IN (SELECT ORDERS_PK FROM ORDERS WHERE ACCOUNT_PK = ?)";
	
    
    
	
	public List<OrdersItemDTO> selectAll(OrdersItemDTO ordersItemDTO){
		
		
		// 주문내역에 해당 주문상세들 전체 출력
		if("SELECT_ALL_ORDERS_ITEM".equals(ordersItemDTO.getCondition())) {
			
			
			return jdbcTemplate.query(
				SELECT_ALL_ORDERS_ITEM,
				new BeanPropertyRowMapper<>(OrdersItemDTO.class),
				ordersItemDTO.getAccountPk(),
				ordersItemDTO.getOrdersPk()
			);
		}
		
		return null;
	}
	

	private OrdersItemDTO selectOne(OrdersItemDTO orderItemDTO) {
		return null;
	}
	
	
	public boolean insert(OrdersItemDTO orderItemDTO) {
		
		int result = 0;

		// 주문이 들어오면 해당 주문의 상품 상세 정보를 DB에 기록
		if("INSERT_ORDERS_ITEM".equals(orderItemDTO.getCondition())) {
			System.out.println("adf");
			System.out.println(orderItemDTO);
			result = jdbcTemplate.update(
				INSERT_ORDERS_ITEM,
				orderItemDTO.getOrdersPk(),
				orderItemDTO.getItemPk(),
				orderItemDTO.getOrdersItemCount(),
				orderItemDTO.getOrdersItemPrice() // ORDERS_ITEM_PRICE : 수량 X 단가 = 총액
			);
		}
		else {
			
		}
		return result > 0;
	}
	

	private boolean update(OrdersItemDTO orderItemDTO) {
		return false;
	}
	
	
	public boolean delete(OrdersItemDTO ordersItemDTO) {
		
		int result = 0;
		
		// 주문 상세 삭제
		if("DELETE_ORDERS_ITEM".equals(ordersItemDTO.getCondition())) {
			result = jdbcTemplate.update(
				DELETE_ORDERS_ITEM, 
				ordersItemDTO.getAccountPk()
			);
		}
		else {
			
		}
		return result > 0;
	}
}