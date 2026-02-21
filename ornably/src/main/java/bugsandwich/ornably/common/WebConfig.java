package bugsandwich.ornably.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	/* 현재 경로 상태
	 * resource.path=C:/HUR/workspace/Ornably/resource
	 * resource.path.review=C:/HUR/workspace/Ornably/resource/images/review
	 * resource.url-prefix=/images
	 */
	
	// application.properties에 적혀있는 resource폴더가 존재하는 경로
	@Value("${resource.path}")
	private String resourcePath; // C:/HUR/workspace/Ornably/resource
	// resource.path에 접근하기 위한 prifix정보
	@Value("${resource.url-prefix}")
	private String urlPrefix; // /images

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 예: GET /images/review/abc.jpg -> file:C:/.../resource/images/review/abc.jpg
		// 데이터를 줄 파일 경로 설정 (이후 .addResourceLocations() 를 통해 등록)
		String location = "file:" + resourcePath + "/images/";	// C:/HUR/workspace/Ornably/resource/images/
		registry.addResourceHandler(urlPrefix + "/**")	// /images/** 요청을 리소스 요청으로 받아줌 
				.addResourceLocations(location);			
		// 스프링이 정적 리소스를 줄 때 http://localhost:8088/images/** 요청은 location으로부터 찾음 
	}
}



