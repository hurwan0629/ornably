package bugsandwich.ornably.account.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.account.AccountDTO;
import bugsandwich.ornably.account.service.AccountService;
import bugsandwich.ornably.address.AddressDTO;
import bugsandwich.ornably.review.ReviewDTO;
import bugsandwich.ornably.review.service.ReviewService;
import bugsandwich.ornably.security.OrnablyUser;


@RestController
@RequestMapping("/api")
public class AccountController {

	@Autowired
	private AccountService accountService;
	@Autowired
	private ReviewService reviewService;
	@Autowired
	private PasswordEncoder PasswordEncoder;

	// 로컬 회원가입 로직	
	@PreAuthorize("anonymous()")// 특정 역할을 가진 사용자만 메서드를 호출할수 있다
	@PostMapping("/guest/account/signup")
	public ResponseEntity<Map<String, Object>> signup(
			@RequestBody AccountDTO accountDTO,//클라이언트에서 보낸 요청에 있는 body를 자바 객체로 바꿔줌
			@RequestBody AddressDTO addressDTO) {
		// 1. 회원가입 시도
		accountDTO.setAccountRole("LOCAL"); //현재 회원가입하려는 사용자의 롤값을 회원 DTO에 넣고
		boolean result = this.accountService.registAccount(accountDTO, addressDTO);//실제 회원가입을 진행한다
		if (result) {//성공시
			return ResponseEntity.status(201).body(Map.of("code", "CREATED", "message", "회원이 정상적으로 생성되었습니다."));
		} else {//실패시
			return ResponseEntity.status(500)
					.body(Map.of("code", "INTERNAL_SERVER_ERROR", "message", "회원 생성 도중 오류가 발생했습니다."));
		}
	}

	// 아이디 중복 체크 기능 ( 쓰임 : 회원가입 )
	@PreAuthorize("anonymous()") // 특정 역할을 가진 사용자만 메서드를 호출할수 있다
	@GetMapping("/guest/account/check-id")
	public ResponseEntity<Map<String, Object>> checkIdDuplicate(
			@ModelAttribute AccountDTO accountDTO
			) {
		boolean isDuplicated = accountService.checkIdDuplicate(accountDTO); //DB에 있는 아이디인지 체크

		return ResponseEntity.status(200).body(Map.of("isDuplicated", isDuplicated)); //응답반환 
	}

	// 소셜 회원가입 기본 데이터 응답 로직
	@PreAuthorize("hasRole('ONBOARD')")
	@GetMapping("/onboard/account/onboard") //소셜 첫로그인 일때
	public ResponseEntity<Map<String, Object>> onboardSignUpData(
			@AuthenticationPrincipal OrnablyUser ornablyUser // 현재 로그인 사용자 꺼내오기
			) {
		AccountDTO accountDTO = new AccountDTO();
		//소셜로그인시 기본 제공되는 값
		Map<String, Object> map = new java.util.HashMap<>();
		map.put("accountId", ornablyUser.getAccountId()); // 아이디
		map.put("accountName", String.valueOf(ornablyUser.getAttributes().get("name"))); //이름
		map.put("accountEmail", String.valueOf(ornablyUser.getAttributes().get("email"))); //이메일
		return ResponseEntity.ok(map);//반환
	}

	// 소셜 회원가입 로직
	@PreAuthorize("hasRole('ONBOARD')")// 롤 온보드인 사람만 접근가능
	@PostMapping("/onboard/account/onboard/signup")
	public ResponseEntity<Map<String, Object>> checkIdDuplicate(
			@AuthenticationPrincipal OrnablyUser ornablyUser,//현재 로그인사용자 꺼내오기
			@RequestBody AccountDTO accountDTO, //클라이언트에서 보낸 요청에 있는 body를 자바 객체로 바꿔줌
			@RequestBody AddressDTO addressDTO) {
		// 1. 회원가입 시도
		accountDTO.setAccountRole(ornablyUser.getAccountRole()); //현재 로그인주체인 오너블리에서 롤값을 꺼내서 회원 DTO에 그대로 넣어준다
		// 2. role 값까지 채워진 accountDTO + addressDTO를 서비스에 넘겨서
		//	    실제 회원가입(계정 생성, 주소 저장 등)을 수행한다.
		//	    result는 성공/실패 여부를 boolean으로 받는 것으로 보인다.
		boolean result = this.accountService.registAccount(accountDTO, addressDTO);
		if (!result) {//회원가입 실패시 응답
			return ResponseEntity.status(500)
					.body(Map.of("code", "INTERNAL_SERVER_ERROR", "message", "회원 생성 도중 오류가 발생했습니다."));
		}
		return ResponseEntity.status(201).body(Map.of("code", "CREATED", "message", "회원이 정상적으로 생성되었습니다.")); //성공시 응답
	}
	//마이페이지 요청
	@PreAuthorize("hasRole('USER')") //롤이 유저인 사용자만 접근가능
	@GetMapping("/user/account/mypage")
	public ResponseEntity<Map<String, Object>> checkIdDuplicate(
			@AuthenticationPrincipal OrnablyUser ornablyUser//현재 로그인사용자 꺼내오기
			) {
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setAccountPk(ornablyUser.getAccountPk()); //회원 pk에 해당하는 유저

		accountDTO = accountService.getMyPageData(accountDTO);//회원 DTO에 있는 마이페이지 데이터 가져오기

		return ResponseEntity.status(200).body(Map.of("accountData", accountDTO));//응답반환
	}
	//비밀번호 체크
	@PreAuthorize("hasRole('USER')")// 롤이 유저인 사용자만 접근 가능
	@PostMapping("/user/account/check-password")
	public ResponseEntity<Map<String, Object>> checkPassword(
			@RequestBody AccountDTO accountDTO, //클라이언트에서 보낸 요청에 있는 body를 자바 객체로 바꿔줌
			@AuthenticationPrincipal OrnablyUser ornablyUser) { // 현재 로그인 사용자
		boolean correct = this.PasswordEncoder.matches(accountDTO.getAccountPassword(), ornablyUser.getPassword());
		//회원DTO에 있는 비밀번호와 오너블리 유저에 있는 비밀번호를 패스워드인코더로 맞는지 확인한다

		return ResponseEntity.status(200).body(Map.of("correct", correct));//응답반환
	}
	//회원 탈퇴
	@PreAuthorize("hasRole('USER')")// 롤이 유저인 사용자만 접근 가능
	@DeleteMapping("/user/account/withdraw")
	public ResponseEntity<Map<String, Object>> accountWithdraw(
			@AuthenticationPrincipal OrnablyUser ornablyUser //현재 로그인한사용자 가져옴
			) {
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setAccountPk(ornablyUser.getAccountPk()); //오너블리 유저에있는회원 pk를 회원DTO에 넣기
		// 1. 회원 주소 싹다 지우고
		// 2. 장바구니 삭제
		// 3. 찜 목록 삭제
		// 4. 회원 id를 NULL로 바꾸기
		if(accountService.accountWithdraw(accountDTO)) { //탈퇴 처리성공시
			return ResponseEntity.noContent().build(); //상태코드204만 전달 
		}
		else {//실패시 에러출력
			return ResponseEntity.internalServerError().build();
		}
	}

