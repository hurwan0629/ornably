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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bugsandwich.ornably.address.AddressDTO;
import bugsandwich.ornably.address.service.AddressService;
import bugsandwich.ornably.address.service.AddressServiceImpl;
import bugsandwich.ornably.security.OrnablyUser;

@RestController
@RequestMapping("/api/user/address")//주소 관련 모든 요청을 처리
public class AddressController {
	
	@Autowired
	private AddressService addressService;


	//=========로그인한 사용자 배송지 목록 조회=========
	@GetMapping//("/api/user/address")
	public ResponseEntity <List<String, Object>> getMyAddresses(
			//ResponseEntity HTTP 응답(상태코드,응답 바디,헤더)을 내가 원하는대로 조립할수 잇음
			@AuthenticationPrincipal OrnablyUser ornablyUser
			//	@AuthenticationPrincipal 스프링 시큐리티가 로그인 처리해두고 보관한 현재 로그인한 사용자 정보를 파라미터로 꺼내줌
			){
		//로그인 체크
		//보통 스프링시큐리티가 인증안된 요청을 처리해주긴하는데 만약을 대비해 만들어놓음
		if(ornablyUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		//로그인한 사용자 pk 가져오기
		Integer accountPk = ornablyUser.getAccountPk();

		//실제 비즈니스 로직 처리를 위해 서비스 부름 
		List<AddressDTO.getAddressList> addresses = addressService.getAddressesByAccountPk(accountPk);
		//응답 만들기
		return ResponseEntity.ok(addresses);

	}

	//=========특정주소삭제===============
	@DeleteMapping("/{addressPk}")
	public ResponseEntity void deleteMyAddress(
			//url 경로 값받기
			@PathVariable Integer addressPk,
			//@PathVariable //url 경로에 있는 값을 거내서 컨트롤러 메서드 파라이터에 넣어 주는 어노테이션
			//로그인 사용자 받기
			@AuthenticationPrincipal OrnablyUser ornablyUser
			){
		//로그인 체크(예외상황 대비용)
		if(ornablyUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		Integer accountPk = ornablyUser.getAccountPk();

		//서비스에게 내 주소PK 삭제요청
		//삭제하려는 주소PK가 내것인지 검증하는것이 중요함
		boolean deleted = addressService.deleteAdsress(accountPk,addressPk);

		//결과에 따라 응답
		if(!dedeted) {
			//내 주소가 아니거나,존재하지 않거나, 삭제 불가능한경우
			//에러상태 출력
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();\
		}

		//삭제 성공
		return ResponseEntity.noContent().bulid();

	}
	//=========기본배송지로 변경===============
	@PatchMapping("/{addressPk}")
	public ResponseEntity<Void> patchMyAddress(
			@PathVariable Integer addressPk, // url에 있는 주소PK
			@RequestBody AddressDTO.AddressPatchRequest req // 수정할 주소정보
			@AuthenticationPrincipal OrnablyUser ornablyUser //로그인 사용자
			)
	//예외방지용 로그인 체크
	if(ornablyUser == null) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}
	Integer accountPk = ornablyUser.getAccountPk();

	//서비스에게 내 주소PK 변경요청
	//변경하려는 주소PK가 내것인지 검증하는것이 중요함
	boolean updated = addressService.patchAdsress(accountPk,addressPk,req);

	//결과에 따라 응답
	if(!update) {
		//내 주소가 아니거나,존재하지 않거나, 수정 불가능한경우
		//에러상태 출력
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	//수정 성공
	return ResponseEntity.noContent().bulid();

}
//============배송지등록 ==============
@PostMapping("/regist")
public ResponseEntity<AddressDTO.AddressCreateResponse> registAddress(
		@RequestBody AddressDTO.AddressCreateRequest req, // 등록할 주소 정보
		@AuthenticationPrincipal OrnablyUser ornablyUser
		){
	//로그인 체크
	if(ornablyUser == null) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}
	//로그인사용자PK
	Integer accountPk = ornablyUser.getAccountPk();

	//서비스 호출(DB insert)
	Integer createAddressPk = addressService.createAddress(accountPk,req);

	//생성상태 코드 응답
	AddressDTO.AddressCreatResponse res = new AddressDTO.AddressCreateResponse(createdAddressPk);

	return ResponseEntity.status(HttpStatus.CREATED).body(res);




}
}
