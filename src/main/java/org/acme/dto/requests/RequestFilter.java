package org.acme.dto.requests;

import lombok.Getter;
import lombok.Setter;
import org.acme.model.enums.RequestType;

import java.util.List;

@Getter
@Setter
public class RequestFilter {
    private String associationName;
    private String associationId;
    private RequestType requestType;
    private String urgency;
    private String status;
    private String assignedAdmin;
    private String location;
    private List<String> tags;
    private String dateFrom;
    private String dateTo;

    public boolean isEmpty() {
        return isNullOrEmpty(associationName) &&
                isNullOrEmpty(requestType.toString()) &&
                isNullOrEmpty(urgency) &&
                isNullOrEmpty(status) &&
                isNullOrEmpty(assignedAdmin) &&
                isNullOrEmpty(location) &&
                (tags == null || tags.isEmpty()) &&
                isNullOrEmpty(dateFrom) &&
                isNullOrEmpty(dateTo);
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
