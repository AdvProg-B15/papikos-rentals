package id.ac.ui.cs.advprog.papikos.rentals.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Jumlah thread inti
        executor.setMaxPoolSize(10); // Jumlah thread maksimum
        executor.setQueueCapacity(25); // Kapasitas task queue
        executor.setThreadNamePrefix("RentalAsync-");
        executor.initialize();
        return executor;
    }
}