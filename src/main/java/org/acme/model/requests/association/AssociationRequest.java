package org.acme.model.requests.association;

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
@MongoEntity(collection = "association_requests")
public class AssociationRequest extends BaseRequest {
    public ObjectId _id;  // Unique MongoDB identifier
    public String date;
    public ContactInfo contactInfo;
    public List<String> attachments;
    public String location;

    @Getter
    @Setter
    public static class ContactInfo {
        private String email;
        private String phone;
    }

//    @Getter
//    @Setter
//    public static class History {
//        private String changedBy;
//        private String timestamp;
//        private Changes changes;
//
//        @Getter
//        @Setter
//        public static class Changes {
//            private String motivation;
//        }
//    }
}
