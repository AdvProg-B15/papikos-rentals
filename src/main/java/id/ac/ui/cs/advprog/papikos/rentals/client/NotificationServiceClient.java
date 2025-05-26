package id.ac.ui.cs.advprog.papikos.rentals.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${notification.service.url}")
public interface NotificationServiceClient {

    @PostMapping("/api/v1/notifications/rentalUpdate")
    ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest notificationRequest);

}