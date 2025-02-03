package org.acme.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.dto.requests.RequestFilter;
import org.acme.model.enums.RequestType;
import org.acme.model.requests.BaseRequest;
import org.acme.model.requests.InventoryRequest;
import org.acme.model.requests.JoinRequest;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RequestListResponse {
    List<JoinRequest> joinRequests = new ArrayList<>();
    List<InventoryRequest> inventoryRequests = new ArrayList<>();
    List<? extends BaseRequest> allRequests = new ArrayList<>();
    RequestFilter filters;
    RequestType requestTypeEnum;
}