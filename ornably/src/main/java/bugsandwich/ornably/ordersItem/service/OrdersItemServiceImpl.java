package bugsandwich.ornably.ordersItem.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bugsandwich.ornably.ordersItem.OrdersItemDTO;
import bugsandwich.ornably.ordersItem.OrdersItemRepository;

@Service
public class OrdersItemServiceImpl implements OrdersItemService {
	
	@Autowired
   private OrdersItemRepository ordersItemRepository;
   
   @Override
   public boolean insertOrdersItem(OrdersItemDTO ordersItemDTO) {
      return false;
   }

   @Override
   public boolean updateOrdersItem(OrdersItemDTO ordersItemDTO) {
      return false;
   }

   @Override
   public boolean deleteOrdersItem(OrdersItemDTO ordersItemDTO) {
      return false;
   }

   @Override
   public OrdersItemDTO getOrdersItemData(OrdersItemDTO ordersItemDTO) {
      return null;
   }

   @Override
   public List<OrdersItemDTO> getOrdersItemList(OrdersItemDTO ordersItemDTO) {
      return ordersItemRepository.selectAll(ordersItemDTO);
   }

}
