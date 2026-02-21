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
		return this.eventRepository.insert(eventDTO);
	}

	@Override
	public boolean updateEvent(EventDTO eventDTO) {
		return this.eventRepository.update(eventDTO);
	}

	@Override
	public Boolean deleteEvent(EventDTO eventDTO) {
		return null;
	}

	@Override
	public EventDTO getEvent(EventDTO eventDTO) {
		 return this.eventRepository.selectOne(eventDTO);
	}

	@Override
	public List<EventDTO> getEventList(EventDTO eventDTO) {
		if("SELECT_ALL_EVENT".equals(eventDTO.getCondition())) {			
			return this.eventRepository.selectAll(eventDTO);
		}
		else if("SELECT_ALL_PROGRESS_EVENT".equals(eventDTO.getCondition())) {
			return this.eventRepository.selectAllProgress(eventDTO);
		}
		else {
			return null;
		}
	}
}
