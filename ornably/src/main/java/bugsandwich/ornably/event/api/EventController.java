package bugsandwich.ornably.event.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import bugsandwich.ornably.event.EventDTO;
import bugsandwich.ornably.event.Service.EventService;

@RestController
@RequestMapping("/api")
public class EventController {
	
	private EventService eventService;
	
//  ===================== 이벤트 정보 요청 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/event/all")
	public ResponseEntity<Map<String, Object>> getEvent(EventDTO eventDTO){
		
	    eventDTO.setCondition("ADMIN_SELECT_ALL_EVENT");
		
		List<EventDTO> list = eventService.getEventList(eventDTO);
		
		return ResponseEntity.ok(null);
	}
	
	
	// ===================== 이벤트 종료 요청 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/admin/event/{eventPk}/end")
	public ResponseEntity<Map<String, Object>> endEvent(
	        @PathVariable Integer eventPk,
	        EventDTO eventDTO
	) {	
		
		eventDTO.setEventPk(eventPk);
		eventDTO.setCondition("DELETE_EVENT");
		
		if(!eventService.updateEvent(eventDTO)) {
			return ResponseEntity.status(404).body(Map.of(
					"code", "VALIDATION_ERROR",
					"message", "이벤트를 찾을 수 없습니다.."
					));
			
		}
		
	    return ResponseEntity.ok(Map.of(
	            "eventPk", eventDTO.getEventPk(),
	            "eventEndDate", eventDTO.getEventEndDate()
	    ));
	}
	
	
	
//  ===================== 현재 진행중인 이벤트 =====================
	@GetMapping("/all/event/in-progress")
	public ResponseEntity<Map<String, Object>> mainEvent(EventDTO eventDTO){
		
		eventDTO.setCondition("SELECT_ALL_EVENT");
	    List<EventDTO> list = eventService.getEventList(eventDTO);

	    // 이벤트 없을 때
	    if (list.isEmpty()) {
	        return ResponseEntity.status(404).body(Map.of(
	                "code", "NO_ACTIVE_EVENT",
	                "message", "현재 진행중인 이벤트가 없습니다."
	        ));
	    }

	    return ResponseEntity.ok(Map.of(
	            "eventDatas", list
	    ));
	}
	
//  ===================== 이벤트 등록 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/admin/event")
	public ResponseEntity<Map<String, Object>> insertEvent(
	        @RequestPart("eventImage") MultipartFile eventImage,
	        @RequestPart("eventTargetAccount") String eventTargetAccountJson,  // JSON 문자열로 받기
	        @RequestBody EventDTO eventDTO){
		
		
		if(!eventService.insertEvent(eventDTO)) {
    		return ResponseEntity.status(404).body(Map.of(
        			"code", "ITEM_NOT_FOUND",
        			"message", "해당 이벤트 정보를 찾을 수 없습니다."
    				));
		}
		
		return ResponseEntity.ok().body(Map.of(
				"code", "sucess",
				"message", "이벤트 등록 성공"
				));
	}
	
}
