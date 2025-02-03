package org.acme.model.requests;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@MongoEntity(collection = "inventory_requests")
public class InventoryRequest extends BaseRequest {
    private List<InventoryChange> inventoryChanges;
    private String approvedBy;
    private String approvalDate;
    private String requestSource;

    @Getter
    @Setter
    public static class InventoryChange {
        private String itemId;  // Reference to MySQL inventory item
        private String itemName;
        private int previousQuantity;
        private int requestedQuantity;
        private String changeType;  // Increase or Decrease
    }
}
