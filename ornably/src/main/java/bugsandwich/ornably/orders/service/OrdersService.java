package bugsandwich.ornably.orders.service;

import java.util.List;

import bugsandwich.ornably.cart.CartDTO;
import bugsandwich.ornably.item.ItemDTO;
import bugsandwich.ornably.orders.OrdersDTO;
import bugsandwich.ornably.ordersItem.OrdersItemDTO;

public interface OrdersService {
	boolean insertOrders(OrdersDTO ordersDTO);
	boolean updateOrders(OrdersDTO ordersDTO);
	boolean deleteOrders(OrdersDTO ordersDTO);
	
	OrdersDTO getOrdersData(OrdersDTO ordersDTO);
	List<OrdersDTO> getOrdersList(OrdersDTO ordersDTO);
	
	boolean paymentComplete(OrdersDTO ordersDTO);
	public boolean buyNowPaymentComplete(OrdersDTO ordersDTO);
}
