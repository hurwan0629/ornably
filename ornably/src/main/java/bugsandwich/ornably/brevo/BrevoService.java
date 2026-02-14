package bugsandwich.ornably.brevo;


import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import bugsandwich.ornably.account.AccountDTO;
import bugsandwich.ornably.event.EventDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrevoService {
   


    private final BrevoClient brevoClient;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name:Ornably}")
    private String senderName;

    /** 이벤트 등록 테스트 메일 */ // SELECT_ACCOUNT_EMAIL_EVENT_OPTIN
    public void sendEventInsertMail(EventDTO eventDTO) {     
        String subject = "[Ornably] 이벤트 등록 테스트";
        String html = "<h1>이벤트 등록 완료!</h1>"
                + "<p>eventPk: " + eventDTO.getEventPk() + "</p>"
                + "<p>eventName: " + eventDTO.getEventName() + "</p>";

        brevoClient.send(senderEmail, senderName, eventDTO.getAccountEmail(), subject, html);
    }
    
    // 이메일 전송 비동기 (Async)
    @Async
    public void  sendEventMailToAllAgreeAsync(List<AccountDTO> emails, EventDTO eventDTO) {
        String subject = "[Ornably] 이벤트 안내";
        String html =
               "<div style='max-width:600px;margin:0 auto;font-family:Arial,sans-serif;"
               + "background-color:#f9f6ff;padding:20px;border-radius:12px;'>"

               // 헤더
               + "<div style='text-align:center;'>"
               + "<h1 style='color:#8E44AD;'>🎄 Ornably 이벤트 안내</h1>"
               + "</div>"

               // 이벤트 제목
               + "<div style='background:white;padding:20px;border-radius:10px;"
               + "box-shadow:0 2px 6px rgba(0,0,0,0.1);'>"

               + "<h2 style='color:#333;text-align:center;'>"
               + eventDTO.getEventName()
               + "</h2>"

               // 이미지
               + "<div style='text-align:center;margin:20px 0;'>"
               + "<img src='"
               + "https://as2.ftcdn.net/jpg/05/15/99/71/1000_F_515997143_2MuevGmHnKVce8y4fgYipEW3zlPTPyE8.jpg"
               + "' "
               + "style='width:100%;max-width:400px;border-radius:10px;'/>"
               + "</div>"

               // 설명
               + "<p style='text-align:center;color:#555;'>"
               + eventDTO.getEventDescription()
               + "</p>"

               // 버튼
               + "<div style='text-align:center;margin-top:25px;'>"
               + "<a href='http://localhost:5173/' "
               + "style='background-color:#8E44AD;color:white;padding:12px 25px;"
               + "text-decoration:none;border-radius:8px;font-weight:bold;'>"
               + "이벤트 보러가기"
               + "</a>"
               + "</div>"

               + "</div>" // 카드 종료

               // 푸터
               + "<p style='text-align:center;font-size:12px;color:#aaa;margin-top:20px;'>"
               + "© Ornably. All rights reserved."
               + "</p>"

               + "</div>";
        
        for (AccountDTO dto : emails) {
            String toEmail = dto.getAccountEmail();
            if (toEmail == null || toEmail.isBlank()) continue;
            try {
                brevoClient.send(senderEmail, senderName, toEmail, subject, html);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
