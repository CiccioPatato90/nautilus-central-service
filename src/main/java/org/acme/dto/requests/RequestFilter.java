package org.acme.dto.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestFilter {
    private String associationName;
    private String category;
    private String urgency;
    private String status;
    private String assignedAdmin;
    private String location;
    private List<String> tags;
    private String dateFrom;
    private String dateTo;

    public boolean isEmpty() {
        return isNullOrEmpty(associationName) &&
                isNullOrEmpty(category) &&
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
