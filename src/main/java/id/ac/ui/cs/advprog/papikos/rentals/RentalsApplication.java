package id.ac.ui.cs.advprog.papikos.rentals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "id.ac.ui.cs.advprog.papikos.rentals")
@EnableAsync
@EnableFeignClients(basePackages = "id.ac.ui.cs.advprog.papikos.rentals.client")
public class RentalsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalsApplication.class, args);
    }

}
