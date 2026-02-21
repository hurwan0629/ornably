package bugsandwich.ornably.item.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bugsandwich.ornably.item.ItemDTO;
import bugsandwich.ornably.item.ItemRepository;

@Service
public class ItemServiceImpl implements ItemService {

	@Autowired
	private ItemRepository itemRepository;
	

	@Override
	public boolean insertItem(ItemDTO itemDTO) {
		return itemRepository.insert(itemDTO);
	}

	@Override
	public boolean updateItem(ItemDTO itemDTO) {
		return itemRepository.update(itemDTO);
	}

	@Override
	public boolean deleteItem(ItemDTO itemDTO) {
		return itemRepository.delete(itemDTO);
	}

	@Override
	public ItemDTO getItem(ItemDTO itemDTO) {
		return itemRepository.selectOne(itemDTO);
	}
	
	@Override
	public List<ItemDTO> getItemList(ItemDTO itemDTO) {
		return itemRepository.selectAll(itemDTO);
	}
	
	
	
	
}
