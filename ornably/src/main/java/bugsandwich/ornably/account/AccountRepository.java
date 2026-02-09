package bugsandwich.ornably.account;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {
	@Autowired // 의존주입
    private JdbcTemplate jdbcTemplate;

	// 로그인
	private static final String LOGIN =
		    "SELECT " +
		    "ACCOUNT_PK   AS accountPk, " +
		    "ACCOUNT_ID   AS accountId, " +
		    "ACCOUNT_NAME AS accountName, " +
		    "ACCOUNT_ROLE AS accountRole, " +
		    "ACCOUNT_PASSWORD_HASH AS accountPasswordHash " +
		    "FROM ACCOUNT " +
		    "WHERE ACCOUNT_ID = ? ";


	// 회원가입
	private static final String ACCOUNT_JOIN = 
		    "INSERT INTO ACCOUNT (ACCOUNT_ID, ACCOUNT_PASSWORD_HASH, ACCOUNT_NAME, ACCOUNT_EMAIL, ACCOUNT_PHONE, ACCOUNT_EVENT_OPT_IN, ACCOUNT_ROLE) " +
		    "VALUES (?, ?, ?, ?, ?, ?, ?)";


	// 회원 탈퇴
	private static final String UPDATE_SIGN_OUT = 
		    "UPDATE ACCOUNT " +
		    "SET ACCOUNT_ID = NULL " + // ID만 NULL로 변경 나머지는 보존
		    "WHERE ACCOUNT_PK = ?";

	// 탈퇴 전 비밀번호 확인
	private static final String SELECT_CHECK_PASSWORD_BY_PK =
		    "SELECT " +
		    "ACCOUNT_PASSWORD_HASH AS accountPasswordHash " +
		    "FROM ACCOUNT " +
		    "WHERE ACCOUNT_PK = ? ";

	// 마이페이지 조회
	private static final String SELECT_MY_PAGE =
		    "SELECT " +
		    "a.ACCOUNT_ID    AS accountId, " +
		    "a.ACCOUNT_NAME  AS accountName, " +
		    "a.ACCOUNT_EMAIL AS accountEmail, " +
		    "a.ACCOUNT_PHONE AS accountPhone, " +
		    "a.ACCOUNT_DATE  AS accountDate, " +
		    "IFNULL(SUM(oi.ORDERS_ITEM_COUNT * oi.ORDERS_ITEM_PRICE), 0) AS accountTotalAmount " +
		    "FROM ACCOUNT a " +
		    "LEFT JOIN ORDERS o ON a.ACCOUNT_PK = o.ACCOUNT_PK " +
		    "LEFT JOIN ORDERS_ITEM oi ON o.ORDERS_PK = oi.ORDERS_PK " +
		    "WHERE a.ACCOUNT_PK = ? " +
		    "GROUP BY a.ACCOUNT_ID, a.ACCOUNT_NAME, a.ACCOUNT_EMAIL, a.ACCOUNT_PHONE, a.ACCOUNT_DATE";


	// 아이디 중복 확인
	private static final String SELECT_CHECK_LOGIN_ID = 
		    "SELECT COUNT(*) " +
		    "FROM ACCOUNT " +
		    "WHERE ACCOUNT_ID = ?";

	// 폰번호 중복 확인
	private static final String SELECT_CHECK_LOGIN_PHONE = 
		    "SELECT COUNT(*) " +
		    "FROM ACCOUNT " +
		    "WHERE ACCOUNT_PHONE = ?";
	
	// 회원 아이디로 회원 PK 찾기
	private static final String SELECT_ACCOUNT_PK_BY_ACCOUNT_ID =
		    "SELECT ACCOUNT_PK AS accountPk " +
		    "FROM ACCOUNT " +
		    "WHERE ACCOUNT_ID = ?";

	// 계정 1명 조회
	private static final String SELECT_ORNABLY_USER_BY_ACCOUNT_ID =
		    "SELECT " +
		    "    ACCOUNT_PK 	AS accountPk, " +
		    "    ACCOUNT_NAME 	AS accountName, " +
		    "    ACCOUNT_ID 	AS accountId, " +
		    "    ACCOUNT_PASSWORD_HASH AS accountPasswordHash, " +
		    "    ACCOUNT_ROLE 	AS accountRole " +
		    "FROM ACCOUNT " +
		    "WHERE ACCOUNT_ID = ?";

	
	
    // ==============
 	//   관리자 쿼리문
 	// ==============
 	
	// 관리자 회원 검색
	private static final String SELECT_ALL_ROLE_USER_ACCOUNT_BY_ADMIN_SEARCH =
	    "SELECT " +
	    "    a.ACCOUNT_PK   AS accountPk, " +
	    "    a.ACCOUNT_NAME AS accountName, " +
	    "    a.ACCOUNT_DATE AS accountDate, " +
	    "    a.ACCOUNT_ROLE AS accountRole, " +
	    "    IFNULL(SUM(oi.ORDERS_ITEM_COUNT * oi.ORDERS_ITEM_PRICE), 0) AS accountTotalAmount " +
	    "FROM ACCOUNT a " +
	    "LEFT JOIN ORDERS o ON a.ACCOUNT_PK = o.ACCOUNT_PK " +
	    "LEFT JOIN ORDERS_ITEM oi ON o.ORDERS_PK = oi.ORDERS_PK " +
	    "WHERE ( ? IS NULL OR a.ACCOUNT_PK = ? ) " +
	    "  AND ( ? IS NULL OR a.ACCOUNT_NAME LIKE CONCAT('%', ?, '%') ) " +
	    "  AND ( ? IS NULL OR a.ACCOUNT_DATE >= ? ) " +
	    "  AND ( ? IS NULL OR a.ACCOUNT_DATE <= ? ) " +
	    "  AND ( ? IS NULL OR a.ACCOUNT_ROLE = ? ) " +
	    "GROUP BY a.ACCOUNT_PK, a.ACCOUNT_NAME, a.ACCOUNT_DATE, a.ACCOUNT_ROLE " +
	    
	    // 총 구매금액으로 범위 지정
	    "HAVING ( ? IS NULL OR accountTotalAmount >= ? ) " +
	    "   AND ( ? IS NULL OR accountTotalAmount <= ? ) " +
	    "ORDER BY a.ACCOUNT_DATE DESC";


	// 관리자 회원 정보 조회
	private static final String SELECT_ADMIN_ACCOUNT_INFO_BY_ACCOUNT_PK =
	    "SELECT " +
	    "    a.ACCOUNT_PK           AS accountPk, " +
	    "    a.ACCOUNT_ID           AS accountId, " +
	    "    a.ACCOUNT_NAME         AS accountName, " +
	    "    a.ACCOUNT_DATE         AS accountDate, " +
	    "    a.ACCOUNT_ROLE         AS accountRole, " +
	    "    a.ACCOUNT_EVENT_OPT_IN AS accountEventOptIn, " +
	    "    IFNULL(SUM(oi.ORDERS_ITEM_COUNT * oi.ORDERS_ITEM_PRICE), 0) AS accountTotalAmount " +
	    "FROM ACCOUNT a " +
	    "LEFT JOIN ORDERS o ON a.ACCOUNT_PK = o.ACCOUNT_PK " +
	    "LEFT JOIN ORDERS_ITEM oi ON o.ORDERS_PK = oi.ORDERS_PK " +
	    "WHERE a.ACCOUNT_PK = ? " +
	    "GROUP BY " +
	    "    a.ACCOUNT_PK, " +
	    "    a.ACCOUNT_ID, " +
	    "    a.ACCOUNT_NAME, " +
	    "    a.ACCOUNT_DATE, " +
	    "    a.ACCOUNT_ROLE, " +
	    "    a.ACCOUNT_EVENT_OPT_IN";

	
	
	
	
	
	
	
	public List<AccountDTO> selectAll(AccountDTO accountDTO){
		System.out.println("[로그] AccountRepository의 selectAll 시작");
		
		if("SELECT_ALL_ROLE_USER_ACCOUNT_BY_ADMIN_SEARCH".equals(accountDTO.getCondition())) {
			System.out.println("[로그] selectAll의 SELECT_ALL_ROLE_USER_ACCOUNT_BY_ADMIN_SEARCH");
			return jdbcTemplate.query(
				SELECT_ALL_ROLE_USER_ACCOUNT_BY_ADMIN_SEARCH,
				new BeanPropertyRowMapper<>(AccountDTO.class),

				// WHERE ACCOUNT_PK
				accountDTO.getAccountPk(),
				accountDTO.getAccountPk(),

				// WHERE ACCOUNT_NAME
				accountDTO.getAccountName(),
				accountDTO.getAccountName(),

				// WHERE ACCOUNT_DATE >=
				accountDTO.getAccountJoinStartDate(),
				accountDTO.getAccountJoinStartDate(),

				// WHERE ACCOUNT_DATE <=
				accountDTO.getAccountJoinEndDate(),
				accountDTO.getAccountJoinEndDate(),

				// WHERE ACCOUNT_ROLE
				accountDTO.getAccountRole(),
				accountDTO.getAccountRole(),

				// HAVING accountTotalAmount >=
				accountDTO.getAccountTotalAmountMin(),
				accountDTO.getAccountTotalAmountMin(),

				// HAVING accountTotalAmount <=
				accountDTO.getAccountTotalAmountMax(),
				accountDTO.getAccountTotalAmountMax()
			);
		}
		System.out.println("[로그][경고] AccountDAO의 selectAll_condition 없음");
        return null;
	}
	
	
    public AccountDTO selectOne(AccountDTO accountDTO) {
		System.out.println("[로그] AccountRepository의 selectOne 시작");
		
    	// 마이페이지 조회
        if ("SELECT_MY_PAGE".equals(accountDTO.getCondition())) {
    		System.out.println("[로그] selectOne의 SELECT_MY_PAGE");
    		
    		return jdbcTemplate.queryForObject(
        		SELECT_MY_PAGE,
    		    new BeanPropertyRowMapper<>(AccountDTO.class),
    		    accountDTO.getAccountPk()
    		);
        } 
        
        // 탈퇴 전 비밀번호 확인
        else if ("SELECT_CHECK_PASSWORD_BY_PK".equals(accountDTO.getCondition())) {
    		System.out.println("[로그] selectOne의 SELECT_CHECK_PASSWORD_BY_PK");
    		
    		return jdbcTemplate.queryForObject(
        		SELECT_CHECK_PASSWORD_BY_PK,
        		new BeanPropertyRowMapper<>(AccountDTO.class), 
                accountDTO.getAccountPk()
            );
        } 
        
        // 로그인
        else if ("LOGIN".equals(accountDTO.getCondition())) {
    		System.out.println("[로그] selectOne의 LOGIN");
    		
    		return jdbcTemplate.queryForObject(
        		LOGIN,
        		new BeanPropertyRowMapper<>(AccountDTO.class), 
                accountDTO.getAccountId()
            );
        } 
        
        // 아이디 중복 확인
        else if ("SELECT_CHECK_LOGIN_ID".equals(accountDTO.getCondition())) {
    		System.out.println("[로그] selectOne의 SELECT_CHECK_LOGIN_ID");
    		
        	Integer result = jdbcTemplate.queryForObject(
        		SELECT_CHECK_LOGIN_ID,
        		Integer.class,
        	    accountDTO.getAccountId()
        	);
        	return (result != null && result > 0) ? new AccountDTO() : null;
        } 
        
        // 폰 번호 중복 확인
        else if ("SELECT_CHECK_LOGIN_PHONE".equals(accountDTO.getCondition())) {
    		System.out.println("[로그] selectOne의 SELECT_CHECK_LOGIN_PHONE");
    		
        	Integer result = jdbcTemplate.queryForObject(
        		SELECT_CHECK_LOGIN_PHONE,
        		Integer.class,
        	    accountDTO.getAccountPhone()
        	);
        	return (result != null && result > 0) ? new AccountDTO() : null;
        } 
        
        // 아이디로 PK 조회 (주문, 장바구니, 주소 등 FK 연결용)
        else if ("SELECT_ACCOUNT_PK_BY_ACCOUNT_ID".equals(accountDTO.getCondition())) {
    		System.out.println("[로그] selectOne의 SELECT_ACCOUNT_PK_BY_ACCOUNT_ID");
    		

        	List<AccountDTO> list = jdbcTemplate.query(
        		SELECT_ACCOUNT_PK_BY_ACCOUNT_ID,
        		new BeanPropertyRowMapper<>(AccountDTO.class),
                accountDTO.getAccountId()
            );
        	return list.isEmpty() ? null : list.get(0);
        }
        
        // 계정 1명 조회
        else if("SELECT_ORNABLY_USER_BY_ACCOUNT_ID".equals(accountDTO.getCondition())) {
        	System.out.println("[로그] selectOne의 SELECT_ORNABLY_USER_BY_ACCOUNT_ID");
    		
        	return jdbcTemplate.queryForObject(
       			SELECT_ORNABLY_USER_BY_ACCOUNT_ID,
    			new BeanPropertyRowMapper<>(AccountDTO.class),
    			accountDTO.getAccountId()
    		);
        }
        
        // 관리자 회원 정보 조회
        else if("SELECT_ADMIN_ACCOUNT_INFO_BY_ACCOUNT_PK".equals(accountDTO.getCondition())) {
        	System.out.println("[로그] selectOne의 SELECT_ADMIN_ACCOUNT_INFO_BY_ACCOUNT_PK");
    		
        	return jdbcTemplate.queryForObject(
    			SELECT_ADMIN_ACCOUNT_INFO_BY_ACCOUNT_PK,
    			new BeanPropertyRowMapper<>(AccountDTO.class),
    			accountDTO.getAccountPk()
        	);
        }
		System.out.println("[로그][경고] AccountDAO의 selectOne_condition 없음");
        return null;
    }
    

    public boolean insert(AccountDTO accountDTO) {
		System.out.println("[로그] AccountRepository의 insert 시작");
    	int result = 0;
    	
    	// 회원 가입
		if("ACCOUNT_JOIN".equals(accountDTO.getCondition())) {
        	System.out.println("[로그] insert의 ACCOUNT_JOIN");
			result = jdbcTemplate.update(
				ACCOUNT_JOIN,
			    accountDTO.getAccountId(),
			    accountDTO.getAccountPasswordHash(),
			    accountDTO.getAccountName(),
			    accountDTO.getAccountEmail(),
			    accountDTO.getAccountPhone(),
			    accountDTO.getAccountEventOptIn() ? 1 : 0,
			    accountDTO.getAccountRole()
			);
		}
		else {
        	System.out.println("[로그][경고] AccountRepository_insert_condition 없음");
        }
        return result > 0;
    }
    

    public boolean update(AccountDTO accountDTO) {
		System.out.println("[로그] AccountRepository의 update 시작");
		int result = 0;
		
    	// 회원 탈퇴
        if ("UPDATE_SIGN_OUT".equals(accountDTO.getCondition())) {
    		System.out.println("[로그] update의 UPDATE_SIGN_OUT");
    		
             result = jdbcTemplate.update(
        		UPDATE_SIGN_OUT, 
        		accountDTO.getAccountPk()
        	);
        }
        else {
        	System.out.println("[로그][경고] AccountRepository_update_condition 없음");
        }
        return result > 0;
    }
    
    private boolean delete(AccountDTO accountDTO) {
    		return false;
    }
}