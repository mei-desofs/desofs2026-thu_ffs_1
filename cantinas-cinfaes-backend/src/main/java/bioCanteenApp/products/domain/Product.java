    package bioCanteenApp.products.domain;

    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;

    import java.util.List;

    @Entity
    @Table(name = "products")
    @Getter
    @Setter
    public class Product {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column
        private String name;

        @Column
        private String unit;

        @Column
        private Integer expirationDays;

        @ElementCollection(targetClass = Season.class)
        @CollectionTable(name = "product_seasonal_year", joinColumns = @JoinColumn(name = "product_id"))
        @Enumerated(EnumType.STRING)
        @Column(name = "month")
        private List<Season> seasons;

        @ElementCollection(targetClass = Allergen.class)
        @CollectionTable(name = "product_allergens", joinColumns = @JoinColumn(name = "product_id"))
        @Enumerated(EnumType.STRING)
        @Column(name = "allergen")
        private List<Allergen> allergens;

        public Product(String name, String unit, Integer expirationDays, List<Season> seasons, List<Allergen> allergens) {
            this.name = name;
            this.unit = unit;
            this.expirationDays = expirationDays;
            this.seasons = seasons;
            this.allergens = allergens;
        }

        public Product (Long id) {
            this.id = id;
        }

        protected Product() { }
    }
