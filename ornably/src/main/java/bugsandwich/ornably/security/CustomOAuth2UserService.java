package bugsandwich.ornably.security;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import bugSandwich.ornably.account.AccountDTO;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	@Autowired
	private AccountRepository accountRepository;
	
	
	// accessTocken
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) {
		OAuth2User oAuth2User = super.loadUser(userRequest); 
		// ▼ 우리가 직접 session 커스터마이징
		Map<String, Object> attrs = oAuth2User.getAttributes();
		String sub = String.valueOf(attrs.get("sub")); // 구글 고유 ID
		String name = String.valueOf(attrs.get("name"));
		String email = String.valueOf(attrs.get("email"));

		System.out.println("아이디: [GOOGLE_" + sub + "]");
		System.out.println("이름: [" + name + "]");
		System.out.println("이메일: [" + email + "]");
		
		AccountDTO userFlag = accountRepository.findByAccountId("GOOGLE_" + sub);
		
		// 구글 회원이 존재한다면
		if (userFlag!=null) {
			System.out.println("존재하는 회원!");
			return new LoginUser(
					userFlag.getAccountPk(), // accountPk
					name, // accountName
					"GOOGLE_" + sub, // accountId
					null, // accountPasswordHash
					"GOOGLE", // role
					List.of(new SimpleGrantedAuthority("ROLE_USER")), // accountAthority 
					attrs);
		}
		// 구글 회원이 존재하지 않는다면
		else {
			return new LoginUser(0, // accountPk
					name, // accountName
					"GOOGLE_" + sub, // accountId
					null, // accountPasswordHash
					"GOOGLE", // role
					List.of(new SimpleGrantedAuthority("ROLE_ONBOARD")), // accountAthority
					attrs); // 권한
		}
	}
}
