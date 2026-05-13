package bioCanteenApp.suppliers.domain;

import bioCanteenApp.address.Address;
import bioCanteenApp.users.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(unique = true)
    private String nif;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    private Address address;

    @Column
    private String phoneNumber;

    @Lob
    @Column(name = "certified_organic", columnDefinition = "BLOB")
    private byte[] certifiedOrganic;

    @OneToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private SupplierApplication applicationId;

    @Column
    private boolean isQuarantined=false;

    public Supplier(
            User user,
            String nif,
            Address address,
            String phoneNumber,
            byte[] certifiedOrganic,
            SupplierApplication applicationId) {
        this.user = user;
        this.nif = nif;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.certifiedOrganic = certifiedOrganic;
        this.applicationId = applicationId;
    }

    @PrePersist
    protected void onCreate() {
        this.isQuarantined = false;
    }

    protected Supplier() { }
}
