package bioCanteenApp.provisioning.domain;

import bioCanteenApp.menu.domain.Menu;
import bioCanteenApp.products.domain.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "provisioning_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"menu_id", "product_id", "type"})
        })
@Getter
@Setter
public class ProvisioningItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Double quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProvisioningType type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ProvisioningItem() {}

    public ProvisioningItem(Menu menu,
                            Product product,
                            Double quantity,
                            ProvisioningType type,
                            LocalDateTime createdAt) {
        this.menu = menu;
        this.product = product;
        this.quantity = quantity;
        this.type = type;
        this.createdAt = createdAt;
    }
}
