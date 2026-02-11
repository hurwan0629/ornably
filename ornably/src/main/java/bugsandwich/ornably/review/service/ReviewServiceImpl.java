package bugsandwich.ornably.review.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import bugsandwich.ornably.review.ReviewDTO;
import bugsandwich.ornably.review.ReviewRepository;

@Service
public class ReviewServiceImpl implements ReviewService {

	@Autowired
	private ReviewRepository reviewRepository;

	// config.properties에 있는 리소스 저장하는 파일 절대경로
	 @Value("${resource.path}")
	private String resourcePath;
	 
	 @Value("${resource.review.prefix")
	 private String reivewPrefix;
	 
	 @Value("${server.origin}")
	 private String serverOrigin;

	// 허용되는 파일 확장자 종류
	private static final Set<String> ALLOWED_EXTENTION = Set.of("jpg", "jpeg", "png", "webp");
	// 허용되는 이미지 크기
	private static final Long MAX_BYTES = 10L * 1024 * 1024; // 10MB

	//사용자pk로 리뷰 조회
	// accountPk를 받아서 reviewDatas 반환하는 메서드
	@Override
	public List<ReviewDTO> getReviewByAccountPk(Integer accountPk) { 
		ReviewDTO reviewDTO = new ReviewDTO();
		reviewDTO.setCondition("SELECT_ALL_REVIEW_BY_ACCOUNT_PK");
		reviewDTO.setAccountPk(accountPk);

		return this.reviewRepository.selectAll(reviewDTO);
	}

	//상품pk로 리뷰조회
	// page / dataCount / itemPk 들어있는 reviewDTO를 인자로 받아서
	// 상품 상세페이지에 쓰일 reveiwDatas를 반환하는 메서드
	@Override
	public List<ReviewDTO> getReviewByItemPk(ReviewDTO reviewDTO) {
		
		//페이지네이션
		// LIMIT [dataCount] OFFSET ? 를 주기 위한 데이터
		reviewDTO.setStartReviewNum((reviewDTO.getPage() - 1) * reviewDTO.getDataCount() + 1);
		//이번 페이지의 첫번째 row번호(시작 인덱스)를 계산, 1부터 시작함
		reviewDTO.setCondition("SELECT_ALL_REVIEW_PAGENATION_BY_ITEM_PK");
		
		//응답반환
		return this.reviewRepository.selectAll(reviewDTO);
	}
	
	// 리뷰pk를 통한 리뷰 데이터 조회 (reviewImageUrl반환)
	@Override
	public ReviewDTO getReviewDataByReviewPk(Integer reviewPk) {
		ReviewDTO reviewDTO = new ReviewDTO();
		reviewDTO.setReviewPk(reviewPk);
		reviewDTO.setCondition("SELECT_REVIEW_DATA_BY_REVIEW_PK");

		return this.reviewRepository.selectOne(reviewDTO);
	}
	
	// 관리자 리뷰 데이터 조회
	@Override
	public List<ReviewDTO> getReviewDatasByReviewPkAdmin(Integer itemPk) {
		// 조회 전 리뷰 DTO 설정
		ReviewDTO reviewDTO = new ReviewDTO();
		reviewDTO.setItemPk(itemPk);
		reviewDTO.setCondition("SELECT_ALL_REVIEW_DATAS_BY_ITEM_PK_ADMIN_VIEW");
		
		// 리뷰 조회 후 반환
		return this.reviewRepository.selectAll(reviewDTO);
	}
	
	// 리뷰 pk를 통한 리뷰 삭제
	@Override
	public boolean deleteReviewByReviewPk(Integer reviewPk) {
		
		// 리뷰 데이터 설정
		ReviewDTO reviewDTO = new ReviewDTO();
		reviewDTO.setReviewPk(reviewPk);
		reviewDTO.setCondition("DELETE_REVIEW_BY_REVIEW_PK");
		
		// 실행 후 반환
		return this.reviewRepository.delete(reviewDTO);
	}

