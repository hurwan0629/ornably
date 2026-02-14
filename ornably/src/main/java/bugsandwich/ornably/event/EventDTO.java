package bugsandwich.ornably.event;


import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.type.Alias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data 				// getter/setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor	// 기본 생성자
@AllArgsConstructor	// 모든 필드를 받는 생성자
// @Alias("eventDTO")
public class EventDTO {

	// [ 테이블 컬럼 ] 
	private Integer eventPk;					// 이벤트 고유 번호 (Pk)
	private String eventName;					// 이벤트 이름
	private LocalDate eventStartDate;			// 이벤트 시작일
	private LocalDate eventEndDate;				// 이벤트 종료일
	private String eventTargetAccount;		// 이벤트 대상
	private String eventTargetCategory;		// 이벤트 상품 카테고리
	private String eventDescription;			// 이벤트 설명
	private Integer eventDiscountRate;			// 할인율
	private String eventImageUrl;				// 이벤트 이미지 URL
	
	// [ 추가 멤버 변수 ]
	private String condition;					// 분기점
	private Integer eventAccountPk;  			// 조회용 계정 PK
	private List<EventDTO> accountEventPkList;  // 쿼리 결과 이벤트 리스트
	private String accountEmail;				// 이벤트 내용 보낼 이메일
}