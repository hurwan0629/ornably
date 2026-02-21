package bugsandwich.ornably.item.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import bugsandwich.ornably.item.ItemDTO;
import bugsandwich.ornably.item.service.ItemService;
import bugsandwich.ornably.review.service.ReviewService;
import bugsandwich.ornably.security.OrnablyUser;

@RestController
@RequestMapping("/api")
public class ItemController {

	@Autowired
	private ItemService itemService;
	@Autowired
	private ReviewService reviewService;
	
	@Value("${resource.path}")
	private String resourcePath;

	@Value("${resource.item.prefix}")
	private String itemPrefix;

//  ===================== 상품 목록 보기  =====================
	@GetMapping("/all/item")
	public ResponseEntity<Map<String, Object>> getAllItems(@ModelAttribute ItemDTO itemDTO,
			@AuthenticationPrincipal OrnablyUser ornablyUser) {
		
		// ResponseBody면 항상 200OK 반환
		// HTTP 응답을 컨트롤 하기 위해 ResponseEntity 사용
		Integer accountPk = null;
		if(ornablyUser!=null) {
			accountPk = ornablyUser.getAccountPk();
		}
		itemDTO.setAccountPk(accountPk);

		// 기본값 보정 ( 1페이지 기본값 )
		if (itemDTO.getPage() == null || itemDTO.getPage() < 1) {
			itemDTO.setPage(1);
		}
		// 데이터 수 ( '20개' 기본값 )
		if (itemDTO.getDataCount() == null || itemDTO.getDataCount() < 1) {
			itemDTO.setDataCount(20);
		}
		// 카테고리 ( 'all' 기본값 )
		if (itemDTO.getCategory() == null) {
			itemDTO.setCategory("all");
		}
		// 정렬 ( 'default' 기본값 )
		if (itemDTO.getSort() == null) {
			itemDTO.setSort("default");
		}

		// 1) totalCount 먼저
		itemDTO.setCondition("TOTAL_ITEM_COUNT");
		ItemDTO total = itemService.getItem(itemDTO);

		// pagination 계산
		int limit = itemDTO.getDataCount(); // limit => 몇개 데이터를 조회할지
		int offset = (itemDTO.getPage() - 1) * limit; // offset => 앞에서 몇 개의 데이터를 건너뛸지
		// LIMIT 12 OFFSET 36;
		// 37번째 데이터 부터 12개 조회

		// DB 페이지네이션 전달 준비
		itemDTO.setItemLimit(limit);
		itemDTO.setItemOffset(offset);

		// 분기점
		itemDTO.setCondition("SELECT_ALL_ITEM");

		// 서비스 호출
		List<ItemDTO> list = itemService.getItemList(itemDTO);
		

		// maxPages 계산
		int safeLimit = Math.max(limit, 1); // limit가 0이면 1로 바꿈

		int totalCount = (total == null) ? 0 : total.getItemTotalCount();
		int maxPages = (totalCount == 0) ? 1 : (int) Math.ceil((double) totalCount / safeLimit);

		return ResponseEntity.ok(Map.of("itemDatas", list, "maxPages", maxPages));
		// Map.of => json 방식과 유사하게 데이터 저장
	}

//  ===================== 상품 상세 보기 =====================
	@GetMapping("/all/item/{itemPk}")
	public ResponseEntity<?> getItemDetail(@PathVariable Integer itemPk, ItemDTO itemDTO,
			@AuthenticationPrincipal OrnablyUser ornablyUser) {
		ItemDTO check = new ItemDTO();
		check.setItemPk(itemPk);
		check.setCondition("SELECT_ONE_CHECK_ITEM_EXISTS");
		if(itemService.getItem(check)==null) {
			return ResponseEntity.notFound().build();
		}
		
		Integer accountPk = null;
		if(ornablyUser!=null) {
			accountPk = ornablyUser.getAccountPk();
		}
		itemDTO.setAccountPk(accountPk);
		itemDTO.setItemPk(itemPk);
		itemDTO.setCondition("SELECT_ONE_ITEM_DETAIL");
		ItemDTO item = itemService.getItem(itemDTO);

		/*
		 * 로그인 상태면 accountPk 세팅 1. Spring Seciurity 에 존재하는 회원 PK 가져오기 2. 회원 pk가 null 이면
		 * 그냥 wishlistToggle 없고 3. 회원이 로그인 상태 즉 pk가 값이 있다면 wishlistToggle이 true인지
		 * false인지
		 */
		
		return ResponseEntity.ok(Map.of("itemData", item));
	}

