package bugsandwich.ornably.review.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
	 
	 @Value("${resource.review.prefix}")
	 private String reivewPrefix;
	 
	 @Value("${server.origin}")
	 private String serverOrigin;

	// 허용되는 파일 확장자 종류
	private static final Set<String> ALLOWED_EXTENTION = Set.of("jpg", "jpeg", "png", "webp");
	// 허용되는 이미지 크기
	private static final Long MAX_BYTES = 10L * 1024 * 1024; // 10MB

	// accountPk를 받아서 reviewDatas 반환하는 메서드
	@Override
	public List<ReviewDTO> getReviewByAccountPk(Integer accountPk) {
		ReviewDTO reviewDTO = new ReviewDTO();
		reviewDTO.setCondition("SELECT_ALL_REVIEW_BY_ACCOUNT_PK");
		reviewDTO.setAccountPk(accountPk);

		return this.reviewRepository.selectAll(reviewDTO);
	}

	// page / dataCount / itemPk 들어있는 reviewDTO를 인자로 받아서
	// 상품 상세페이지에 쓰일 reveiwDatas를 반환하는 메서드
	@Override
	public List<ReviewDTO> getReviewByItemPk(ReviewDTO reviewDTO) {

		// LIMIT [dataCount] OFFSET ? 를 주기 위한 데이터
		reviewDTO.setStartReviewNum((reviewDTO.getPage() - 1) * reviewDTO.getDataCount() + 1);
		reviewDTO.setEndReviewNum((reviewDTO.getPage()) * reviewDTO.getDataCount());
		reviewDTO.setCondition("SELECT_ALL_REVIEW_PAGENATION_BY_ITEM_PK");

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
		reviewDTO.setCondition("DELETE_BY_REVIEW_PK");
		
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
		// 사진이 존재하면 저장후 저장된 경로 문자열을 DTO에 너허어주기
		if (image != null && !image.isEmpty()) {
			
			try {
				// 예: /images/review/20260206_153012_xxx.png
				reviewDTO.setReviewImageUrl(saveImageAndGetUrl(this.resourcePath, this.reivewPrefix, image));
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		reviewDTO.setCondition("INSERT_REVIEW_WRITE");

		// 리뷰 추가 후 반환 
		return this.reviewRepository.insert(reviewDTO);
	}

	@Override
	public boolean updateReview(ReviewDTO reviewDTO) {
		// 리뷰 수정 실행 후 결과 반환
		reviewDTO.setCondition("REVIEW_WRITE_EDIT");
		return this.reviewRepository.update(reviewDTO);
	}

	// 이미지 파일을 넣으면 저장 후 경로 문자열을 반환해주는 함수
	// 서비스 내부에서만 사용할 메서드라서 private처리
	@Override
	public String saveImageAndGetUrl(String resourcePath, String prefix, MultipartFile file) throws IOException {
		Path saveDir = Path.of(resourcePath + prefix);
		// 디렉토리 보장
		
		Files.createDirectories(saveDir);
		// 파일명 충돌 방지: 시간 + UUID + 확장자
		
		String extention = getExtentionFromFile(file);
		String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		String imageName = timeStamp + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extention;

		// 최종 저장 경로
		Path target = saveDir.resolve(imageName).normalize();

		// 저장
		Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

		return this.serverOrigin + prefix + imageName; // "http://loaclhost:8088/images/review/" + imageName
	}

	private static Map<String, Object> err(String code, String message) {
		return Map.of("code", code, "message", message);
	}

	// 이미지 크기가 규정에 맞는지 확인하는 메서드
	@Override
	public boolean checkFileSize(MultipartFile file) {
		if (file.getSize() > this.MAX_BYTES) {
			return false;
		}
		return true;
	}

	// 파일 이미지 확장자가 정상인지 확인하는 메서드
	@Override
	public boolean checkFileExtention(MultipartFile file) {
		// 확장자 문자열 뽑아내기
		String extention = this.getExtentionFromFile(file);
		
		
		
		if (!this.ALLOWED_EXTENTION.contains(extention)) {
			return false;
		}
		return true;
	}

	// 서비스 내부에서 사용하는 확장자 뽑아주는 함수. 없으면 "" 을 반환
	private String getExtentionFromFile(MultipartFile file) {
		// 확장자 문자열 뽑아내기
		String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

		// 이미지 확장자 소문자 형태
		int idx = original.lastIndexOf('.');
		String extention = null;
		if (idx < 0 || idx == original.length() - 1) {
			extention = "";
		} else {
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

	@Override
	public ReviewDTO getReviewMaxPageByItemPkAndDataCount(ReviewDTO reviewDTO) {
		reviewDTO.setCondition("SELECT_ONE_REVIEW_PAGINATION_MAX_PAGES_BY_ITEM_PK_AND_DATA_COUNT");
		return this.reviewRepository.selectOne(reviewDTO);
	}
}
