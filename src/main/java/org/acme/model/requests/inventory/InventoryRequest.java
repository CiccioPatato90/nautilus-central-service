package org.acme.model.requests.inventory;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;
import org.acme.model.enums.requests.RequestStatus;
import org.bson.types.ObjectId;
import java.util.List;
import java.util.Map;

@GenerateDTO
@Getter
@Setter
@NoArgsConstructor
@MongoEntity(collection = "inventory_requests")
public class InventoryRequest {
    public ObjectId _id;  // Unique MongoDB identifier
    public String associationReqId;  // Mongo Id of association issuing a request
    public long associationSqlId;
    public RequestStatus status;  // Request status (Pending, Approved, etc.)
    public String updatedAt;
    public String createdAt;
    public List<InventoryChange> inventoryChanges;
    public String approvedBy;
}
