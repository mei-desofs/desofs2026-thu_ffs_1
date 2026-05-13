package bioCanteenApp.suppliers.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
public class SupplierCapacity {
    private String productName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double quantity; //no contrato
    private Double remainingQuantity;

    public SupplierCapacity(String productName, LocalDate startDate, LocalDate endDate, Double quantity) {
        this.productName = productName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.quantity = quantity;
    }

    public SupplierCapacity() {}
}
