package bugsandwich.ornably.solapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;


// Redis 데이터베이스 연결 객체 생성 설정 객체
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        return new StringRedisTemplate(connectionFactory);
    }
}