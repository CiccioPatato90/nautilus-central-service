package org.acme.model.requests;

import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@MongoEntity(collection = "association_requests")
public class JoinRequest extends BaseRequest {
    private String associationName;
    private String date;
    private ContactInfo contactInfo;
    private List<String> attachments;
    private String location;

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
