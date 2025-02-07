package org.acme.model.requests;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@MongoEntity(collection = "inventory_requests")
public class InventoryRequest extends BaseRequest {
    public ObjectId _id;  // Unique MongoDB identifier
    public List<InventoryChange> inventoryChanges;
    public String approvedBy;
    public String approvalDate;
    public String requestSource;

    @Getter
    @Setter
    public static class InventoryChange {
        public String itemId;  // Reference to MySQL inventory item
        public String itemName;
        public int previousQuantity;
        public int requestedQuantity;
        public String changeType;  // Increase or Decrease
    }
}
