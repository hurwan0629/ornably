package bugsandwich.ornably.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import bugSandwich.ornably.account.AccountDTO;

@Repository
public class AccountRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public boolean checkIdAndPassword(AccountDTO accountDTO) {
		String sql = """
				SELECT COUNT(*)
				FROM ACCOUNT
				WHERE ACCOUNT_ID=? AND ACCOUNT_PASSWORD
				""";
		return jdbcTemplate.queryForObject(sql, Integer.class, accountDTO.getAccountId(), accountDTO.getAccountPasswordHash())>0;
	}
	
	// ACCOUNT_ID로 계정 조회 (Optional 버전)
	public AccountDTO findByAccountId(String accountId) {
		System.out.println(accountId);
		String sql = """
				    SELECT *
				    FROM ACCOUNT
				    WHERE ACCOUNT_ID = ?
				""";

		try {
			AccountDTO account = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(AccountDTO.class),
					accountId);
			return account;
		} catch (Exception e) {
			System.out.println("에러 발생");
			return null;
		}
	}

	public boolean insertAccount(AccountDTO accountDTO) {
		String sql = "INSERT INTO account (" + "  ACCOUNT_ID, " + "  ACCOUNT_PASSWORD_HASH, " + "  ACCOUNT_NAME, "
				+ "  ACCOUNT_EMAIL, " + "  ACCOUNT_PHONE, " + "  ACCOUNT_ROLE " + ") VALUES (?, ?, ?, ?, ?, ?)";
		boolean flag = jdbcTemplate.update(sql, accountDTO.getAccountId(), accountDTO.getAccountPasswordHash(),
				accountDTO.getAccountName(), accountDTO.getAccountEmail(), accountDTO.getAccountPhone(),
				accountDTO.getAccountRole()) == 1;
		return flag;
	}
}