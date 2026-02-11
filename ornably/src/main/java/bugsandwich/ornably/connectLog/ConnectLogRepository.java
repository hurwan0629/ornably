package bugsandwich.ornably.connectLog;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConnectLogRepository {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	// 새 접속 기록 추가
	private static final String INSERT_CONNECT_LOG = 
			"INSERT INTO CONNECT_LOG (ACCOUNT_PK, CONNECT_IP, CONNECT_DEVICE) " +
		    "VALUES (?, ?, ?)";
	
	// 특정 사용자 로그 전체 조회
	private static final String SELECT_ALL_ACCOUNT_CONNECT_LOG = 
		    "SELECT * " +
		    "FROM CONNECT_LOG " +
		    "WHERE ACCOUNT_PK = ? " +
		    "ORDER BY CONNECT_DATE DESC";
	
	// 특정 사용자 로그 최신 접속 1건 조회
	private static final String SELECT_LATEST_ACCOUNT_CONNECT_LOG = 
		    "SELECT * FROM CONNECT_LOG " +
		    "WHERE ACCOUNT_PK = ? " +
		    "ORDER BY CONNECT_DATE DESC " +
		    "LIMIT 1";

	
	// 사용자 로그 전체 삭제
	private static final String DELETE_ACCOUNT_CONNECT_LOG = 
		    "DELETE FROM CONNECT_LOG " +
		    "WHERE ACCOUNT_PK = ?";

	
	public List<ConnectLogDTO> selectAll(ConnectLogDTO connectLogDTO){
		System.out.println("[로그] ConnectLogRepository의 selectAll 시작");
		
		// 특정 사용자 로그 전체 조회
		if("SELECT_ACCOUNT_CONNECT_LOG".equals(connectLogDTO.getCondition())) {
	        System.out.println("[로그] selectAll의 SELECT_ACCOUNT_CONNECT_LOG");
	        return jdbcTemplate.query(
	            SELECT_ALL_ACCOUNT_CONNECT_LOG,
	            new BeanPropertyRowMapper<>(ConnectLogDTO.class),
	            connectLogDTO.getAccountPk()
	        );
	    }
		System.out.println("[로그][경고] ConnectLogRepository_selectAll_condition 없음");
		// 조건이 없으면 빈 리스트 반환
	    return java.util.Collections.emptyList();
	}
	
	public ConnectLogDTO selectOne(ConnectLogDTO connectLogDTO) {
		System.out.println("[로그] ConnectLogRepository의 selectOne 시작");
		
		// 특정 사용자 로그 최신 접속 1건 조회
		if("SELECT_LATEST_CONNECT_LOG".equals(connectLogDTO.getCondition())) {
	        System.out.println("[로그] selectOne의 SELECT_LATEST_CONNECT_LOG");
	        return jdbcTemplate.queryForObject(
	            SELECT_LATEST_ACCOUNT_CONNECT_LOG,
	            new BeanPropertyRowMapper<>(ConnectLogDTO.class),
	            connectLogDTO.getAccountPk()
	        );
	    }
		System.out.println("[로그][경고] ConnectLogRepository_selectOne_condition 없음");
		return null;
	}
	
	public boolean insert(ConnectLogDTO connectLogDTO) {
		System.out.println("[로그] ConnectLogRepository의 insert 시작");
		int result = 0;
		
		// 새 접속 기록 추가
		if("INSERT_CONNECT_LOG".equals(connectLogDTO.getCondition())) {
			System.out.println("[로그] insert의 INSERT_CONNECT_LOG");
			result = jdbcTemplate.update(
				INSERT_CONNECT_LOG,
				connectLogDTO.getAccountPk(),
				connectLogDTO.getConnectIP(),
				connectLogDTO.getConnectDevice()
			);
		}
		else {
			System.out.println("[로그][경고] ConnectLogRepository_insert_condition 없음");
		}
		return result > 0;
	}
	
	private boolean update(ConnectLogDTO connectLogDTO) {
		return false;
	}
	
	public boolean delete(ConnectLogDTO connectLogDTO) {
		System.out.println("[로그] ConnectLogRepository의 delete 시작");
		int result = 0;
		
		// 사용자 로그 전체 삭제
		if("DELETE_ACCOUNT_CONNECT_LOG".equals(connectLogDTO.getCondition())) {
			System.out.println("[로그] delete의 DELETE_ACCOUNT_CONNECT_LOG");
			result = jdbcTemplate.update(
				DELETE_ACCOUNT_CONNECT_LOG,
				connectLogDTO.getAccountPk()
			);
		}

		System.out.println("[로그][경고] ConnectLogRepository_delete_condition 없음");
		return result > 0;
	}
}
