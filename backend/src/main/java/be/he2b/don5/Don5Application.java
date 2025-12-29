package be.he2b.don5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Enable scheduling for OutboxEventPublisher
@EnableKafka // Enable Kafka listeners
public class Don5Application {

	public static void main(String[] args) {
		SpringApplication.run(Don5Application.class, args);
	}

}