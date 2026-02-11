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

	// 장바구니 조회 -> 재고 차감 -> 주문내역 생성 -> 
	@Override
	@Transactional
	public boolean paymentComplete(OrdersDTO ordersDTO) {
		
		Integer accountPk = ordersDTO.getAccountPk();
		
        // 1) 장바구니 조회
		CartDTO cartDTO = new CartDTO();
		cartDTO.setCondition("SELECT_ALL_CART");
		cartDTO.setAccountPk(accountPk);
        List<CartDTO> cartItems = cartRepository.selectAll(cartDTO);
        if (cartItems.isEmpty()) throw new RuntimeException("결제할 상품이 없습니다."); // 트랜잭션 예외 던지기
		
        
        // 2) 재고차감 
        for (CartDTO c : cartItems) {
            ItemDTO itemDTO = new ItemDTO();
            itemDTO.setItemPk(c.getItemPk());
            itemDTO.setCartCount(c.getCartCount()); // itemDTO =>  추가함
            itemDTO.setCondition("BUY_ITEM");
            if (!itemRepository.update(itemDTO)) {
                throw new RuntimeException("재고 부족"); // 재고 - 막는 수정 필요 -> 쿼리 수정 완료
            }
        }
        
        // 3) 주문내역 생성
        ordersDTO.setCondition("INSERT_ORDERS");
        if(!ordersRepository.insert(ordersDTO)) { 
        	throw new RuntimeException("주문내역 생성 실패..");
        }
        System.out.println(ordersDTO);
        ordersDTO.setCondition("SELECT_ONE_ORDERS_PK_BY_UID");
        System.out.println(ordersDTO);
        // 방금 생성한 주문내역 pk 조회 
        ordersDTO = ordersRepository.selectOne(ordersDTO);
        // 조회 실패시 트랜잭션 롤백
        if(ordersDTO.getOrdersPk() == null) {
        	throw new RuntimeException("주문내역 찾지 못함..");
        }

        	
        // 4) 주문 상새 내역 생성     
        for (CartDTO c : cartItems) {
            OrdersItemDTO ordersItemDTO = new OrdersItemDTO();
            ordersItemDTO.setOrdersPk(ordersDTO.getOrdersPk());
            ordersItemDTO.setItemPk(c.getItemPk());
            ordersItemDTO.setOrdersItemCount(c.getCartCount());
            ordersItemDTO.setOrdersItemPrice(c.getCartTotalPrice());
            ordersItemDTO.setOrdersItemPrice(c.getItemPrice());
            ordersItemDTO.setCondition("INSERT_ORDERS_ITEM");
            if(!ordersItemRepository.insert(ordersItemDTO)) { // insert => ordersDTO로 변경 가능?
            		throw new RuntimeException("주문상새 내역 생성 실패..");
            }
        } 
        
        // 5) 사용자 장바구니 삭제
        cartDTO.setAccountPk(accountPk);
        cartDTO.setCondition("DELETE_CART_BY_ACCOUNT_PK");
        System.out.println(cartDTO);
        cartRepository.delete(cartDTO);
        
		return true;
	}

}
