package bugsandwich.ornably;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OrnablyApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrnablyApplication.class, args);
	}

}
