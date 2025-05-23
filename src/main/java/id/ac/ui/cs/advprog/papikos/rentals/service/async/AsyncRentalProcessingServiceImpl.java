package id.ac.ui.cs.advprog.papikos.rentals.service.async;

import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;
// import id.ac.ui.cs.advprog.papikos.rentals.client.SomeOtherServiceClient; // Jika ada
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AsyncRentalProcessingServiceImpl implements AsyncRentalProcessingService {

    private static final Logger log = LoggerFactory.getLogger(AsyncRentalProcessingServiceImpl.class);
    // private final SomeOtherServiceClient someOtherServiceClient; // Contoh jika memanggil service lain

    @Override
    @Async("asyncTaskExecutor") // Gunakan executor yang dikonfigurasi
    public CompletableFuture<Void> performPostApprovalTasks(Rental approvedRental) {
        log.info("Starting asynchronous post-approval tasks for rental ID: {}", approvedRental.getId());
        try {
            // Simulasi pekerjaan, mis:
            // 1. Mengirim data ke sistem audit eksternal
            // someOtherServiceClient.logRentalApproval(approvedRental.getId(), approvedRental.getKosId());
            Thread.sleep(1000); // Simulasi latensi jaringan atau pemrosesan
            log.info("Simulated call to external audit system for rental ID: {}", approvedRental.getId());

            // 2. Memperbarui statistik internal (misalnya, jumlah rental aktif per kos)
            // Ini bisa jadi panggilan ke KosService atau pembaruan data denormalisasi
            Thread.sleep(500); // Simulasi
            log.info("Simulated update of statistics for kos ID: {}", approvedRental.getKosId());

            log.info("Completed asynchronous post-approval tasks for rental ID: {}", approvedRental.getId());
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Asynchronous post-approval task interrupted for rental ID: {}", approvedRental.getId(), e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Error during asynchronous post-approval tasks for rental ID: {}", approvedRental.getId(), e);
            // Penting untuk menangani pengecualian agar tidak "hilang"
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @Async("asyncTaskExecutor")
    public CompletableFuture<Void> performPostCancellationTasks(Rental cancelledRental) {
        log.info("Starting asynchronous post-cancellation tasks for rental ID: {}", cancelledRental.getId());
        try {
            // Mis: Memberitahu sistem lain bahwa slot mungkin tersedia
            Thread.sleep(800);
            log.info("Simulated notification to vacancy management system for kos ID: {}", cancelledRental.getKosId());

            log.info("Completed asynchronous post-cancellation tasks for rental ID: {}", cancelledRental.getId());
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Asynchronous post-cancellation task interrupted for rental ID: {}", cancelledRental.getId(), e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Error during asynchronous post-cancellation tasks for rental ID: {}", cancelledRental.getId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}