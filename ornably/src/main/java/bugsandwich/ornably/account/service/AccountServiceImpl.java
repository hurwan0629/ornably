package bugsandwich.ornably.account.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bugsandwich.ornably.account.AccountDTO;
import bugsandwich.ornably.account.AccountRepository;
import bugsandwich.ornably.address.AddressDTO;
import bugsandwich.ornably.address.AddressRepository;
import bugsandwich.ornably.cart.CartDTO;
import bugsandwich.ornably.cart.CartRepository;
import bugsandwich.ornably.wishlist.WishlistDTO;
import bugsandwich.ornably.wishlist.WishlistRepository;

@Service
public class AccountServiceImpl implements AccountService {

	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private AddressRepository addressRepository;
	@Autowired
	private WishlistRepository wishlistRepository;
	@Autowired
	private CartRepository cartRepository;

	@Override
	@Transactional
	public boolean registAccount(AccountDTO accountDTO, AddressDTO addressDTO) {
		
		boolean flag = true;
		
		accountDTO.setCondition("INSERT_ACCOUNT_SIGN_UP");
		flag = flag && this.accountRepository.insert(accountDTO);
		
		addressDTO.setCondition("INSERT_ACCOUNT_FIRST_ADDRESS");
		flag = flag && this.addressRepository.insert(addressDTO);

		return flag;
	}

	@Override
	public boolean checkIdDuplicate(AccountDTO accountDTO) {
		accountDTO.setCondition("SELECT_CHECK_LOGIN_ID");
		return accountRepository.selectOne(accountDTO)!=null;
	}
	
	@Override
	public AccountDTO getMyPageData(AccountDTO accountDTO) {
		accountDTO.setCondition("SELECT_MY_PAGE");
		return accountRepository.selectOne(accountDTO);
	}

	@Override
	@Transactional
	public boolean accountWithdraw(AccountDTO accountDTO) {
		// 1. 회원 주소 싹다 지우고
		AddressDTO addressDTO = new AddressDTO();
		addressDTO.setAccountPk(accountDTO.getAccountPk());
		addressDTO.setCondition("DELETE_ADDRESS_BY_ACCOUNT_PK");;
		addressRepository.delete(addressDTO);
		
		// 2. 장바구니 삭제
		CartDTO cartDTO = new CartDTO();
		cartDTO.setAccountPk(accountDTO.getAccountPk());
		cartDTO.setCondition("DELETE_CART_BY_ACCOUNT_PK");
		cartRepository.delete(cartDTO);
		
		// 3. 찜 목록 삭제
		WishlistDTO wishlistDTO = new WishlistDTO();
		wishlistDTO.setAccountPk(accountDTO.getAccountPk());
		wishlistDTO.setCondition("DELETE_ALL_WISHLIST_BY_ACCOUNT_PK");
		wishlistRepository.delete(wishlistDTO);
		
		// 4. 회원 id를 NULL로 바꾸기
		accountDTO.setCondition("UPDATE_ACCOUNT_WITHDRAW");
		accountRepository.update(accountDTO);
		
		return true;
	}

	@Override
	public List<AccountDTO> getAdminSearchAccount(AccountDTO accountDTO) {
		accountDTO.setCondition("SELECT_ALL_ROLE_USER_ACCOUNT_BY_ADMIN_SEARCH");
		List<AccountDTO> accountDatas = accountRepository.selectAll(accountDTO);
		
		return accountDatas;
	}

	@Override
	public AccountDTO getAdminAccountInfo(Integer accountPk) {
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setAccountPk(accountPk);
		accountDTO.setCondition("SELECT_ADMIN_ACCOUNT_INFO_BY_ACCOUNT_PK");
		
		return accountRepository.selectOne(accountDTO);
	}
}
