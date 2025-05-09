package id.ac.ui.cs.advprog.papikos.rentals.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "auth-service", url = "${auth.service.url:http://localhost:8080/api/v1/auth}")
public interface AuthServiceClient {

    @GetMapping("/users/{userId}/internal")
    UserDetailsDto getUserDetails(@PathVariable("userId") UUID userId);
}