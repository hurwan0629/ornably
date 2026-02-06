package bugsandwich.ornably.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class OrnablyUser implements UserDetails,OAuth2User{
   
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
   
   private int accountPk;
   private String accountName;
   private String accountId;
   private ArrayList<Integer> accountEventPkList;
   private String accountPasswordHash;
   private String accountRole;
   private Collection<? extends GrantedAuthority> authorities;
   private Map<String, Object>  attributes;
   
   
    // ✅ 1) 전체 필드 받는 생성자 (All-Args Constructor)
    // - 모든 멤버변수를 한 번에 초기화할 때 사용
    public OrnablyUser(int accountPk,
                     String accountName,
                     String accountId,
                     ArrayList<Integer> accountEventPkList,
                     String accountPasswordHash,
                     String accountRole,
                     Collection<? extends GrantedAuthority> authorities,
                     Map<String, Object> attributes) {

        // this.필드 = 파라미터;  → 객체의 멤버변수에 값 세팅
        this.accountPk = accountPk;
        this.accountName = accountName;
        this.accountId = accountId;
        this.accountEventPkList = accountEventPkList;
        this.accountPasswordHash = accountPasswordHash;
        this.accountRole = accountRole;
        this.authorities = authorities;
        this.attributes = attributes;
    }
   
   public int getAccountPk() {
      return accountPk;
   }


   public String getAccountId() {
      return accountId;
   }


   public ArrayList<Integer> getAccountEventPkList() {
      return accountEventPkList;
   }
   
   public String getAccountRole() {
	   return accountRole;
   }

   
   @Override
   public Map<String, Object> getAttributes() {
      return attributes;
   }

   @Override
   public String getName() {
      return accountName;
   }

   @Override
   public Collection<? extends GrantedAuthority> getAuthorities() {
      return authorities;
   }

   @Override
   public @Nullable String getPassword() {
      return accountPasswordHash;
   }

   @Override
   public String getUsername() {
      return accountName;
   }

}
