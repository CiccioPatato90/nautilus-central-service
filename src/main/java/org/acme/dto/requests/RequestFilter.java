package org.acme.dto.requests;

import lombok.Getter;
import lombok.Setter;
import org.acme.model.enums.RequestStatus;
import org.acme.model.enums.RequestType;

import java.util.List;

@Getter
@Setter
public class RequestFilter {
    private RequestType requestType;
    private String associationName;
    private String associationId;
//    MAPPED TO true/false
    private String associationConfirmed;
    private RequestStatus status;
    private String location;
    private String dateFrom;
    private String dateTo;

    public boolean isEmpty() {
        return isNullOrEmpty(associationName) &&
                isNullOrEmpty(requestType.toString()) &&
                isNullOrEmpty(status.toString()) &&
                isNullOrEmpty(location) &&
                isNullOrEmpty(dateFrom) &&
                isNullOrEmpty(dateTo);
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
