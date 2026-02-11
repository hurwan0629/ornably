package bugsandwich.ornably.connectLog;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ConnectLogRepository {
	@Autowired
	private SqlSession sqlSession;

	private static final String NAMESPACE = "ConnectLog.";
	
	public List<ConnectLogDTO> selectAll(ConnectLogDTO connectLogDTO){
		System.out.println("[로그] ConnectLogRepository의 selectAll 시작");
		
		// 특정 사용자 로그 전체 조회
		List<ConnectLogDTO> list = sqlSession.selectList(NAMESPACE + "getList", connectLogDTO);
		System.out.println("[로그] selectAll count = " + list.size());
		
		// selectList() : 조회 결과가 없으면 빈 리스트 반환
        return list;		
	}
	
	public ConnectLogDTO selectOne(ConnectLogDTO connectLogDTO) {
	    System.out.println("[로그] ConnectLogRepository selectOne 시작");

	    // 특정 사용자 로그 최신 접속 한 건 조회
	    ConnectLogDTO result = sqlSession.selectOne(NAMESPACE + "getOne", connectLogDTO);
	    if (result != null) {
	        System.out.println("[로그] selectOne 성공");
	    } 
	    else {
	        System.out.println("[로그] selectOne 결과 없음");
	    }
	    return result;
	}

	
	public boolean insert(ConnectLogDTO connectLogDTO) {
		System.out.println("[로그] ConnectLogRepository의 insert 시작");
		
		// 새 접속 기록 추가
		if(sqlSession.insert(NAMESPACE + "insert", connectLogDTO) > 0) {
			System.out.println("[로그] ConnectLogRepository insert 성공");
	        return true;
		}
		else {
			System.out.println("[로그] insert 실패");
			return false;
		}
	}
	
	private boolean update(ConnectLogDTO connectLogDTO) {
		return false;
	}
	
	public boolean delete(ConnectLogDTO connectLogDTO) {
		System.out.println("[로그] ConnectLogRepository의 delete 시작");
		
		// 사용자 로그 전체 삭제
		if(sqlSession.delete(NAMESPACE + "delete", connectLogDTO) > 0) {
			System.out.println("[로그] delete 성공");
			return true;
		}
		else {
			System.out.println("[로그] delete 실패");		
			return false;
		}
	}
}
