package org.acme.model.requests.association;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;
import org.acme.model.enums.requests.RequestStatus;
import org.bson.types.ObjectId;
import java.util.List;

@GenerateDTO
@Getter
@Setter
@NoArgsConstructor
@MongoEntity(collection = "association_requests")
public class AssociationRequest {
    public ObjectId _id;  // Unique MongoDB identifier
    public String associationName;
    public Boolean associationConfirmed;
    public String associationSQLId;  // MySQL Id of association issuing a request
    public String motivation;
    public RequestStatus status;  // Request status (Pending, Approved, etc.)
    public String updatedAt;
    public String createdAt;

    public ContactInfo contactInfo;
    public List<String> attachments;
    public String location;


    @Getter
    @Setter
    public static class ContactInfo {
        private String email;
        private String phone;
    }
}