	/*
	 * 리뷰 등록하기 Integer accountPk MultipartFile reviewImage String reviewTitle String
	 * reviewContent Integer reviewStar
	 */
	@Override
	public boolean registReview(ReviewDTO reviewDTO) {
		// 2) 이미지 저장 + URL 생성
		MultipartFile image = reviewDTO.getReviewImage();
		// 사진이 존재하면 저장후 저장된 경로 문자열을 DTO에 넣어주기
		if (image != null && !image.isEmpty()) {

			try {
				// 예: /images/review/20260206_153012_xxx.png
				reviewDTO.setReviewImageUrl(saveReviewImageAndGetUrl(image));
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		reviewDTO.setCondition("INSERT_REGIST_REVIEW");

		// 리뷰 추가 후 반환 
		return this.reviewRepository.insert(reviewDTO);
	}

	@Override
	public boolean updateReview(ReviewDTO reviewDTO) {
		// 리뷰 수정 실행 후 결과 반환
		reviewDTO.setCondition("UPDATE_REVIEW_BY_USER");
		return this.reviewRepository.update(reviewDTO);
	}

	// 이미지 파일을 넣으면 저장 후 경로 문자열을 반환해주는 함수
	// 서비스 내부에서만 사용할 메서드라서 private처리
	private String saveReviewImageAndGetUrl(MultipartFile file) throws IOException {
		//리뷰이미지가 저장될 디렉토리 경로 만들기
		//resourcePath 서버파일 저장 루트경로(예: "C:/upload/")
		//revewPrefix 리뷰 이미지 폴더경로  (예: "/review/")
		Path reviewDir = Path.of(this.resourcePath + this.reivewPrefix);
		// 디렉토리 보장
		// 디렉토리 없으면 생성(있으면 통과)
		Files.createDirectories(reviewDir);

		// 파일명 충돌 방지: 시간 + UUID + 확장자
		//1) 확장자 추출
		String extention = getExtentionFromFile(file);
		//2) 현재 시간 문자열
		String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		//3) 최종 파일명 생성
		//UUID의 "-"는 제거해서 깔끔하게 만든다
		String reviewImageName = timeStamp + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extention;

		// 최종 저장 경로
		//resolve() reviewDir + 파일명 결합
		//nomalize() ../같은 이상한 경로 형태를 정리해줌
		Path target = reviewDir.resolve(reviewImageName).normalize();

		// 저장
		//REPLACE_EXISTING: 같은 이름이 있으면 덮어쓰기(근데 UUID라 충돌 가능성 매우 낮음)
		Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

		//저장 완료후 클라이언트가 접근할 url형태로 만들어서 반환
		// serverOrigin: "http://localhost:8088" 같은 서버 주소
	    // reivewPrefix: "/review/" 같은 경로 prefix
	    // reviewImageName: 실제 저장된 파일명
		return this.serverOrigin + this.reivewPrefix + reviewImageName;
	}
//에러 응답용 맵 만들 어서 에러 코드 호출
	private static Map<String, Object> err(String code, String message) {
		return Map.of("code", code, "message", message);
	}

	// 이미지 크기가 규정에 맞는지 확인하는 메서드
	@Override
	public boolean checkFileSize(MultipartFile file) {
		if (file.getSize() > this.MAX_BYTES) {
			return false;//너무 크면 실패
		}
		return true;//허용크기면 성공
	}

	// 파일 이미지 확장자가 정상인지 확인하는 메서드
	@Override
	public boolean checkFileExtention(MultipartFile file) {
		// 확장자 문자열 뽑아내기
		String extention = this.getExtentionFromFile(file);
//허용확장자 목록에 없으면 실패
		if (!this.ALLOWED_EXTENTION.contains(extention)) {
			return false;
		}
		return true;
	}

	// 서비스 내부에서 사용하는 확장자 뽑아주는 함수. 없으면 "" 을 반환
	private String getExtentionFromFile(MultipartFile file) {
		// 확장자 문자열 뽑아내기
		//원본 파일명 가져오기
		 //getOriginalFilename()이 null일 수 있으므로 ""로 대체
	    // cleanPath: 경로 문자(\, / 등) 섞인 입력을 정리해서 안전하게 만듦
		String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

		// 이미지 확장자 소문자 형태
		//마지막 "."의 위치를 찾아서 확장자 분리
		int idx = original.lastIndexOf('.');
		
		String extention = null;
		if (idx < 0 || idx == original.length() - 1) {
			//"."이 없거나 '.'으로 끝나면 확장자가 없다고 판단
			extention = "";
		} else {
			//"." 뒤의 문자열을 확장자고 사용하고 소문자로 통일
			extention = original.substring(idx + 1).toLowerCase();
		}
		return extention;
	}

	// 올바른 확장자 종류 Set 반환 메서드
	public Set<String> getAllowedExtentionSet() {
		return this.ALLOWED_EXTENTION;
	}

	// 허용된 이미지 최대 크기 반환(Byte)
	@Override
	public Long getAllowedImageMaxBytes() {
		return this.MAX_BYTES;
	}
}
