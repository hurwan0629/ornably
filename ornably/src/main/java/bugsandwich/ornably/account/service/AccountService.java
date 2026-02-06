package bugsandwich.ornably.account.service;

import java.util.List;

import bugsandwich.ornably.account.AccountDTO;
import bugsandwich.ornably.address.AddressDTO;
import bugsandwich.ornably.review.ReviewDTO;

public interface AccountService {
	boolean registAccount(AccountDTO accountDTO, AddressDTO addressDTO);
	
	boolean checkIdDuplicate(AccountDTO accountDTO);
	
	public AccountDTO getMyPageData(AccountDTO accountDTO);
	
	public boolean accountWithdraw(AccountDTO accountDTO);
	
	public List<AccountDTO> getAdminSearchAccount(AccountDTO accountDTO);
	
	public AccountDTO getAdminAccountInfo(Integer accountPk);
}
