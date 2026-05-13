package bioCanteenApp.products.domain;

import bioCanteenApp.suppliers.domain.Supplier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "product_batches")
@Getter
@Setter
public class ProductBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column
    private Double quantity;

    @Column(nullable = false)
    private LocalDate receivedDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    private boolean isBio;

    private boolean isQuarantined=false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    public ProductBatch(Product product, Double quantity, LocalDate receivedDate, boolean isBio, Supplier supplier) {
        this.product = product;
        this.quantity = quantity;
        this.receivedDate = receivedDate;
        this.expirationDate = receivedDate.plusDays(product.getExpirationDays());
        this.isBio = isBio;
        this.supplier = supplier;
    }

    protected ProductBatch() { }
}
