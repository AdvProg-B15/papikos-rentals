package id.ac.ui.cs.advprog.papikos.rentals.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${notification.service.url:http://localhost:8083/api/v1/notifications}")
public interface NotificationServiceClient {

    @PostMapping("/internal/send")
    ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest notificationRequest);

    /*
    @PostMapping("/internal/vacancy")
    ResponseEntity<Void> triggerVacancyNotification(@RequestBody VacancyTriggerRequest vacancyRequest);
    */
}