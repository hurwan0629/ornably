package bugsandwich.ornably.event;

import java.util.List;

public interface EventService {
	boolean insertEvent(EventDTO dto);
	boolean updateEvent(EventDTO dto);
	boolean deleteEvent(EventDTO dto);
	
	EventDTO selectOne(EventDTO dto);
	List<EventDTO> selectAll(EventDTO dto);          	 // 전체 이벤트 조회
	List<EventDTO> selectAllProgressEvent(EventDTO dto); // 진행중 이벤트 조회
}