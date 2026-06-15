package bioCanteenApp.suppliers.domain;

import bioCanteenApp.address.Address;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supplier_applications")
@Getter
@Setter
public class SupplierApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    private Address address;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    private Long nif;

    @Lob
    @Column(name = "certified_organic", columnDefinition = "MEDIUMBLOB")
    private byte[] bioCertificate;

    @ElementCollection()
    private List<SupplierCapacity> supplierCapacity= new ArrayList<>();

    @Column(nullable = false)
    private LocalDate applicationDate= LocalDate.now();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SupplierApplicationStatus status;

    @Column
    private InterviewStatus interviewStatus;

    public SupplierApplication( String name, String email, String phoneNumber, Address address, byte[] bioCertificate,
                                Long nif, List<SupplierCapacity> supplierCapacity, LocalDate applicationDate) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.bioCertificate = bioCertificate;
        this.nif = nif;
        this.supplierCapacity = supplierCapacity;
        this.applicationDate = applicationDate;
        this.status = SupplierApplicationStatus.PENDING;
        this.interviewStatus = InterviewStatus.TO_BE_DONE;
    }

    protected SupplierApplication() { }
}
