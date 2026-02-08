package bugsandwich.ornably.solapi;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.dto.response.MultipleDetailMessageSentResponse;
import com.solapi.sdk.message.exception.SolapiEmptyResponseException;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.exception.SolapiUnknownException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;

@Service
public class SolapiService {
	
	private static DefaultMessageService messageService; 
	
	// 인증 번호 저장용 코드
	private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();
	
	@Value("${SOLAPI_FROM_NUMBER}")
	private String SOLAPI_API_NUMBER;
	
	public SolapiService(
			StringRedisTemplate redisTemplate,
			@Value("${SOLAPI_API_KEY}") String apiKey,
            @Value("${SOLAPI_API_SECRET}") String apiSecret) {
		this.redisTemplate = redisTemplate;
		this.messageService = SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);
	}
	
	public boolean sendCodeMessage(String targetPhone, String accountKey) {
		
		// 전화번호 유효성 검사
		if(!this.checkPhonePattern(targetPhone)) {
			return false;
		}
		
		String code = this.generateCode(6);
		String text = "오너블리 인증번호는 ["+ code + "] 입니다.\r\n"
					+ "본인 확인을 위해 입력해 주세요.";
		
		// SOLAPI 메시지 객체 생성
		Message message = new Message();
        message.setFrom(this.SOLAPI_API_NUMBER);
        message.setTo(targetPhone);
        message.setText(text);

        MultipleDetailMessageSentResponse response;
		try {
			response = messageService.send(message, null);
		} catch (SolapiMessageNotReceivedException e) {
			e.printStackTrace();
		} catch (SolapiEmptyResponseException e) {
			e.printStackTrace();
		} catch (SolapiUnknownException e) {
			e.printStackTrace();
		}
		
		// 레디스 서버에 TTL 3분의 인증번호 저장하기
		String redisOptKey = this.getOptKey(accountKey);
		this.redisTemplate.opsForValue()
				.set(redisOptKey, code, Duration.ofMinutes(3));
		
		return true;
	}
	
	public boolean validateCode(String code, String key) {
		String redisOptKey = this.getOptKey(key);
		
		String redisCode = redisTemplate.opsForValue().get(redisOptKey);
		
		if(code.equals(redisCode)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private String getOptKey(String key) {
		return "opt:account:"+key;
	}
	
	
	
	private boolean checkPhonePattern(String phoneNumber) {
		return phoneNumber != null && Pattern.compile("^010\\d{8}$").matcher(phoneNumber).matches();
	}
	
	private String generateCode(int length) {
		
		// 3 이하의 코드 거부
	    if (length <= 3) {
	        throw new IllegalArgumentException("length must be greater than 3");
	    }

	    // 난수 length 길이 생성
	    StringBuilder sb = new StringBuilder(length);
	    for (int i = 0; i < length; i++) {
	        sb.append(this.secureRandom.nextInt(10)); // 0~9
	    }
	    
	    // 만들어진 length길이의 난수 반환
	    return sb.toString();
	}
	
}
