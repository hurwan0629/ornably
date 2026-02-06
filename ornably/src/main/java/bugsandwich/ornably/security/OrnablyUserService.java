package bugsandwich.ornably.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import bugsandwich.ornably.account.AccountDTO;
import bugsandwich.ornably.account.AccountRepository;
import bugsandwich.ornably.event.EventDTO;
import bugsandwich.ornably.event.EventRepository;

public class OrnablyUserService implements UserDetailsService {
   @Autowired
   private AccountRepository accountRepository;
   @Autowired
   private EventRepository eventRepository;
   
   @Override
   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      // TODO Auto-generated method stub
      
      System.out.println("OrnablyUserService.loadUserByUsername | id: [" + username + "]");
      
      AccountDTO accountDTO = new AccountDTO();
      accountDTO.setCondition("SELECT_ORNABLY_USER_BY_ACCOUNT_ID");
      accountDTO.setAccountId(username);
      accountDTO = accountRepository.selectOne(accountDTO);
      
      
      
      if (accountDTO == null) {
            System.out.println("회원 찾지 못함");
            throw new UsernameNotFoundException("not found: " + username);
      }
      
      EventDTO eventDTO = new EventDTO();
      eventDTO.setCondition("SELECT_ALL_APPLICABLE_EVENT_PK_BY_ACCOUNT_PK");
      eventDTO.setEventAccountPk(accountDTO.getAccountPk());
      ArrayList<EventDTO> eventList = eventRepository.selectAll(eventDTO);
      ArrayList<Integer> accountEventPkList;
      for(EventDTO eventPk:eventList) {
         accountEventPkList.add(eventPk.getEventPk());
      }
      
      
      //스프링 시큐리티가 유저네임을 주면 거기에 맞는 유저스 디테일을 상속 받은 객체를 반환
      
      return new OrnablyUser(
               accountDTO.getAccountPk(),
                    accountDTO.getAccountName(),
                    accountDTO.getAccountId(),
                    accountEventPkList,
                    accountDTO.getAccountPasswordHash(),
                    accountDTO.getAccountRole(), // ADMIN, LOCAL, GOOGLE, KAKAO
                   // ![USER, ADMIN, ONBOARD] == 
                   List.of(new SimpleGrantedAuthority("ROLE_"+(accountDTO.getAccountRole().equals("LOCAL")?"USER":"ADMIN"))), 
                   Map.of()//attributes
            );
   }


}
	