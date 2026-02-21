package bugsandwich.ornably.dashboard;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.item.ItemDTO;
import bugsandwich.ornably.item.ItemRepository;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

	@Autowired
	private ItemRepository ItemRepository;

	@GetMapping("/category")
	public ResponseEntity<?> getCategoriesTotalSales() {
		ItemDTO itemDTO = new ItemDTO();
		itemDTO.setCondition("SELECT_ALL_DASHBOARD_CATEGORY_SALES");

		List<ItemDTO> list = this.ItemRepository.selectAll(itemDTO);

		return ResponseEntity.ok().body(Map.of("categorySales", list));
	}

	@GetMapping("/daily")
	public ResponseEntity<?> getDailySales(@ModelAttribute ItemDTO itemDTO) {
		itemDTO.setCondition("SELECT_ALL_DASHBOARD_DAILY_SALES");

		List<ItemDTO> list = this.ItemRepository.selectAll(itemDTO);

		return ResponseEntity.ok().body(Map.of("dailySales", list));
	}
	
	@GetMapping("/online-users")
	public ResponseEntity<?> getOnlineUsers() {
		return ResponseEntity.ok().body(Map.of("onlineUsers", SessionCounter.getCount()));
	}
}
