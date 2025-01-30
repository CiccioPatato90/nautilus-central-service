package org.acme.dto.requests;

import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link org.acme.model.requests.JoinRequest}
 */
@Value
@NoArgsConstructor(force = true)
public class JoinRequestDto implements Serializable {
    String associationName;
    String timestamp;
    String date;
    String motivation;
    ContactInfoDto contactInfo;
    String category;
    String urgency;
    String location;
    String status;
    String assignedAdmin;
    String priority;
    String actionTaken;
    String resolutionTimestamp;
    String requestSource;
    List<String> tags;
    Integer processingTime;
    List<HistoryDto> history;

    /**
     * DTO for {@link org.acme.model.requests.JoinRequest.ContactInfo}
     */
    @Value
    public static class ContactInfoDto implements Serializable {
        String email;
        String phone;
    }

    /**
     * DTO for {@link org.acme.model.requests.JoinRequest.History}
     */
    @Value
    public static class HistoryDto implements Serializable {
        String changedBy;
        String timestamp;
    }
}