	/*
	 * ============================================================ 관리자
	 * ===========================================================
	 */
//  ===================== 관리자 상품 목록 보기 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/item/search")
	public ResponseEntity<Map<String, Object>> adminSearchItem(@ModelAttribute ItemDTO itemDTO) {
		
		try {
			// 기본값 (필요한 것만)
			if (itemDTO.getItemCategory() == null)
				itemDTO.setItemCategory("ALL");
			if (itemDTO.getItemPriceMin() == null)
				itemDTO.setItemPriceMin(0);
			if (itemDTO.getItemPriceMax() == null)
				itemDTO.setItemPriceMax(Integer.MAX_VALUE);

			if (itemDTO.getItemPriceMin() < 0 || itemDTO.getItemPriceMax() < 0) {
				return ResponseEntity.status(400)
						.body(Map.of("code", "VALIDATION_ERROR", "message", "요청 값이 올바르지 않습니다."));
			}

			// INVALID_PRICE_RANGE
			if (itemDTO.getItemPriceMin() > itemDTO.getItemPriceMax()) {
				return ResponseEntity.status(400)
						.body(Map.of("code", "INVALID_PRICE_RANGE", "message", "가격 범위가 올바르지 않습니다."));
			}

			// 검색 실행
			itemDTO.setCondition("ADMIN_SEARCH_ITEM");
			List<ItemDTO> list = itemService.getItemList(itemDTO);
			
			// 200 OK
			return ResponseEntity.ok(Map.of("itemDatas", list));

		} catch (Exception e) {
			// 500 INTERNAL_SERVER_ERROR
			return ResponseEntity.status(500)
					.body(Map.of("code", "INTERNAL_SERVER_ERROR", "message", "상품 검색 중 오류가 발생했습니다."));
		}
	}

//  ===================== 관리자 상품 삭제 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/admin/item/{itemPk}")
	public ResponseEntity<Map<String, Object>> adminDeleteItem(@PathVariable Integer itemPk, ItemDTO itemDTO) {
		

		itemDTO.setItemPk(itemPk);
		itemDTO.setCondition("ADMIN_DELETE_ITEM");

		if (!itemService.deleteItem(itemDTO)) { // 삭제 실패 시
			return ResponseEntity.status(404).body(Map.of("code", "ITEM_NOT_FOUND", "message", "해당 상품을 찾을 수 없습니다.")); // 수정
																														// 필요
		}

		return ResponseEntity.ok(Map.of("code", "success", "message", "삭제 성공")); // 수정 필요
	}

//  ===================== 관리자 상품 등록 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping(value = "/admin/item", consumes = "multipart/form-data")
	public ResponseEntity<Map<String, Object>> adminInsertItem(@ModelAttribute ItemDTO itemDTO,
			@RequestPart("itemImage") MultipartFile itemImage) {
		
		
	    // 0) 필수값 검증
	    if (itemImage == null || itemImage.isEmpty()) {
	        return ResponseEntity.badRequest().body(Map.of(
	                "code", "VALIDATION_ERROR",
	                "message", "상품 이미지는 필수입니다."
	        ));
	    }
		
        else if(!this.reviewService.checkFileSize(itemImage)) {
            return ResponseEntity.status(400).body(Map.of(
                     "code", "TOO_BIG_IMAGE_SIZE",
                     "message", "이미지 크기는 "+ this.reviewService.getAllowedImageMaxBytes() +"kb까지 가능합니다."
             ));
         }
         else if(!this.reviewService.checkFileExtention(itemImage)) {
            return ResponseEntity.status(400).body(Map.of(
                     "code", "IMAGE_TYPE_ERROR",
                     "message", "이미지 확장자는"+ this.reviewService.getAllowedExtentionSet() +"만 가능합니다."
             ));   
         }
         
         String eventImageUrl;
		try {
			eventImageUrl = this.reviewService.saveImageAndGetUrl(this.resourcePath, this.itemPrefix, itemImage);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(Map.of("code", "IMAGE_PROCESSING_ERROR", "message", "이미지를 처리하는 도중 에러가 발생했습니다."));
		}
         /*
		// 1) 폴더
		String uploadDir = resourcePath + "/images/item/";
		File dir = new File(uploadDir);
		if (!dir.exists())
			dir.mkdirs();

		// 2) 파일명
		String fileName = UUID.randomUUID() + "_" + itemImage.getOriginalFilename();

		// 3) 저장
		File dest = new File(uploadDir, fileName);
		try {
			itemImage.transferTo(dest);
		} catch (Exception e) {
			throw new RuntimeException("파일 저장 실패", e);
		}
		*/
		// 4) DB 저장할 URL
		itemDTO.setItemImageUrl(eventImageUrl); // itemPrefix="/images/item/"
		itemDTO.setCondition("ADMIN_INSERT_ITEM");
		if (!itemService.insertItem(itemDTO)) {
			return ResponseEntity.status(500).body(Map.of("code", "ITEM_INSERT_ERROR", "message", "상품 등록중 오류가 발생했습니다."));
		}

		return ResponseEntity.ok(Map.of("code", "success", "message", "등록 성공")); // 수정 필요
	}

//  ===================== 관리자 상품 상세 보기 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/item/manage/{itemPk}")
	public ResponseEntity<Map<String, Object>> adminItemDetail(@PathVariable Integer itemPk, ItemDTO itemDTO) {
		

		itemDTO.setItemPk(itemPk);
		itemDTO.setCondition("ADMIN_SELECT_ONE_ITEM");

		ItemDTO data = itemService.getItem(itemDTO);

		if (data == null) { // data 가 null 이면
			return ResponseEntity.status(404).body(Map.of("code", "ITEM_NOT_FOUND", "message", "해당 상품이 존재하지 않습니다."));
		}

		return ResponseEntity.ok(Map.of("itemData", data));
	}

//  ===================== 관리자 상품 이름 수정 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/account/item/{itemPk}/itemName")
	public ResponseEntity<Map<String, Object>> itemNameUpdate(@PathVariable Integer itemPk,
			@RequestBody ItemDTO itemDTO) {
		

		// itemName이 없을 때
		if (itemDTO.getItemName() == null || itemDTO.getItemName().isBlank()) {
			return ResponseEntity.status(400).body(Map.of("code", "VALIDATION_ERROR", "message", "요청 값이 올바르지 않습니다."));
		}

		itemDTO.setItemPk(itemPk);
		itemDTO.setCondition("ADMIN_UPDATE_NAME_ITEM");

		// 수정 실패 시
		if (!itemService.updateItem(itemDTO)) {
			return ResponseEntity.status(404).body(Map.of("code", "ITEM_NOT_FOUND", "message", "해당 상품 정보를 찾을 수 없습니다."));
		}

		return ResponseEntity.ok(Map.of("code", "success", "message", "수정 성공")); // 수정 필요
	}

	
	

// ===================== 관리자 상품 가격 수정 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/account/item/{itemPk}/itemPrice")
	public ResponseEntity<Map<String, Object>> itemPriceUpdate(@PathVariable Integer itemPk, @RequestBody ItemDTO itemDTO) {
			

			// itemPrice 값이 0 이하
			if (itemDTO.getItemPrice() <= 0) {
				return ResponseEntity.status(400).body(Map.of(
					"code", "VALIDATION_ERROR", 
					"message", "요청 값이 올바르지 않습니다."
				));
			}
			itemDTO.setItemPk(itemPk);
			itemDTO.setCondition("ADMIN_UPDATE_PRICE_ITEM");
			
			// 수정 실패 시
			if(!itemService.updateItem(itemDTO)) {
	    		return ResponseEntity.status(404).body(Map.of(
	        			"code", "ITEM_NOT_FOUND",
	        			"message", "해당 상품 정보를 찾을 수 없습니다."
	    				));
			}
	        return ResponseEntity.ok(Map.of(
	        		"code", "success",
	        		"message", "수정 성공"
	        		)); // 수정 필요	
		}

	
	
// ===================== 관리자 상품 재고 수정 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/account/item/{itemPk}/itemStock")
	public ResponseEntity<Map<String, Object>> itemStockUpdate(@PathVariable Integer itemPk,
			@RequestBody ItemDTO itemDTO) {
		

		// itemStock 값이 0 이하
		if (itemDTO.getItemStock() <= 0) {
			return ResponseEntity.status(400).body(Map.of("code", "VALIDATION_ERROR", "message", "요청 값이 올바르지 않습니다."));
		}

		itemDTO.setItemPk(itemPk);
		itemDTO.setCondition("ADMIN_UPDATE_STOCK_ITEM");

		// 수정 실패 시
		if (!itemService.updateItem(itemDTO)) {
			return ResponseEntity.status(404).body(Map.of("code", "ITEM_NOT_FOUND", "message", "해당 상품 정보를 찾을 수 없습니다."));
		}

		return ResponseEntity.ok(Map.of("code", "success", "message", "수정 성공")); // 수정 필요
	}



	
// ===================== 관리자 상품 설명 수정 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/account/item/{itemPk}/itemDescription")
	public ResponseEntity<Map<String, Object>> itemDesciptionUpdate(@PathVariable Integer itemPk, @RequestBody ItemDTO itemDTO){
			
			
			// itemDesciptionUpdate가 값이 0이하
			if (itemDTO.getItemDescription() == null || itemDTO.getItemDescription().isBlank()){
				return ResponseEntity.status(400).body(Map.of(
						"code", "VALIDATION_ERROR", 
						"message", "요청 값이 올바르지 않습니다."
						));
			}
			
			itemDTO.setItemPk(itemPk);
			itemDTO.setCondition("ADMIN_UPDATE_DESCRIPTION_ITEM");
			
			// 수정 실패 시
			if(!itemService.updateItem(itemDTO)) {
				return ResponseEntity.status(404).body(Map.of(
						"code", "ITEM_NOT_FOUND",
						"message", "해당 상품 정보를 찾을 수 없습니다."
						));
			}	
	        return ResponseEntity.ok(Map.of(
	        		"code", "success",
	        		"message", "수정 성공"
	        		)); // 수정 필요	
		}

// ===================== 관리자 상품 이미지 수정 =====================			
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping(value = "/account/item/{itemPk}/itemImage", consumes = "multipart/form-data")
	public ResponseEntity<Map<String, Object>> itemImageUpdate(
	        @PathVariable Integer itemPk,
	        @RequestPart("itemImage") MultipartFile itemImage,
	        @ModelAttribute ItemDTO itemDTO
	) {
	    

	    // 0) 필수값 검증
	    if (itemImage == null || itemImage.isEmpty()) {
	        return ResponseEntity.badRequest().body(Map.of(
	                "code", "VALIDATION_ERROR",
	                "message", "이미지 파일이 필요합니다."
	        ));
	    }
	    /*
	    // 1) 폴더
	    String uploadDir = resourcePath + "/images/item/";
	    File dir = new File(uploadDir);
	    if (!dir.exists()) dir.mkdirs();
	    
	    // 2) 파일명 (확장자 유지 or 기본값)
	    String original = itemImage.getOriginalFilename();
	    String ext = org.springframework.util.StringUtils.getFilenameExtension(original);
	    if (ext == null || ext.isBlank()) ext = "png";

	    String fileName = UUID.randomUUID() + "." + ext.toLowerCase();

	    // 3) 저장
	    File dest = new File(uploadDir, fileName);
	    try {
	        itemImage.transferTo(dest);
	    } catch (Exception e) {
	        throw new RuntimeException("파일 저장 실패", e);
	    }
	    
	    // 4) DB 저장할 URL
	    ///
	     */
	    String imageUrl= null;
		try {
			imageUrl = reviewService.saveImageAndGetUrl(resourcePath, itemPrefix, itemImage);
		} catch (IOException e) {
			e.printStackTrace();
		}
//	     = serverOrigin + itemPrefix + fileName; // itemPrefix="/images/item/"
	    itemDTO.setItemPk(itemPk);
	    itemDTO.setItemImageUrl(imageUrl);
	    itemDTO.setCondition("ADMIN_UPDATE_IMAGE_ITEM");

	    // 5) DB 업데이트 (실패 시 파일 삭제)
	    try {
	        if (!itemService.updateItem(itemDTO)) {
//	            dest.delete(); // DB 실패하면 파일 롤백
	            return ResponseEntity.status(404).body(Map.of(
	                    "code", "ITEM_NOT_FOUND",
	                    "message", "해당 상품 정보를 찾을 수 없습니다."
	            ));
	        }
	    } catch (Exception e) {
//	        dest.delete(); // 예외 나도 파일 롤백
	        throw e;
	    }
		return ResponseEntity.ok(Map.of("code", "success", "message", "수정 성공")); // 수정 필요
	}
}
