package bugsandwich.ornably.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import bugsandwich.ornably.account.AccountDTO;
import bugsandwich.ornably.account.AccountRepository;



//OAuth2로그인 과정에서 사용자 정보를 가공하는 역할을 하는 서비스
//우리서비스용 Principal로 변환
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	@Autowired //DB 접근을 담당하는 Repository Bean을 스프링으로부터 주입받음
	private AccountRepository accountRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {


		/* 1) 부모 클래스인 DefaultOAuth2UserService의 loaduser를 호출
		OAuth 제공자(카카오,구글) 서버에 사용자 정보 요청을 보내고 응답을 받아옴   userRequest = 로그인 요청 정보*/
		OAuth2User oAuth2User = super.loadUser(userRequest);
		

		// 2) 어떤 OAuth2제공자인지 확인 (카카오,구글) - application.properties의 registrationId랑 동일
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		// 3) OAuth에서 내려준 사용자 정보 전체 (map형태)
		Map<String,Object> attr = oAuth2User.getAttributes();

		// 4) OAuth제공자별로 응답구조가 다르기때문에 공통형태로 정규화해서 꺼내기
		SocialUserInfo socialUser = extractSocialUserInfo(registrationId, attr);

		/* 5) 우리서비스에서 사용할 회원아이디 생성
		provider prefix + providerId 예: kakao_123456789 / google_10987654321*/
		String accountId = socialUser.provider()+ "_" +socialUser.providerId();
		

		// 6) DB에서 기존회원 여부 확인
		//결과 있으면 기존회원 없으면 신규회원(온보딩 필요함)
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setCondition("SELECT_ACCOUNT_PK_BY_ACCOUNT_ID");
		accountDTO.setAccountId(accountId);

		AccountDTO result = accountRepository.selectOne(accountDTO);
		Integer accountPk = (result!= null) ? result.getAccountPk() : null ;
        



		// 7) 기존/신규에 따라 권한 부여하기
		Collection<? extends GrantedAuthority> authorities;
		//회원Pk가 널이 아니면
		if(accountPk != null) {
			//기존회원
			authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

		} else {
			//신규회원 (온보딩 필요함)
			authorities = List.of(new SimpleGrantedAuthority("ROLE_ONBOARD"));
		}
        

		/* 8) 스프링 시큐리티 세션(SecurityContext)에 저장될 Principal(OAuth2User) 만들기
		DefaultOAuth2User는 "권한(authorities) + attributes + nameKey" 조합으로 Principal 생성
        이 키가 getName(고유값)의 기준이 됨 */

		String nameAttributeKey = userRequest
				.getClientRegistration()
				.getProviderDetails()
				.getUserInfoEndpoint()
				.getUserNameAttributeName();

		// 9) 우리서비스 식별에 꼭 필요한 값
		//맵 복사형태로 사용
		Map<String,Object> mergedAttributes = new HashMap<>(attr);

		mergedAttributes.put("accountId", accountId); //우리 서비스 로그인키
		mergedAttributes.put("accountPk", accountPk); //pk없으면 null
		mergedAttributes.put("provider", socialUser.provider()); //카카오/구글 구분
		mergedAttributes.put("providerId", socialUser.providerId()); //카카오 id/구글 sub

		//소셜에서 받는 기본값(온보딩 페이지 인풋용으로 사용)
		mergedAttributes.put("name", socialUser.name()); //소셜에서 기본적용되는 이름
		mergedAttributes.put("email", socialUser.email()); //이메일있으면 가져오고 없으면 null
		// 이걸 반환해야 (principal 반환) authorities(권한)가 실제로 적용됨
		// 소셜은 이벤트목록/비번/role 같은 게 아직 없으니 기본값 넣기

		return new OrnablyUser(
				(accountPk != null) ? accountPk : 0,                              // accountPk (신규면 0)
				 socialUser.name(),               // accountName (소셜에서 받은 이름)
				 accountId,                       // accountId (provider_providerId)
				 null,                            // accountPasswordHash (소셜은 비번 없음)
				 registrationId.toUpperCase(),    // accountRole (원하면 "SOCIAL" 같은 값으로 통일해도 됨)
				 authorities,                     // authorities (ROLE_USER or ROLE_ONBOARD)
				 mergedAttributes                 // attributes (OAuth2 원본 + 우리가 넣은 값)
				);
	}// ✅ loadUser 끝


	//제공자별 응답 맵
	//우리서비스에서 사용하기 쉬운 공통형태로 뽑아냄
	private SocialUserInfo extractSocialUserInfo(String registrationId,Map<String,Object> attributes) {
		
		//=====카카오========
		if ("kakao".equals(registrationId)) {
			//카카오 고유 아이디
			String kakaoId = String.valueOf(attributes.get("id"));

			//카카오 account안에 프로필/이메일 이 들어가는경우
			String email = null;
			String name = null;

			Object kakaoAccountObj = attributes.get("kakao_account");
			if(kakaoAccountObj instanceof Map<?,?> kakaoAccount){

				//이메일은 설정/동의항목에 따라 없을 수 있음
				Object emailObj = kakaoAccount.get("email");
				if (emailObj !=null) {
					email = String.valueOf(emailObj);
				}

				//프로필.닉네임
				Object profileObj = kakaoAccount.get("profile");
				if(profileObj instanceof Map<?,?> profile) {
					Object nicknameObj = profile.get("nickname");
					if (nicknameObj != null) {
						name = String.valueOf(nicknameObj);
					}
				}
			}

			return new SocialUserInfo("kakao",kakaoId, name,email);
		}
		//======google =======
		if("google".equals(registrationId)) {

			//구글 고유 id는 보통 sub
			String googleSub = String.valueOf(attributes.get("sub"));

			//구글은 이름과 이메일이 최상위에 온다
			String name = attributes.get("name") != null ? String.valueOf(attributes.get("name")): null;
			String email = attributes.get("email")!= null ? String.valueOf(attributes.get("email")): null;

			// 소셜로그인 사용자 정보 반환
			return new SocialUserInfo("google",googleSub,name,email);
		}
		
		//====== naver =======
		if ("naver".equals(registrationId)) {

		    // 네이버는 attributes 최상위에 response라는 Map이 들어있음
		    Object responseObj = attributes.get("response");

		    if (responseObj instanceof Map<?, ?> response) {

		        // 네이버 고유 ID (필수)
		        String naverId = response.get("id") != null
		                ? String.valueOf(response.get("id"))
		                : "unknown";

		        // 이름/이메일은 동의항목에 따라 null일 수 있음
		        String name = response.get("name") != null
		                ? String.valueOf(response.get("name"))
		                : null;

		        String email = response.get("email") != null
		                ? String.valueOf(response.get("email"))
		                : null;

		        return new SocialUserInfo("naver", naverId, name, email);
		    }

		    // response가 없으면 fallback
		    return new SocialUserInfo("naver", "unknown", null , null);
		}

		// 둘다 아니면 기본값 반환(리턴 누락방지)
		return new SocialUserInfo(registrationId,"unknown", null, null);

	}// ✅ extract 메서드 끝

	//소셜 제공자에서 받은 정보를 공통형태로 묶기위한 클래스
	private record SocialUserInfo(String provider,String providerId,String name, String email) {}
}


