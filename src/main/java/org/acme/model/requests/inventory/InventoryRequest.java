package org.acme.model.requests.inventory;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;
import org.acme.model.requests.base.BaseRequest;
import org.bson.types.ObjectId;
import java.util.List;

@GenerateDTO
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
}
