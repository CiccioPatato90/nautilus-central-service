package org.acme.model.requests;

import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.model.enums.RequestStatus;
import org.acme.model.enums.RequestType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
//@MongoEntity(collection = "requests")
public abstract class BaseRequest {
    public String requestId;  // Unique MongoDB identifier
    public String associationReqId;  // Mongo Id of association issuing a request
    public String associationSQLId;  // MySQL Id of association issuing a request
    public boolean associationConfirmed = false;
    public String associationName;
    public RequestType requestType;  // Type of request: "JoinRequest", "InventoryRequest", "Generated" for Logging in algorithm execution
    public String motivation;
    public RequestStatus status;  // Request status (Pending, Approved, etc.)
    public List<String> tags;  // Common tagging system
    public String updatedAt;
    public String createdAt;
}