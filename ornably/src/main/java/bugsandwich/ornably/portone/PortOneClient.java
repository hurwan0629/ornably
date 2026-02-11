package bugsandwich.ornably.portone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
public class PortOneClient {

    private final RestClient restClient; // PortOne API 호출을 위한 RestClient (HTTP 통신 객체)
    private final String apiSecret;

    public PortOneClient(
            @Value("${portone.v2.api-secret}") String apiSecret
    ) {
        this.apiSecret = apiSecret;
        
        // RestClient 객체 생성
        // PortOne API 기본 URL 설정
        this.restClient = RestClient.builder()
                .baseUrl("https://api.portone.io") // PortOne 서버 기본 주소
                // 모든 요청을 JSON 타입으로 보내도록 설정
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) 
                .build();
    }

    /** 결제 단건 조회: GET /payments/{paymentId} */
    public PortOnePaymentDTO getPayment(String paymentId) {
        return restClient.get()  // GET 요청 생성
                .uri("/payments/{paymentId}", paymentId) // 실제 호출 URL:
                // 형식: Authorization: PortOne {API_SECRET}
                .header(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret) // PortOne 인증 헤더 추가
                .retrieve() // 서버 응답 받아오기

                .body(PortOnePaymentDTO.class);
    }
    
}
