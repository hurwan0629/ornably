package bugsandwich.ornably.portone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
public class PortOneClient {

    private final RestClient restClient;
    private final String apiSecret;

    public PortOneClient(
            @Value("${portone.v2.api-secret}") String apiSecret
    ) {
        this.apiSecret = apiSecret;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.portone.io")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /** 결제 단건 조회: GET /payments/{paymentId} */
    public PortOnePaymentDTO getPayment(String paymentId) {
    	
    	
    	
    	
    	
    	
        return restClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret)
                .retrieve()
                .body(PortOnePaymentDTO.class);
    }
    
}
