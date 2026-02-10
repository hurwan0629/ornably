package bugsandwich.ornably.event.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bugsandwich.ornably.event.EventDTO;
import bugsandwich.ornably.event.EventRepository;

@Service
public class EventServiceImpl implements EventService{
	
	@Autowired
	private EventRepository eventRepository;
	
	@Override
	public boolean insertEvent(EventDTO eventDTO) {
		return eventRepository.insert(eventDTO);
	}

	@Override
	public boolean updateEvent(EventDTO eventDTO) {
		return eventRepository.update(eventDTO);
	}

	@Override
	public boolean deleteEvent(EventDTO eventDTO) {
		return eventRepository.delete(eventDTO);
	}

	@Override
	public EventDTO getEvent(EventDTO eventDTO) {
		return eventRepository.selectOne(eventDTO);
	}

	@Override
	public List<EventDTO> getEventList(EventDTO eventDTO) {
		return eventRepository.selectAll(eventDTO);
	}
}
