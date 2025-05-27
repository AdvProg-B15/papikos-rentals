package id.ac.ui.cs.advprog.papikos.rentals.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalFeignConfig {

    @Value("${internal.token.secret}")
    private String internalTokenSecret;

    @Bean
    public RequestInterceptor propertyBasedRequestInterceptor() {
        return template -> {
            if (internalTokenSecret != null && !internalTokenSecret.isEmpty()) {
                template.header("X-Internal-Token", internalTokenSecret);
            }
        };
    }
}