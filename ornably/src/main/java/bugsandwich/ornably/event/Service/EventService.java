package bugsandwich.ornably.event.Service;

import java.util.List;

import bugsandwich.ornably.event.EventDTO;


public interface EventService {
	boolean insertEvent(EventDTO eventDTO);
	boolean updateEvent(EventDTO eventDTO);
	Boolean deleteEvent(EventDTO eventDTO);
	
	EventDTO getEvent(EventDTO eventDTO);
	List<EventDTO> getEventList(EventDTO eventDTO);
}
