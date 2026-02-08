package bugsandwich.ornably;

import java.util.Arrays;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class OrnablyApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrnablyApplication.class, args);
	}
	
	@Bean
	  ApplicationRunner propCheck(Environment env) {
	    return args -> {
	      System.out.println("=== PROP CHECK ===");
	      System.out.println("activeProfiles=" + Arrays.toString(env.getActiveProfiles()));
	      System.out.println("resource.path.review=" + env.getProperty("resource.path.review"));
	      System.out.println("resource.path=" + env.getProperty("resource.path"));
	      System.out.println("resource.url-prefix=" + env.getProperty("resource.url-prefix"));
	      System.out.println("==================");
	    };
	  }
}
