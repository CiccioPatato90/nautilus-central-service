package org.acme.model.requests;

import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.model.enums.RequestType;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
//@MongoEntity(collection = "requests")
public abstract class BaseRequest {
    @Id
    private ObjectId _id;  // Unique MongoDB identifier
    private String requestId;  // Unique MongoDB identifier
    private String associationId;  // Common reference to the organization
    private RequestType requestType;  // Type of request: "JoinRequest", "InventoryRequest", "Generated" for Logging in algorithm execution
    private String motivation;
    private String status;  // Request status (Pending, Approved, etc.)
    private List<String> tags;  // Common tagging system
    private String updatedAt;
    private String createdAt;
}