package bugsandwich.ornably.account.service;

import java.util.List;

import bugsandwich.ornably.account.AccountDTO;
import bugsandwich.ornably.address.AddressDTO;

public interface AccountService {

	boolean registAccount(AccountDTO accountDTO, AddressDTO addressDTO);

	boolean checkIdDuplicate(AccountDTO accountDTO);

	AccountDTO getMyPageData(AccountDTO accountDTO);

	boolean accountWithdraw(AccountDTO accountDTO);

	List<AccountDTO> getAdminSearchAccount(AccountDTO accountDTO);
	List<AccountDTO> getEmailDatas();
	
	AccountDTO getAdminAccountInfo(Integer accountPk);
	
}
