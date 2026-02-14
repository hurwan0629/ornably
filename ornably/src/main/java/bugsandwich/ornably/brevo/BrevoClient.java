package bugsandwich.ornably.brevo;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BrevoClient {
   
  private final RestClient rest;

  public BrevoClient(@Value("${brevo.api-key}") String apiKey) {
    this.rest = RestClient.builder()
        .baseUrl("https://api.brevo.com/v3")
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("api-key", apiKey)
        .build();
  }

  public void send(String fromEmail, String fromName, String toEmail, String subject, String html) {
    Map<String, Object> body = Map.of(
        "sender", Map.of("email", fromEmail, "name", fromName), // properties에 고정 ( 보낸 이메일 / 보낸 사람 )
        "to", List.of(Map.of("email", toEmail)),
        "subject", subject,
        "htmlContent", html
    );

    rest.post().uri("/smtp/email").body(body).retrieve().toBodilessEntity();
  }
}