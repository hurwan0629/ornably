package bugsandwich.ornably.ordersItem.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.orders.OrdersDTO;
import bugsandwich.ornably.orders.OrdersRepository;
import bugsandwich.ornably.orders.service.OrdersService;
import bugsandwich.ornably.ordersItem.OrdersItemDTO;
import bugsandwich.ornably.ordersItem.service.OrdersItemService;
import bugsandwich.ornably.security.OrnablyUser;

@RestController
@RequestMapping("/api")
public class OrdersItemController {
   
   @Autowired
   private OrdersItemService ordersItemService;
   
   @Autowired
   private OrdersService ordersService;
   
   @Autowired
   private OrdersRepository ordersRepository;	
   

   
//  ===================== 주문내역 전체 보기 =====================
   @PreAuthorize("hasRole('USER')")    // GET /api/user/orders-item/me?ordersPk={number}
   @GetMapping("/user/orders-item/me")
   public ResponseEntity<Map<String, Object>> getOrdersItemList(
         @ModelAttribute OrdersItemDTO ordersItemDTO,
         @AuthenticationPrincipal OrnablyUser ornablyUser
         ){
      
	   /*
       if (ordersItemDTO.getOrdersPk() <= 0) {
           return ResponseEntity.badRequest().body(Map.of(
                   "code", "VALIDATION_ERROR",
                   "message", "ordersPk가 올바르지 않습니다."
           ));
       }
       */
       Integer ordersPk = ordersItemDTO.getOrdersPk();
      
      ordersItemDTO.setCondition("SELECT_ALL_ORDERS_ITEM");
      ordersItemDTO.setAccountPk(ornablyUser.getAccountPk());
      
      List<OrdersItemDTO> list = ordersItemService.getOrdersItemList(ordersItemDTO);
      
      OrdersDTO ordersDTO = new OrdersDTO();   
      ordersDTO.setOrdersPk(ordersPk);
      // ordersDTO.setAccountPk(ornablyUser.getAccountPk());
      ordersDTO.setCondition("SELECT_ONE_ORDERS_PAGE_DATA");
      
      OrdersDTO data = ordersService.getOrdersData(ordersDTO);
      
      System.out.println(data);
      
      return ResponseEntity.ok(Map.of(
            "ordersItemDatas", list,
               "ordersData", data
               ));
   }
   
}
