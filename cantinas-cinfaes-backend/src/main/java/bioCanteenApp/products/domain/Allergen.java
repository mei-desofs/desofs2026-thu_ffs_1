package bioCanteenApp.products.domain;

public enum Allergen {
    GLUTEN("Glúten"),
    CRUSTACEANS("Crustáceos"),
    EGGS("Ovos"),
    FISH("Peixe"),
    PEANUTS("Amendoins"),
    SOYBEANS("Soja"),
    MILK("Leite"),
    NUTS("Frutos de Casca Rija"),
    CELERY("Aipo"),
    MUSTARD("Mostarda"),
    LUPIN("Tremoço"),
    MOLLUSCS("Moluscos");

    private final String ptLabel;

    Allergen(String ptLabel) {
        this.ptLabel = ptLabel;
    }

    public String getPtLabel() {
        return ptLabel;
    }
}