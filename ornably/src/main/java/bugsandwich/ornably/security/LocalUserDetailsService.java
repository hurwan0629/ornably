package bugsandwich.ornably.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import bugSandwich.ornably.account.AccountDTO;
import bugSandwich.ornably.event.Service.EventService;

@Service
public class LocalUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;
    
    @Autowired
    private EventService eventService;
    
    public LocalUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    // 로그인 과정 작업
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    		System.out.println("로그인 시도 username:["+username+"]");
        AccountDTO a = accountRepository.findByAccountId(username);
       
        if (a == null) {
        		System.out.println("회원 찾지 못함");
        		throw new UsernameNotFoundException("not found: " + username);
        }
        System.out.println("회원 존재");
        System.out.println(a);
        
        ArrayList<Integer> eventPkList = eventService.getApplicableEventsByAccountPk(a.getAccountPk());
        
        // DB ROLE이 LOCAL/ADMIN/GOOGLE 등이라도 MVP에서는 ROLE_USER만
        return new LoginUser(
        			a.getAccountPk(),
        			a.getAccountName(),
                a.getAccountId(),
                a.getAccountPassword(),
                a.getAccountRole(), // ADMIN, LOCAL
                // ![USER, ADMIN, ONBOARD] == 
                List.of(new SimpleGrantedAuthority("ROLE_"+(a.getAccountRole().equals("LOCAL")?"USER":"ADMIN"))), 
                Map.of(),
                eventPkList;
        );
    }
}
