package be.he2b.don5.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    
    /**
     * Object mapper to serialize events in the Outbox for Kafka
     * Also to deserialize Kafka's messages for the consumers
     * @return
     */
    @Bean
    public ObjectMapper objectMapper() {
        // "findAndRegisterModules" will register the JavaTimeModule to handle Java 8 date/time types
        return new ObjectMapper().findAndRegisterModules();
    }
}