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
@MongoEntity(collection = "join")
public class JoinRequest {
    @Id
    private ObjectId _id; // MongoDB's _id field as ObjectId
    private String requestId; // Additional requestId field as String
    private String associationName;
    private String timestamp;
    private String date;
    private String motivation;
    private ContactInfo contactInfo;
    private String category;
    private String urgency;
    private List<String> attachments;
    private String location;
    private String status;
    private String assignedAdmin;
    private String priority;
    private String actionTaken;
    private String resolutionTimestamp;
    private String requestSource;
    private List<String> tags;
    private Integer processingTime;
    private List<History> history;

    @Getter
    @Setter
    public static class ContactInfo {
        private String email;
        private String phone;
    }

    @Getter
    @Setter
    public static class History {
        private String changedBy;
        private String timestamp;
        private Changes changes;

        @Getter
        @Setter
        public static class Changes {
            private String motivation;
        }
    }
}
