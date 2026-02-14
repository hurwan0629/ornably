package bugsandwich.ornably.account.service;

import java.util.List;

import bugsandwich.ornably.account.AccountDTO;
import bugsandwich.ornably.address.AddressDTO;

public interface AccountService {

	boolean registAccount(AccountDTO accountDTO, AddressDTO addressDTO); //회원정보등록

	boolean checkIdDuplicate(AccountDTO accountDTO); //아이디 중복체크

	AccountDTO getMyPageData(AccountDTO accountDTO);//마이페이지 데이터 가져오기

	boolean accountWithdraw(AccountDTO accountDTO); //회원탈퇴

	List<AccountDTO> getAdminSearchAccount(AccountDTO accountDTO);
	List<AccountDTO> getEmailDatas();
	
	AccountDTO getAdminAccountInfo(Integer accountPk);
	
}
