package sn.edu.ept.mediconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableJpaRepositories(basePackages = "sn.edu.ept.mediconnect")
public class MediconnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediconnectApplication.class, args);
	}

}
