package bugsandwich.ornably.portone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // JSON에 없는 DTO 멤버변수가 없어도 에러 무시하고 넘기기
public class PortOnePaymentDTO {
    private String status;
    private Amount amount;
    private Method method;   // ✅ 추가

    @Data
    public static class Amount {
        private int total;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Method {
        private String type;      // e.g. "PaymentMethodEasyPay", "PaymentMethodCard"
        private String provider;  // e.g. "KAKAOPAY", "NAVERPAY" (EasyPay일 때 의미 있음)
    }
    
 // ✅ ORDERS_PAYMENT_TYPE에 넣을 값 만들기
    public String resolveOrdersPaymentType() {
        if (method == null || method.type == null) return null;

        // 1) 일반 카드 결제
        if (method.type.contains("Card")) return "CARD";

        // 2) 간편결제
        if (method.type.contains("EasyPay")) {
            // provider가 "KAKAOPAY" 같이 오면 DB 규칙에 맞춰 매핑
            if (method.provider == null) return "EASY_PAY";

            return switch (method.provider) {
                case "KAKAOPAY" -> "KAKAO_PAY";
                case "NAVERPAY" -> "NAVER_PAY";
                case "TOSSPAY"  -> "TOSS_PAY";
                default -> method.provider; // 필요하면 "EASY_PAY"로 통일해도 됨
            };
        }

        // 3) 기타(계좌이체, 가상계좌 등)
        return method.type; // 너의 DB 규칙에 맞춰 추가 매핑
    }
}
