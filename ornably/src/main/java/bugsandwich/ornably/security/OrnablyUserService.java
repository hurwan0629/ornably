package bugsandwich.ornably.security;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import bugsandwich.ornably.account.AccountDTO;
import bugsandwich.ornably.account.AccountRepository;
import bugsandwich.ornably.event.EventRepository;

@Service
public class OrnablyUserService implements UserDetailsService { // 유저디테일 서비스를 상속 받음

	// 의존성 주입
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private EventRepository eventRepository;

	@Override
	// 유저 디테일 안에 있는 유저이름을 가져옴
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		// 가져온 유저이름 체크

		// 회원DTO 만들어서 DB안에있는 회원이름을 가져오기
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setAccountId(username);
		accountDTO.setCondition("SELECT_ORNABLY_USER_BY_ACCOUNT_ID");
		accountDTO = accountRepository.selectOne(accountDTO);

		// 회원 정보가 없을때
		if (accountDTO == null) {
			throw new UsernameNotFoundException("not found: " + username);
		}

		/*
		 * //해당회원이 참여가능한 이벤트 pK 목록 가져오기 EventDTO eventDTO = new EventDTO(); // DTO에에서
		 * 구분할 컨디션 가져오기
		 * eventDTO.setCondition("SELECT_ALL_APPLICABLE_EVENT_PK_BY_ACCOUNT_PK"); //이벤트
		 * DTO에서 이벤트 어카운트Pk 가져오기 eventDTO.setEventPk(accountDTO.getAccountPk()); //DAO에서
		 * 전체 이벤트 리스트 가져오기 ArrayList<AccountDTO> eventList =
		 * eventRepository.selectAll(eventDTO); //리스트로 회원이벤트 pk리스트 가져오기
		 * ArrayList<Integer> accountEventPkList = new ArrayList<>(); for(AccountDTO
		 * eventPk:eventList) { accountEventPkList.add(eventPk.getEventPk()); }
		 */

		// 스프링 시큐리티가 유저네임을 주면 거기에 맞는 유저스 디테일을 상속 받은 객체를 반환
		return new OrnablyUser(accountDTO.getAccountPk(), // accountPk
				accountDTO.getAccountName(), // accountName
				accountDTO.getAccountId(), // accountId
				accountDTO.getAccountPasswordHash(), // accountPasswordHash,
				accountDTO.getAccountRole(), // ADMIN, LOCAL, GOOGLE, KAKAO
				// ![USER, ADMIN, ONBOARD] == //accountRole,
				List.of(new SimpleGrantedAuthority(
						"ROLE_" + (accountDTO.getAccountRole().equals("LOCAL") ? "USER" : "ADMIN"))), // authorities
				Map.of()// attributes
		);
	}

}
