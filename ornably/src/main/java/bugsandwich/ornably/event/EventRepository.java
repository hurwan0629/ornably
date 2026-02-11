package bugsandwich.ornably.event;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepository {
	@Autowired // 의존 주입
	private JdbcTemplate jdbcTemplate;
	
	// 이벤트 등록
	private static final String INSERT_EVENT =
			"INSERT INTO EVENT " +
            "(EVENT_NAME, EVENT_IMAGE, EVENT_START_DATE, EVENT_END_DATE, " +
            "EVENT_TARGET_ACCOUNT, EVENT_TARGET_CATEGORY, EVENT_DISCOUNT_RATE, EVENT_DESCRIPTION) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	
	// 이벤트 종료 요청 (종료 날짜 > 종료 요청한 날짜)
	private static final String UPDATE_END_EVENT =
			"UPDATE EVENT " +			
			"SET EVENT_END_DATE = NOW() " + // 종료 요청 시 현재 시각으로 종료
			"WHERE EVENT_PK = ?";
	

	// 전체 이벤트 요청
	private static final String SELECT_ALL_EVENT =
	        "SELECT " +
	        "EVENT_PK         AS eventPk, " +                 	// 이벤트 고유 번호
	        "EVENT_NAME       AS eventName, " +               	// 이벤트 이름
	        "EVENT_START_DATE AS eventStartDate, " +          	// 이벤트 시작일
	        "EVENT_END_DATE   AS eventEndDate, " +            	// 이벤트 종료일
	        "EVENT_TARGET_ACCOUNT  AS eventTargetAccount, " + 	// 이벤트 대상 계정
	        "EVENT_TARGET_CATEGORY AS eventTargetCategory, " +	// 이벤트 적용 카테고리
	        "EVENT_DISCOUNT_RATE   AS eventDiscountRate, " +   	// 할인율
	        "EVENT_DESCRIPTION     AS eventDescription " +     	// 이벤트 설명
	        "FROM EVENT " +
	        "ORDER BY EVENT_START_DATE DESC"; // 시작일 기준 내림차순 (최근 이벤트 먼저)

	
	// 현재 진행중인 이벤트 요청
	private static final String SELECT_ALL_PROGRESS_EVENT =
	        "SELECT " +
	        "EVENT_PK       AS eventPk, " +                  // 이벤트 고유 번호
	        "EVENT_NAME     AS eventName, " +                // 이벤트 이름
	        "EVENT_IMAGE    AS eventImageUrl, " +            // 이벤트 이미지
	        "EVENT_START_DATE    AS eventStartDate, " +      // 이벤트 시작일
	        "EVENT_END_DATE      AS eventEndDate, " +        // 이벤트 종료일
	        "EVENT_DESCRIPTION   AS eventDescription, " +    // 이벤트 설명
	        "EVENT_DISCOUNT_RATE AS eventDiscountRate " +    // 할인율
	        "FROM EVENT " +
	        "WHERE EVENT_START_DATE <= NOW() " +             // 현재 진행중인 이벤트 필터 (시작일 <= 현재 시각)
	        "  AND EVENT_END_DATE >= NOW() " +               // 현재 진행중인 이벤트 필터 (종료일 >= 현재 시각)
	        "ORDER BY EVENT_START_DATE ASC";                 // 시작일 기준 오름차순 (오래된 이벤트 먼저)
	
	

	
	public List<EventDTO> selectAll(EventDTO eventDTO){
	    System.out.println("[로그] EventRepository의 selectAll 시작");
	    
	    // 전체 이벤트 요청
	    if("SELECT_ALL_EVENT".equals(eventDTO.getCondition())) {
		    System.out.println("[로그] selectAll의 SELECT_ALL_EVENT");
		    return jdbcTemplate.query(
		    	SELECT_ALL_EVENT, 
	    		new BeanPropertyRowMapper<>(EventDTO.class)
	    	);
	    }
	    
	    // 현재 진행중인 이벤트 요청
	    else if("SELECT_ALL_PROGRESS_EVENT".equals(eventDTO.getCondition())) {
	    	System.out.println("[로그] selectAll의 SELECT_ALL_EVENT");
		    return jdbcTemplate.query(
		    	SELECT_ALL_PROGRESS_EVENT, 
	    		new BeanPropertyRowMapper<>(EventDTO.class)
	    	);
	    }
		System.out.println("[로그][경고] EventRepository_selectAll_condition 없음");
		return null;
	}
	
	private EventDTO selectOne(EventDTO eventDTO) {
		return null;
	}
	
	public boolean insert(EventDTO eventDTO) {
	    System.out.println("[로그] EventRepository의 insert 시작");
	    int result = 0;
	    
	    // 이벤트 등록
	    if("INSERT_EVENT".equals(eventDTO.getCondition())) {
	    	System.out.println("[로그] insert의 INSERT_EVENT");
	    	result = jdbcTemplate.update(
	    		INSERT_EVENT,
	    		eventDTO.getEventName(),
	    		eventDTO.getEventImageUrl(),
	    		Date.valueOf(eventDTO.getEventStartDate()), // DATE로 타입 변환
	            Date.valueOf(eventDTO.getEventEndDate()),	// DATE로 타입 변환
	            eventDTO.getEventTargetAccount(),
	            eventDTO.getEventTargetCategory(),
	            eventDTO.getEventDiscountRate(),
	            eventDTO.getEventDescription()
	    	);
	    }
	    else {
			System.out.println("[로그][경고] EventRepository_insert_condition 없음");
		}
	    return result > 0;
	}
	
	public boolean update(EventDTO eventDTO) {
	    System.out.println("[로그] EventRepository의 update 시작");
	    int result = 0;
	    
	    if("UPDATE_END_EVENT".equals(eventDTO.getCondition())) {
	    	System.out.println("[로그] update의 UPDATE_END_EVENT");
	    	result = jdbcTemplate.update(
    			UPDATE_END_EVENT,
    			eventDTO.getEventPk()
	    	);
	    } 
	    else {
	    	System.out.println("[로그][경고] EventRepository_update_condition 없음");
	    }
		return result > 0;
	}
	
	private boolean delete(EventDTO eventDTO) {
		return false;
	}
}