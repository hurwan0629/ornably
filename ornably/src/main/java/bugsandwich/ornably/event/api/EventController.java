package bugsandwich.ornably.event.api;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import bugsandwich.ornably.event.EventDTO;
import bugsandwich.ornably.event.Service.EventService;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class EventController {
	
	@Autowired
	private EventService eventService;
	
	@Value("${resource.path}")
	private String resourcePath;

	@Value("${resource.event.prefix}")
	private String eventPrefix;
	
	
//  ===================== 이벤트 정보 요청 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/event/all")
	public ResponseEntity<Map<String, Object>> getEvent(EventDTO eventDTO){
		
	    eventDTO.setCondition("SELECT_ALL_EVENT");
		
		List<EventDTO> list = eventService.getEventList(eventDTO);
		
		return ResponseEntity.ok(Map.of("eventDatas", list));
	}
	
	
// ===================== 이벤트 종료 요청 =====================
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/admin/event/{eventPk}/end")
	public ResponseEntity<Map<String, Object>> endEvent(
	        @PathVariable Integer eventPk,
	        EventDTO eventDTO
	) {	
		
		eventDTO.setEventPk(eventPk);
		eventDTO.setCondition("UPDATE_END_EVENT");
		
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
		
		eventDTO.setCondition("SELECT_ALL_PROGRESS_EVENT");
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
	        @RequestPart("eventTargetAccount") String eventTargetAccountJson,
	        @RequestPart("eventTargetCategory") String eventTargetCategoryJson,
	        @ModelAttribute EventDTO eventDTO
	) {
	    try {
	    	// JSON 문자열을 객체로 변환할 때 사용하는 Jackson 라이브러리 도구
	        ObjectMapper mapper = new ObjectMapper();
	        
            // JSON 문자열 → JsonNode 객체로 변환
	        // DTO 필드 타입이 JsonNode라서 변환 필요
	        eventDTO.setEventTargetAccount(mapper.readTree(eventTargetAccountJson));
	        eventDTO.setEventTargetCategory(mapper.readTree(eventTargetCategoryJson));


	        // 실제 파일을 저장할 서버 경로
	        // resourcePath = application.properties에서 가져온 값
	        // 예: C:/HUR/workspace/Ornably/resource
	        String uploadDir = resourcePath + "/images/event/";
	        
	        // 해당 폴더가 없으면 생성
	        File dir = new File(uploadDir);
	        if (!dir.exists()) dir.mkdirs();
	        
	        // 파일 이름 중복 방지를 위해 UUID 사용
	        // UUID = 랜덤 고유 문자열
	        String fileName = UUID.randomUUID() + "_" + eventImage.getOriginalFilename();
	        

	        // 저장될 실제 파일 경로 생성	        
	        File dest = new File(uploadDir, fileName);

	        // 업로드된 파일을 서버 디스크에 저장
	        // MultipartFile → 실제 파일로 변환
	        eventImage.transferTo(dest);

	        //  DB에 저장할 URL
	        eventDTO.setEventImageUrl(eventPrefix + fileName); // eventPrefix="/images/event/"

	        eventDTO.setCondition("INSERT_EVENT");

	        if (!eventService.insertEvent(eventDTO)) {
	            return ResponseEntity.status(400).body(Map.of(
	                    "code", "EVENT_INSERT_FAIL",
	                    "message", "이벤트 등록에 실패했습니다."
	            ));
	        }
	        
	        // INSERT 성공
	        return ResponseEntity.ok(Map.of(
	                "code", "success",
	                "message", "이벤트 등록 성공",
	                "eventPk", eventDTO.getEventPk()
	        ));
	    } catch (Exception e) { // JSON 변환 실패 / 파일 저장 실패 시 예외 처리
	        return ResponseEntity.status(400).body(Map.of(
	                "code", "BAD_REQUEST",
	                "message", "요청 데이터가 올바르지 않습니다."
	        ));
	    }
	}
	
}