	//=============관리자===============

	//사용자 검색하기
	@PreAuthorize("hasRole('ADMIN')") //관리자만 접근 가능
	@GetMapping("/admin/account/search")
	public ResponseEntity<Map<String, Object>> adminSearchAccount(
			@ModelAttribute AccountDTO accountDTO
			// - GET 쿼리 파라미터(예: ?page=1&size=10)가 있으면 AddressDTO 필드에 자동 바인딩
			// - 파라미터가 없어도 스프링이 AddressDTO 객체를 자동 생성해 줌(new 없이 사용 가능)
			) {
		/*
accountPk={number}				- 기본값: 0
accountName={string}			- 기본값: ""
accountJoinStartDate={date}		- 기본값: 2026-01-01	
accountJoinEndDate={date}		- 기본값: date.최댓값
accountRole={string}			- 기본값: "ALL"
accountTotalAmountMin={number}	- 기본값: 0
accountTotalAmountMax={number}	- 기본값: Integer.max
		 */
		//회원 pk별 검색
		if (accountDTO.getAccountPk() == 0) {
			accountDTO.setAccountPk(null);
		}
		//회원 이름별검색
		if (accountDTO.getAccountName() == "") {
			accountDTO.setAccountName(null);
		}
		//회원 가입일별 검색
		if (accountDTO.getAccountJoinStartDate() == null) { 
			accountDTO.setAccountJoinStartDate(LocalDate.of(2026, 1, 1));
		}

		if (accountDTO.getAccountJoinEndDate() == null) {        	
			accountDTO.setAccountJoinEndDate(LocalDate.of(9999, 12, 31));
		}
		//권한별 검색
		//롤이 없으면 전체를 의미한다
		if (accountDTO.getAccountRole() == null || accountDTO.getAccountRole().isBlank()) {
			accountDTO.setAccountRole("ALL");
		}
		//결제 금액별 검색(작은순)
		if (accountDTO.getAccountTotalAmountMin() == null) {
			accountDTO.setAccountTotalAmountMin(0);        	
		}
		//결제 금액별 검색(큰순)
		if (accountDTO.getAccountTotalAmountMax() == null) {
			accountDTO.setAccountTotalAmountMax(Integer.MAX_VALUE);
		}

		List<AccountDTO> accountDatas = accountService.getAdminSearchAccount(accountDTO); //검색결과에 맞는 회원 DTO를 리스트로 생성

		return ResponseEntity.ok(Map.of("accountDatas", accountDatas)); //생성한 데이터를 반환
	}
//사용자 리뷰 관리
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/account/{accountPk}")
	public ResponseEntity<Map<String, Object>> adminShowAccountInfo(
			@PathVariable Integer accountPk //url 경로에 있는 값을 꺼내서 컨트롤러 메서드 파라이터에 넣어 주는 어노테이션
			) {
		// 회원 정보 받아오기
		AccountDTO accountDTO = accountService.getAdminAccountInfo(accountPk);

		// 회원이 쓴 리뷰 받아오기
		List<ReviewDTO> reviewDatas = reviewService.getReviewByAccountPk(accountPk);
		
		// 받아온 데이터를 json으로 반환
		return ResponseEntity.ok(Map.of(
				"accountPk", accountDTO.getAccountPk(), //회원Pk
				"acconutId", accountDTO.getAccountId(), //아이디
				"accountName", accountDTO.getAccountName(),//이
				"accountDate", accountDTO.getAccountDate(),//회원가입일
				"accountRole", accountDTO.getAccountRole(), //권한
				"accountEventOptIn", accountDTO.getAccountEventOptIn(), // 이벤트 동의 여부
				"accountTotalAmount", accountDTO.getAccountTotalAmount(),//총결제금액
				"reviewDatas", reviewDatas //받아온 리뷰데이터
				));
	}
}
