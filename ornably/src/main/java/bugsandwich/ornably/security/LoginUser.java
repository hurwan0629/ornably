	package bugsandwich.ornably.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

// UserDetails : 로컬 회원 DTO
// OAuth2User : 소셜 회원을 위한 DTO
public class LoginUser implements UserDetails, OAuth2User {

	private final long accountPk;
	private final String accountName; // 로컬이면 accountId, 소셜이면 sub/email 중 택1
	private final String accountId;
	private final String accountPasswordHash;
	private final String role;
	private final ArrayList<Integer> eventPkList;
	private final Collection<? extends GrantedAuthority> authorities;	 // spring security에서 권한 확인할 때 사용하는 값
	private final Map<String, Object> attributes; // OAuth2에서 회원가입할때 정보받거나 로그인할 때 id 받기위해
	
	public LoginUser(long accountPk, String accountName, String accountId, String accountPasswordHash, String role,
			Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes,
			ArrayList<Integer> eventPkList) {
		this.accountPk = accountPk;
		this.accountName = accountName;
		this.accountId = accountId;
		this.accountPasswordHash = accountPasswordHash;
		this.role = role;
		this.authorities = authorities;
		this.attributes = attributes == null ? Collections.emptyMap() : attributes;
		this.eventPkList = eventPkList;
	}

	// --- app에서 필요하면 쓰는 최소 getter
	public String getRole() { 
		return role;
	}
	
	public String getAccountId() {
		return accountId;
	}
	
	// --- UserDetails
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}
	
	@Override
	public String getPassword() {
		return accountPasswordHash;
	}

	@Override
	public String getUsername() {
		return accountName;
	}
	/*
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	*/
	// --- OAuth2User
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public String getName() {
		return accountName;
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}
}
