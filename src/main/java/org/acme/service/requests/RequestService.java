package org.acme.service.requests;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.requests.RequestFilter;
import org.acme.model.enums.RequestType;
import org.acme.model.requests.BaseRequest;
import org.acme.model.requests.JoinRequest;
import org.acme.model.response.RequestListResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.acme.model.enums.RequestType.INVENTORY_REQUEST;
import static org.acme.model.enums.RequestType.ORGANIZATION_JOIN_REQUEST;

@ApplicationScoped
public class RequestService implements RequestInterface {

    @Inject
    OrgRequestService orgRequestService;

    @Inject
    InventoryRequestService inventoryRequestService;

    public RequestListResponse get(RequestFilter filter) {
        List<BaseRequest> allRequests = new ArrayList<>();

        switch(filter.getRequestType()){
            case ORGANIZATION_JOIN_REQUEST -> allRequests.addAll(orgRequestService.getList(filter));
            case INVENTORY_REQUEST -> allRequests.addAll(inventoryRequestService.getList(filter));
            case VIEW_ALL_LIST -> allRequests.addAll(getList(filter));
        };

        RequestListResponse response = new RequestListResponse();

        response.setAllRequests(allRequests); // Assign sorted list
        response.setFilters(filter);

        return response;
    }

//    private RequestListResponse mapToResponse(List<JoinRequest> req, RequestFilter filters) {
//        var res = new RequestListResponse();
//        res.setJoinRequests(req);
//        res.setFilters(filters);
//        return res;
//    }


    @Override
    public List<? extends BaseRequest> getList(RequestFilter filter) {

        List<BaseRequest> merge = new ArrayList<>();

        // Collect all request types into the allRequests list
        for (RequestType type : RequestType.values()) {
            switch (type) {
                case ORGANIZATION_JOIN_REQUEST -> merge.addAll(orgRequestService.getList(filter));
                case INVENTORY_REQUEST -> merge.addAll(inventoryRequestService.getList(filter));
            }
        }

        // Sort all requests by timestamp in descending order
        merge = merge.stream()
                .sorted(Comparator.comparing(
                        r -> Instant.parse(r.getUpdatedAt()), // Convert to Instant (handles "Z" automatically)
                        Comparator.reverseOrder()) // Sort descending (newest first)
                )
                .collect(Collectors.toList());

        return merge;
    }

    @Override
    public RequestListResponse add(Class<? extends BaseRequest> filter) {
//        return List.of();
        return null;
    }
}
