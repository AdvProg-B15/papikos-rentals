package id.ac.ui.cs.advprog.papikos.rentals.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "kos-service", url = "${kos.service.url:http://localhost:8081/api/v1/kos}")
public interface KosServiceClient {

    @GetMapping("/internal/{kosId}/details")
    KosDetailsDto getKosDetails(@PathVariable("kosId") UUID kosId);

    @GetMapping("/internal/{kosId}/active-rentals-count")
    long getActiveRentalsCountForKos(@PathVariable("kosId") UUID kosId);
}