package bugsandwich.ornably.portone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // JSON에 없는 DTO 멤버변수가 없어도 에러 무시하고 넘기기
public class PortOnePaymentDTO {
    private String status;
    private Amount amount;
    private EasyPay easyPay;

    @Data
    public static class Amount {
        private int total;
    }
    
    @Data
    public static class EasyPay {
    	private String provider;
    }
}
