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
		    "VALUES (?, ?, ?, ?, ?, IFNULL(?, FALSE), ?)";


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
		    "WITH acct AS ( " +
		    "  SELECT " +
		    "    A.ACCOUNT_PK   AS accountPk, " +
		    "	 A.ACCOUNT_ID 	AS accountId, " +
		    "    A.ACCOUNT_NAME AS accountName, " +
		    "    A.ACCOUNT_DATE AS accountDate, " +
		    "    A.ACCOUNT_ROLE AS accountRole, " +
		    "    COALESCE(SUM(OI.ORDERS_ITEM_COUNT * OI.ORDERS_ITEM_PRICE), 0) AS accountTotalAmount " +
		    "  FROM ACCOUNT A " +
		    "  LEFT JOIN ORDERS O " +
		    "    ON A.ACCOUNT_PK = O.ACCOUNT_PK " +
		    "  LEFT JOIN ORDERS_ITEM OI " +
		    "    ON O.ORDERS_PK = OI.ORDERS_PK " +
		    "  WHERE A.ACCOUNT_ROLE != 'ADMIN' " +
		    "  GROUP BY " +
		    "    A.ACCOUNT_PK, A.ACCOUNT_NAME, A.ACCOUNT_DATE, A.ACCOUNT_ROLE " +
		    ") " +
		    "SELECT " +
		    "  acct.accountPk, " +
		    "  acct.accountId, " +
		    "  acct.accountName, " +
		    "  acct.accountDate, " +
		    "  acct.accountRole, " +
		    "  acct.accountTotalAmount " +
		    "FROM acct " +
		    "WHERE ( ? IS NULL OR acct.accountPk = ? ) " +
		    "  AND ( ? IS NULL OR acct.accountName LIKE CONCAT('%', ?, '%') ) " +
		    "  AND ( ? IS NULL OR acct.accountDate >= ? ) " +
		    "  AND ( ? IS NULL OR acct.accountDate <= ? ) " +
		    "  AND ( ? IS NULL OR acct.accountRole = ? ) " +
		    "  AND ( ? IS NULL OR acct.accountTotalAmount >= ? ) " +
		    "  AND ( ? IS NULL OR acct.accountTotalAmount <= ? ) " +
		    "ORDER BY acct.accountDate DESC";
	/*
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
	*/

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

	
    private static final String SELECT_ACCOUNT_EMAIL_EVENT_OPTIN =
            "SELECT a.ACCOUNT_EMAIL AS accountEmail " +
            "FROM ACCOUNT a " +
            "WHERE a.ACCOUNT_EMAIL IS NOT NULL " +      // 이메일 존재
            "AND a.ACCOUNT_ID IS NOT NULL " +           // 탈퇴 회원 제외
            "AND a.ACCOUNT_EVENT_OPT_IN = 1";           // 이벤트 수신 동의
   
   
	
	
	
	
	
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
        } else if("SELECT_ACCOUNT_EMAIL_EVENT_OPTIN".equals(accountDTO.getCondition())) {
           return jdbcTemplate.query(SELECT_ACCOUNT_EMAIL_EVENT_OPTIN, new BeanPropertyRowMapper<>(AccountDTO.class));
        }
        System.out.println("[로그][경고] AccountDAO의 selectAll_condition 없음");
          return null;
     }

	
	
    public AccountDTO selectOne(AccountDTO accountDTO) {
		
		
    	// 마이페이지 조회
        if ("SELECT_MY_PAGE".equals(accountDTO.getCondition())) {
    		
    		
    		return jdbcTemplate.queryForObject(
        		SELECT_MY_PAGE,
    		    new BeanPropertyRowMapper<>(AccountDTO.class),
    		    accountDTO.getAccountPk()
    		);
        } 
        
        // 탈퇴 전 비밀번호 확인
        else if ("SELECT_CHECK_PASSWORD_BY_PK".equals(accountDTO.getCondition())) {
    		
    		
    		return jdbcTemplate.queryForObject(
        		SELECT_CHECK_PASSWORD_BY_PK,
        		new BeanPropertyRowMapper<>(AccountDTO.class), 
                accountDTO.getAccountPk()
            );
        } 
        
        // 로그인
        else if ("LOGIN".equals(accountDTO.getCondition())) {
    		
    		
    		return jdbcTemplate.queryForObject(
        		LOGIN,
        		new BeanPropertyRowMapper<>(AccountDTO.class), 
                accountDTO.getAccountId()
            );
        } 
        
        // 아이디 중복 확인
        else if ("SELECT_CHECK_LOGIN_ID".equals(accountDTO.getCondition())) {
    		
    		
        	Integer result = jdbcTemplate.queryForObject(
        		SELECT_CHECK_LOGIN_ID,
        		Integer.class,
        	    accountDTO.getAccountId()
        	);
        	return (result != null && result > 0) ? new AccountDTO() : null;
        } 
        
        // 폰 번호 중복 확인
        else if ("SELECT_CHECK_LOGIN_PHONE".equals(accountDTO.getCondition())) {
    		
    		
        	Integer result = jdbcTemplate.queryForObject(
        		SELECT_CHECK_LOGIN_PHONE,
        		Integer.class,
        	    accountDTO.getAccountPhone()
        	);
        	return (result != null && result > 0) ? new AccountDTO() : null;
        } 
        
        // 아이디로 PK 조회 (주문, 장바구니, 주소 등 FK 연결용)
        else if ("SELECT_ACCOUNT_PK_BY_ACCOUNT_ID".equals(accountDTO.getCondition())) {
    		
    		

        	List<AccountDTO> list = jdbcTemplate.query(
        		SELECT_ACCOUNT_PK_BY_ACCOUNT_ID,
        		new BeanPropertyRowMapper<>(AccountDTO.class),
                accountDTO.getAccountId()
            );
        	return list.isEmpty() ? null : list.get(0);
        }
        
        // 계정 1명 조회
        else if("SELECT_ORNABLY_USER_BY_ACCOUNT_ID".equals(accountDTO.getCondition())) {
        	
    		
        	List<AccountDTO> list =  jdbcTemplate.query(
       			SELECT_ORNABLY_USER_BY_ACCOUNT_ID,
    			new BeanPropertyRowMapper<>(AccountDTO.class),
    			accountDTO.getAccountId()
    		);
        	return list.isEmpty() ? null : list.get(0);
        }
        
        // 관리자 회원 정보 조회
        else if("SELECT_ADMIN_ACCOUNT_INFO_BY_ACCOUNT_PK".equals(accountDTO.getCondition())) {
        	
    		
        	return jdbcTemplate.queryForObject(
    			SELECT_ADMIN_ACCOUNT_INFO_BY_ACCOUNT_PK,
    			new BeanPropertyRowMapper<>(AccountDTO.class),
    			accountDTO.getAccountPk()
        	);
        }
		
        return null;
    }
    

    public boolean insert(AccountDTO accountDTO) {
		
    	int result = 0;
    	
    	// 회원 가입
		if("ACCOUNT_JOIN".equals(accountDTO.getCondition())) {
        	
			result = jdbcTemplate.update(
				ACCOUNT_JOIN,
			    accountDTO.getAccountId(),
			    accountDTO.getAccountPasswordHash(),
			    accountDTO.getAccountName(),
			    accountDTO.getAccountEmail(),
			    accountDTO.getAccountPhone(),
			    accountDTO.getAccountEventOptIn(),
			    accountDTO.getAccountRole()
			);
		}
		else {
        	
        }
        return result > 0;
    }
    

    public boolean update(AccountDTO accountDTO) {
		
		int result = 0;
		
    	// 회원 탈퇴
        if ("UPDATE_SIGN_OUT".equals(accountDTO.getCondition())) {
    		
    		
             result = jdbcTemplate.update(
        		UPDATE_SIGN_OUT, 
        		accountDTO.getAccountPk()
        	);
        }
        else {
        	
        }
        return result > 0;
    }
    
    private boolean delete(AccountDTO accountDTO) {
    		return false;
    }
}