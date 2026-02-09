package bugsandwich.ornably.orders.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bugsandwich.ornably.cart.CartDTO;
import bugsandwich.ornably.cart.CartRepository;
import bugsandwich.ornably.item.ItemDTO;
import bugsandwich.ornably.item.ItemRepository;
import bugsandwich.ornably.orders.OrdersDTO;
import bugsandwich.ornably.orders.OrdersRepository;
import bugsandwich.ornably.ordersItem.OrdersItemDTO;
import bugsandwich.ornably.ordersItem.OrdersItemRepository;

@Service
public class OrdersServiceImpl implements OrdersService{
	

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrdersItemRepository ordersItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

	
	@Override
	public boolean insertOrders(OrdersDTO ordersDTO) {
		return ordersRepository.insert(ordersDTO);
	}

	@Override
	public boolean updateOrders(OrdersDTO ordersDTO) {
		return false;
	}

	@Override
	public boolean deleteOrders(OrdersDTO ordersDTO) {
		return false;
	}

	@Override
	public OrdersDTO getOrdersData(OrdersDTO ordersDTO) {
		return null;
	}

	@Override
	public List<OrdersDTO> getOrdersList(OrdersDTO ordersDTO) {
		return ordersRepository.selectAll(ordersDTO);
	}

	
	@Override
	@Transactional
	public boolean paymentComplete(OrdersDTO ordersDTO) {
		
        // 1) 장바구니 조회
		CartDTO cartDTO = new CartDTO();
		cartDTO.setCondition("SELECT_ALL_CART");
		cartDTO.setAccountPk(ordersDTO.getAccountPk());
        List<CartDTO> cartItems = cartRepository.selectAll(cartDTO);
        if (cartItems.isEmpty()) throw new RuntimeException("결제할 상품이 없습니다."); // 트랜잭션 예외 던지기
		
        
        // 2) 재고차감 
        for (CartDTO c : cartItems) {
            ItemDTO itemDTO = new ItemDTO();
            itemDTO.setItemPk(c.getItemPk());
            itemDTO.setCartCount(cartDTO.getCartCount()); // itemDTO =>  추가함
            itemDTO.setCondition("BUY_ITEM");
            if (!itemRepository.update(itemDTO)) {
                throw new RuntimeException("재고 부족");
            }
        }
        
        // 3) 주문내역 생성
        ordersDTO.setCondition("INSERT_ORDERS");
        ordersDTO.setOrdersMessage("상품준비 중...");
        ordersDTO.setOrdersPaymentType(null); // PortOne에서 받아옹기
        if(!ordersRepository.insert(ordersDTO)) {
        	throw new RuntimeException("주문내역 생성 실패..");
        }
        
        // 4) 주문 상새 내역 생성     
        for (CartDTO c : cartItems) {
            OrdersItemDTO ordersItemDTO = new OrdersItemDTO();
            ordersItemDTO.setOrdersPk(ordersDTO.getOrdersPk());
            ordersItemDTO.setItemPk(c.getItemPk());
            ordersItemDTO.setOrdersItemCount(c.getCartCount());
            ordersItemDTO.setOrdersItemPrice(c.getCartTotalPrice());
            ordersItemDTO.setCondition("INSERT_ORDERS_ITEM");
            if(ordersItemRepository.insert(ordersItemDTO)) {
            		throw new RuntimeException("주문상새 내역 생성 실패..");
            }
        } 
        
        // 5) 사용자 장바구니 삭제
        cartDTO.setAccountPk(ordersDTO.getAccountPk());
        cartDTO.setCondition("DELETE_CART_BY_ACCOUNT_PK");
        cartRepository.delete(cartDTO);
        
		return true;
	}

}
