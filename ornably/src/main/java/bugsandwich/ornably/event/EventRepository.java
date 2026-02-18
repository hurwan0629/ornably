package bugsandwich.ornably.event;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepository {
	@Autowired // 의존 주입
	private SqlSession sqlSession;
	private static final String NAMESPACE = "EventLog.";

	
	public List<EventDTO> selectAll(EventDTO eventDTO){
	    System.out.println("[로그] EventRepository의 selectAll 시작");
	    
	    // 전체 이벤트 요청
	    List<EventDTO> list = sqlSession.selectList(NAMESPACE + "selectAll");
		System.out.println("[로그] selectAll count = " + list.size());
	        
		// selectList() : 결과 없으면 빈 리스트 반환
		return list;
	}
	
	
	public List<EventDTO> selectAllProgress(EventDTO eventDTO) {
		System.out.println("[로그] EventRepository selectAllProgress 시작");

		// 현재 진행중인 이벤트 요청
		List<EventDTO> list = sqlSession.selectList(NAMESPACE + "selectAllProgressEvent");
		System.out.println("[로그] selectAllProgress count = " + list.size());

		return list;
	}
	
	public EventDTO selectOne(EventDTO eventDTO) {
		return sqlSession.selectOne(NAMESPACE + "selectRecentEventPk");
	}
	
	public boolean insert(EventDTO eventDTO) {
	    System.out.println("[로그] EventRepository의 insert 시작");
	    
	    // 이벤트 등록
	    if (sqlSession.insert(NAMESPACE + "insert", eventDTO) > 0) {
			System.out.println("[로그] insert 성공");
			return true;
		} else {
			System.out.println("[로그] insert 실패");
			return false;
		}
	}
	
	public boolean update(EventDTO eventDTO) {
	    System.out.println("[로그] EventRepository의 update 시작");
	    
	    // 이벤트 종료 요청
	    if (sqlSession.update(NAMESPACE + "update", eventDTO) > 0) {
			System.out.println("[로그] update 성공");
			return true;
		} else {
			System.out.println("[로그] update 실패");
			return false;
		}
	}
	
	private boolean delete(EventDTO eventDTO) {
		return false;
	}
}