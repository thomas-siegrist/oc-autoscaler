package ch.sbb.cloud.autoscaler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ScalerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScalerApplication.class, args);
	}
}
