package id.ac.ui.cs.advprog.papikos.rentals.client;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "kos-service", url = "${kos.service.url}")
public interface KosServiceClient {

    @GetMapping("/{kosId}")
    KosApiResponseWrapper<KosDetailsDto> getKosDetailsApiResponse(@PathVariable("kosId") UUID kosId);
}