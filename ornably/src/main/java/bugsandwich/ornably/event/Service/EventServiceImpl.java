package bugsandwich.ornably.event.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import bugsandwich.ornably.event.EventDTO;

@Service
public class EventServiceImpl implements EventService{

	@Override
	public boolean insertEvent(EventDTO eventDTO) {
		return false;
	}

	@Override
	public boolean updateEvent(EventDTO eventDTO) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteEvent(EventDTO eventDTO) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EventDTO getEvent(EventDTO eventDTO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EventDTO> getEventList(EventDTO eventDTO) {
		// TODO Auto-generated method stub
		return null;
	}

}
