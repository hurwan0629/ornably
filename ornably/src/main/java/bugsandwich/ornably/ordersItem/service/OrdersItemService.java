package bugsandwich.ornably.ordersItem.service;

import java.util.List;

import bugsandwich.ornably.ordersItem.OrdersItemDTO;

public interface OrdersItemService {
	boolean insertOrdersItem(OrdersItemDTO ordersItemDTO);
	boolean updateOrdersItem(OrdersItemDTO ordersItemDTO);
	boolean deleteOrdersItem(OrdersItemDTO ordersItemDTO);
	
	OrdersItemDTO getOrdersItemData(OrdersItemDTO ordersItemDTO);
	List<OrdersItemDTO> getOrdersItemList(OrdersItemDTO ordersItemDTO);
	
}
