package bioCanteenApp.provisioning.dto;

public record ProductionOrderDTO(
    String supplierName,
    String productName,
    Double quantityToOrder,
    String status
){}
