package bugsandwich.ornably.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;


/*
 * 우리서비스가 가지고 있어야되는값
 * accountPk
 * accountName -> 스프링 시큐리티 회원식별값
 * accountId
 * account EventPkList
 * accountPassword
 * accountPasswordHash
 * role
 *  getAuthorities -> 권한
 *  getAttributes -> 오어스 사용자가 사용
 *  
 */

public class OrnablyUser implements UserDetails,OAuth2User{
	// 공유 멤버변수
	private Integer accountPk;
	private String accountName;
	private String accountId;
	private String accountRole;
	private Collection<? extends GrantedAuthority> authorities;
	
	// 로컬 회원 전용 멤버변수 
	private String accountPasswordHash;
	
	// 소셜 회원 전용 멤버변수
	private Map<String, Object>  attributes;
	
	
	 // ✅ 1) 전체 필드 받는 생성자 (All-Args Constructor)
    // - 모든 멤버변수를 한 번에 초기화할 때 사용
    public OrnablyUser(Integer accountPk,
                     String accountName,
                     String accountId,
                     String accountPasswordHash,
                     String accountRole,
                     Collection<? extends GrantedAuthority> authorities,
                     Map<String, Object> attributes) {

        // this.필드 = 파라미터;  → 객체의 멤버변수에 값 세팅
        this.accountPk = accountPk;
        this.accountName = accountName;
        this.accountId = accountId;
        this.accountPasswordHash = accountPasswordHash;
        this.accountRole = accountRole;
        this.authorities = authorities;
        this.attributes = attributes;
    }
    
    public String getAccountRole() {
    		return accountRole;
    }
    
	public Integer getAccountPk() {
		return accountPk;
	}


	public String getAccountId() {
		return accountId;
	}


	@Override
	public Map<String, Object> getAttributes() {
		// TODO Auto-generated method stub
		return attributes;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return accountName;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return authorities;
	}

	@Override
	public @Nullable String getPassword() {
		// TODO Auto-generated method stub
		return accountPasswordHash;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return accountId;
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
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
