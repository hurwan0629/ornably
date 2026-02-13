package bugsandwich.ornably.address.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.address.AddressDTO;
import bugsandwich.ornably.address.service.AddressService;
import bugsandwich.ornably.security.OrnablyUser;

@RestController
@RequestMapping("/api/user/address") // 주소 관련 모든 요청을 처리
public class AddressController {

	@Autowired
	private AddressService addressService;
	
	
	// =========로그인한 사용자 배송지 목록 조회=========

	@GetMapping({"/", "/me"}) // ("/api/user/address")
	public ResponseEntity<?> getMyAddresses(
			// ResponseEntity HTTP 응답(상태코드,응답 바디,헤더)을 내가 원하는대로 조립할수 잇음
			@AuthenticationPrincipal OrnablyUser ornablyUser,
			// @AuthenticationPrincipal 스프링 시큐리티가 로그인 처리해두고 보관한 현재 로그인한 사용자 정보를 파라미터로 꺼내줌
			AddressDTO addressDTO
	// - GET 쿼리 파라미터(예: ?page=1&size=10)가 있으면 AddressDTO 필드에 자동 바인딩
	// - 파라미터가 없어도 스프링이 AddressDTO 객체를 자동 생성해 줌(new 없이 사용 가능)
	) {

		// 로그인 안했으면 401에러 뜸(스프링시큐리티가 인증안된 요청을 처리해주긴하는데 만약을 대비해 만들어놓음)
		if (ornablyUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		// 로그인한 사용자 pk 가져오기
		Integer accountPk = ornablyUser.getAccountPk();
		

		// DAO 분기용 condition + 조회 대상 accountPk 세팅
		addressDTO.setAccountPk(accountPk);// 누구의 주소 목록인지
		addressDTO.setCondition("SELECT_ALL_ADDRESS_BY_ACCOUNT_PK");// DAO분기용

		// 서비스 호출해서 내 주소목록 가져오기
		List<AddressDTO> addresses = addressService.getAddressList(addressDTO);
		

		// 프론트 요청에 맞게 필요한 값만 골라서 보내주기
		// DTO를 전부 보내지 않고 회원pk와 컨디션을 빼고 보내주기
//		List<Map<String, Object>> addressDatas = addresses.stream().map(a -> Map.of("addressPk", a.getAddressPk(), // 주소
//																													// PK
//				"addressName", a.getAddressName(), // 배송지명
//				"addressPostalCode", a.getAddressPostalCode(), // 우편번호
//				"addressRegion", a.getAddressRegion(), // 기본 주소
//				"addressDetail", a.getAddressDetail(), // 상세 주소
//				"addressIsDefault", a.isAddressIsDefault() // 기본 배송지 여부(boolean 게터)
//		)).collect(Collectors.toList());

		// json형태 응답만들기
		return ResponseEntity.ok(Map.of("addressDatas", addresses));

	}

	// =========특정주소삭제===============
	@DeleteMapping("/{addressPk}")
	public ResponseEntity<?> deleteMyAddress(
			// url 경로 값받기
			@PathVariable Integer addressPk,
			// @PathVariable //url 경로에 있는 값을 거내서 컨트롤러 메서드 파라이터에 넣어 주는 어노테이션
			// 로그인 사용자 받기
			@AuthenticationPrincipal OrnablyUser ornablyUser) {
		// 로그인 체크(예외상황 대비용)
		if (ornablyUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		Integer accountPk = ornablyUser.getAccountPk();
		

		AddressDTO addressDTO = new AddressDTO();
		// @
		addressDTO.setAccountPk(accountPk);
		addressDTO.setAddressPk(addressPk);
		addressDTO.setCondition("DELETE_ADDRESS_BY_ADDRESS_PK");

		// 서비스에게 내 주소PK 삭제요청
		// 삭제하려는 주소PK가 내것인지 검증하는것이 중요함
		boolean deleted = addressService.deleteAddress(addressDTO);

		// 결과에 따라 응답
		if (!deleted) {
			// 내 주소가 아니거나,존재하지 않거나, 삭제 불가능한경우
			// 에러상태 출력
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		// 삭제 성공
		return ResponseEntity.noContent().build();

	}

	// =========기본배송지로 변경===============
	@PatchMapping("/{addressPk}")
	public ResponseEntity<?> patchMyAddress(
			@PathVariable Integer addressPk, // url에 있는 주소PK
			@AuthenticationPrincipal OrnablyUser ornablyUser // 로그인 사용자
	) {
		// 예외방지용 로그인 체크
		if (ornablyUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		AddressDTO addressDTO = new AddressDTO();
		addressDTO.setAccountPk(ornablyUser.getAccountPk());
		addressDTO.setAddressPk(addressPk);
		// 서비스에게 내 주소PK 변경요청
		// 변경하려는 주소PK가 내것인지 검증하는것이 중요함

		// 결과에 따라 응답
		if (!addressService.changeDefaultAddress(addressDTO)) {
			// 내 주소가 아니거나,존재하지 않거나, 수정 불가능한경우
			// 에러상태 출력
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "사용자의 주소가 아니거나 이미 기본 주소인 배송지입니다."));
		}

		// 수정 성공
		return ResponseEntity.ok().build();

	}

//============배송지등록 ==============
	@PostMapping("/regist")
	public ResponseEntity<?> registAddress(@RequestBody AddressDTO addressDTO, // 등록할 주소 정보
			// 요청받은 제이슨의 body를 DTO로 변환해줌
			/*
			 * { "addressName": "우리집", "addressPostalCode": "12345", "addressRegion":
			 * "서울시 강남구 ...", "addressDetail": "101동 1001호" }
			 */
			@AuthenticationPrincipal OrnablyUser ornablyUser // 로그인 사용자
	) {
		// 로그인 체크
		if (ornablyUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		// 로그인사용자PK
		addressDTO.setAccountPk(ornablyUser.getAccountPk());

		addressDTO.setCondition("INSERT_NEW_ADDRESS");
		
		// 서비스 호출(DB insert)
		if (addressService.insertAddress(addressDTO)) {
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} else {
			return ResponseEntity.internalServerError().build();
		}
//
//	//생성상태 코드 응답
//	AddressDTO.AddressCreateResponse res = new AddressDTO.AddressCreateResponse(insertAddressPk);
//
//	return ResponseEntity.status(HttpStatus.CREATED).body(res);

	}
}
