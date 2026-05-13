package bioCanteenApp.address;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "address")
@Getter
@Setter
@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String street;

    @Enumerated(EnumType.STRING)
    private Municipality municipality;

    @Enumerated(EnumType.STRING)
    private Village village;

    private String country;

    private String postalCode;

    public Address(String street, Municipality municipality, Village village, String country, String postalCode) {
        this.street = street;
        this.municipality = municipality;
        this.village = village;
        this.country = country;
        this.postalCode = postalCode;
    }

    public Address() {}
}
