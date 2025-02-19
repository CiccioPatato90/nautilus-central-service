package org.acme.model.response.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.model.requests.association.AssociationRequest;
import org.acme.model.requests.inventory.InventoryRequest;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.enums.requests.RequestType;
import org.acme.model.requests.base.BaseRequest;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RequestListResponse {
    List<AssociationRequest> associationRequests = new ArrayList<>();
    List<InventoryRequest> inventoryRequests = new ArrayList<>();
    List<? extends BaseRequest> allRequests = new ArrayList<>();
    RequestFilter filters;
    RequestType requestTypeEnum;
}