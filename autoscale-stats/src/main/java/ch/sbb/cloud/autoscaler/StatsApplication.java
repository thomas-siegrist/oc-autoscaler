package ch.sbb.cloud.autoscaler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by bsmk on 9/9/15.
 */
@SpringBootApplication
@EnableScheduling
public class StatsApplication extends SpringBootServletInitializer {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(StatsApplication.class, args);
  }

}