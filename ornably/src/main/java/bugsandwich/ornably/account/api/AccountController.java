package bugsandwich.ornably.account.api;

import java.time.LocalDate;
import java.util.HashMap;
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
import bugsandwich.ornably.account.OnboardSignupRequest;
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
	@PreAuthorize("anonymous()")
	@PostMapping("/guest/account/signup")
	public ResponseEntity<Map<String, Object>> signup(@RequestBody OnboardSignupRequest req) {

		AccountDTO accountDTO = req.getAccount();
		AddressDTO addressDTO = req.getAddress();

		if (accountDTO == null || addressDTO == null) {
			return ResponseEntity.badRequest()
					.body(Map.of("code", "VALIDATION_ERROR", "message", "요청 데이터가 올바르지 않습니다."));
		}

		// 1. 회원가입 시도
		accountDTO.setAccountRole("LOCAL");
		boolean result = this.accountService.registAccount(accountDTO, addressDTO);
		if (result) {
			return ResponseEntity.status(201).body(Map.of("code", "CREATED", "message", "회원이 정상적으로 생성되었습니다."));
		} else {
			return ResponseEntity.status(500)
					.body(Map.of("code", "INTERNAL_SERVER_ERROR", "message", "회원 생성 도중 오류가 발생했습니다."));
		}
	}

	// 아이디 중복 체크 기능 ( 쓰임 : 회원가입 )
	@PreAuthorize("anonymous()")
	@GetMapping("/guest/account/check-id")
	public ResponseEntity<Map<String, Object>> checkIdDuplicate(@ModelAttribute AccountDTO accountDTO) {
		boolean isDuplicated = accountService.checkIdDuplicate(accountDTO);

		return ResponseEntity.status(200).body(Map.of("isDuplicated", isDuplicated));
	}

	// 소셜 회원가입 기본 데이터 응답 로직
	@PreAuthorize("hasRole('ONBOARD')")
	@GetMapping("/onboard/account/onboard")
	public ResponseEntity<Map<String, Object>> onboardSignUpData(@AuthenticationPrincipal OrnablyUser ornablyUser) {
		AccountDTO accountDTO = new AccountDTO();

		Map<String, Object> map = new java.util.HashMap<>();
		map.put("accountId", ornablyUser.getAccountId());
		map.put("accountName", ornablyUser.getAttributes().get("name")==null ? null : String.valueOf(ornablyUser.getAttributes().get("name")));
		map.put("accountEmail", ornablyUser.getAttributes().get("email")==null ? null : String.valueOf(ornablyUser.getAttributes().get("email")));
		return ResponseEntity.ok(map);
	}

	// 소셜 회원가입 로직
	@PreAuthorize("hasRole('ONBOARD')")
	@PostMapping("/onboard/account/onboard/signup")
	public ResponseEntity<Map<String, Object>> checkIdDuplicate(@AuthenticationPrincipal OrnablyUser ornablyUser,
			@RequestBody OnboardSignupRequest req) {
		
		AccountDTO accountDTO = req.getAccount();
		AddressDTO addressDTO = req.getAddress();

		if (accountDTO == null || addressDTO == null) {
			return ResponseEntity.badRequest()
					.body(Map.of("code", "VALIDATION_ERROR", "message", "요청 데이터가 올바르지 않습니다."));
		}
		
		// 1. 회원가입 시도
		accountDTO.setAccountRole(ornablyUser.getAccountRole());
		addressDTO.setAddressIsDefault(true);
		boolean result = this.accountService.registAccount(accountDTO, addressDTO);
		if (!result) {
			return ResponseEntity.status(500)
					.body(Map.of("code", "INTERNAL_SERVER_ERROR", "message", "회원 생성 도중 오류가 발생했습니다."));
		}
		return ResponseEntity.status(201).body(Map.of("code", "CREATED", "message", "회원이 정상적으로 생성되었습니다."));
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/user/account/mypage")
	public ResponseEntity<Map<String, Object>> checkIdDuplicate(@AuthenticationPrincipal OrnablyUser ornablyUser) {
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setAccountPk(ornablyUser.getAccountPk());

		accountDTO = accountService.getMyPageData(accountDTO);

		return ResponseEntity.status(200).body(Map.of("accountData", accountDTO));
	}

	@PreAuthorize("hasRole('USER')")
	@PostMapping("/user/account/check-password")
	public ResponseEntity<Map<String, Object>> checkPassword(@RequestBody AccountDTO accountDTO,
			@AuthenticationPrincipal OrnablyUser ornablyUser) {
		boolean correct = this.PasswordEncoder.matches(accountDTO.getAccountPassword(), ornablyUser.getPassword());

		return ResponseEntity.status(200).body(Map.of("correct", correct));
	}

	@PreAuthorize("hasRole('USER')")
	@DeleteMapping("/user/account/withdraw")
	public ResponseEntity<Map<String, Object>> accountWithdraw(
			@AuthenticationPrincipal OrnablyUser ornablyUser) {
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setAccountPk(ornablyUser.getAccountPk());
		// 1. 회원 주소 싹다 지우고
		// 2. 장바구니 삭제
		// 3. 찜 목록 삭제
		// 4. 회원 id를 NULL로 바꾸기
		if (accountService.accountWithdraw(accountDTO)) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.internalServerError().build();
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/account/search")
	public ResponseEntity<Map<String, Object>> adminSearchAccount(@ModelAttribute AccountDTO accountDTO) {
		/*
		 * accountPk={number} - 기본값: 0 accountName={string} - 기본값: ""
		 * accountJoinStartDate={date} - 기본값: 2026-01-01 accountJoinEndDate={date} -
		 * 기본값: date.최댓값 accountRole={string} - 기본값: "ALL"
		 * accountTotalAmountMin={number} - 기본값: 0 accountTotalAmountMax={number} - 기본값:
		 * Integer.max
		 */
		if (accountDTO.getAccountPk()==null || accountDTO.getAccountPk() == 0) {
			accountDTO.setAccountPk(null);
		}
		if (accountDTO.getAccountName()==null || accountDTO.getAccountName() == "") {
			accountDTO.setAccountName(null);
		}

		if (accountDTO.getAccountJoinStartDate() == null) {
			accountDTO.setAccountJoinStartDate(LocalDate.of(2026, 1, 1));
		}

		if (accountDTO.getAccountJoinEndDate() == null) {
			accountDTO.setAccountJoinEndDate(LocalDate.of(9999, 12, 31));
		}

		if (accountDTO.getAccountRole() == null || accountDTO.getAccountRole().isBlank()) {
			accountDTO.setAccountRole(null);
		}

		if (accountDTO.getAccountTotalAmountMin() == null) {
			accountDTO.setAccountTotalAmountMin(0);
		}
		if (accountDTO.getAccountTotalAmountMax() == null) {
			accountDTO.setAccountTotalAmountMax(Integer.MAX_VALUE);
		}
		
		List<AccountDTO> accountDatas = accountService.getAdminSearchAccount(accountDTO);
		
		return ResponseEntity.ok(Map.of("accountDatas", accountDatas));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/account/{accountPk}")
	public ResponseEntity<Map<String, Object>> adminShowAccountInfo(@PathVariable Integer accountPk) {
		// 회원 정보 받아오기
		AccountDTO accountDTO = accountService.getAdminAccountInfo(accountPk);

		// 회원이 쓴 리뷰 받아오기
		List<ReviewDTO> reviewDatas = reviewService.getReviewByAccountPk(accountPk);
		
		Map<String, Object> body = new HashMap<>();
		body.put("accountPk", accountDTO.getAccountPk());
		body.put("accountId", accountDTO.getAccountId());
		body.put("accountName", accountDTO.getAccountName());
		body.put("accountDate", accountDTO.getAccountDate());
		body.put("accountRole", accountDTO.getAccountRole());
		body.put("accountEventOptIn", accountDTO.getAccountEventOptIn());
		body.put("accountTotalAmount", accountDTO.getAccountTotalAmount());
		body.put("reviewDatas", reviewDatas);

		return ResponseEntity.ok(body);
	}
}
