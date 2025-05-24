package id.ac.ui.cs.advprog.papikos.rentals.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class RentalEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String rentalId;
    private String userId;
    private String kosId;
    private String kosOwnerId;
    private String kosName;
    private LocalDate bookingDate;
    private Integer rentalDurationMonths;
    private BigDecimal price;
    private String status;
    private String tenantName;
    private String tenantPhone;
    private String timestamp;

    public RentalEvent(String rentalId, String userId, String kosId, String kosOwnerId, String kosName,
                       LocalDate bookingDate, Integer rentalDurationMonths, BigDecimal price, String status,
                       String tenantName, String tenantPhone, String timestamp) {
        this.rentalId = rentalId;
        this.userId = userId;
        this.kosId = kosId;
        this.kosOwnerId = kosOwnerId;
        this.kosName = kosName;
        this.bookingDate = bookingDate;
        this.rentalDurationMonths = rentalDurationMonths;
        this.price = price;
        this.status = status;
        this.tenantName = tenantName;
        this.tenantPhone = tenantPhone;
        this.timestamp = timestamp;
    }
}