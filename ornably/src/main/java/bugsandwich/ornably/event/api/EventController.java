package bugsandwich.ornably.event.api;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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

import bugsandwich.ornably.account.AccountDTO;
import bugsandwich.ornably.account.service.AccountService;
import bugsandwich.ornably.brevo.BrevoClient;
import bugsandwich.ornably.brevo.BrevoService;
import bugsandwich.ornably.event.EventDTO;
import bugsandwich.ornably.event.Service.EventService;
import bugsandwich.ornably.review.service.ReviewService;

@RestController
@RequestMapping("/api")
public class EventController {
	
	@Autowired
	private BrevoClient brevoClient;
	@Autowired
	private BrevoService brevoService;
	
	@Autowired
	private EventService eventService;
	@Autowired
	private ReviewService reviewService;
	@Autowired
	private AccountService accountService;
	
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
	            "eventEndDate", LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
	    ));
	}
	
	
	
//  ===================== 현재 진행중인 이벤트 =====================
	@GetMapping("/all/event/in-progress")
	public ResponseEntity<?> mainEvent(EventDTO eventDTO){
		
		eventDTO.setCondition("SELECT_ALL_PROGRESS_EVENT");
	    List<EventDTO> list = eventService.getEventList(eventDTO);

	    // 이벤트 없을 때
	    if (list==null || list.isEmpty()) {
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
//           @RequestPart("eventTargetAccount") String eventTargetAccountJson,
//           @RequestPart("eventTargetCategory") String eventTargetCategoryJson,
           @ModelAttribute EventDTO eventDTO
   ) {
          try {
              if(eventImage == null || eventImage.isEmpty()) {
                 return ResponseEntity.status(400).body(Map.of(
                          "code", "MISSING_IMAGE",
                          "message", "이미지를 불러오지 못하였습니다."
                  ));
              }
              else if(!this.reviewService.checkFileSize(eventImage)) {
                 return ResponseEntity.status(400).body(Map.of(
                          "code", "TOO_BIG_IMAGE_SIZE",
                          "message", "이미지 크기는 "+ this.reviewService.getAllowedImageMaxBytes() +"kb까지 가능합니다."
                  ));
              }
              else if(!this.reviewService.checkFileExtention(eventImage)) {
                 return ResponseEntity.status(400).body(Map.of(
                          "code", "IMAGE_TYPE_ERROR",
                          "message", "이미지 확장자는"+ this.reviewService.getAllowedExtentionSet() +"만 가능합니다."
                  ));   
              }
              
              String eventImageUrl = this.reviewService.saveImageAndGetUrl(resourcePath, eventPrefix, eventImage);
              
               //  DB에 저장할 URL
               eventDTO.setEventImageUrl(eventImageUrl); // eventPrefix="/images/event/"
               
               eventDTO.setCondition("INSERT_EVENT");
               if (!eventService.insertEvent(eventDTO)) {
                   return ResponseEntity.status(400).body(Map.of(
                           "code", "EVENT_INSERT_FAIL",
                           "message", "이벤트 등록에 실패했습니다."
                   ));
               }
           
               //  테스트 메일 발송 (실패해도 이벤트 등록은 성공 처리)
               try {
                  List<AccountDTO> emails = accountService.getEmailDatas();
                  brevoService.sendEventMailToAllAgreeAsync(emails, eventDTO);
               } catch (Exception e) {
                  e.printStackTrace();
               }      
               
               // eventPk 호출
               eventDTO.setCondition("SELECT_ONE_EVENT_PK_RECENT");           
               eventDTO = eventService.getEvent(eventDTO);
           
           // INSERT 성공 및 이메일 발송 성공
           return ResponseEntity.ok(Map.of(
                   "code", "success",
                   "message", "이벤트 등록 성공",
                   "eventPk", eventDTO.getEventPk()
           ));
           
       } catch (Exception e) { // JSON 변환 실패 / 파일 저장 실패 시 예외 처리
    	   e.printStackTrace();
           return ResponseEntity.status(400).body(Map.of(
                   "code", "BAD_REQUEST",
                   "message", "요청 데이터가 올바르지 않습니다."
           ));
       }
   }	
}
