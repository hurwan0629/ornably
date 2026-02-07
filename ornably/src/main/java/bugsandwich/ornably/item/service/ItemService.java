package bugsandwich.ornably.item.service;

import java.util.List;

import bugsandwich.ornably.item.ItemDTO;
import bugsandwich.ornably.review.ReviewDTO;

public interface ItemService {
	boolean insertItem(ItemDTO boardDTO);
	boolean updateItem(ItemDTO boardDTO);
	boolean deleteItem(ItemDTO boardDTO);
	
	ItemDTO getItem(ItemDTO boardDTO);
	List<ItemDTO> getItemList(ItemDTO boardDTO);
}
