package org.acme.service.requests;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.ProjectRequestDTO;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.enums.requests.RequestType;
import org.acme.model.requests.base.BaseRequest;
import org.acme.model.requests.common.RequestCommand;
import org.acme.model.response.requests.RequestCommonData;
import org.acme.model.response.requests.RequestListResponse;
import org.acme.service.settings.AssociationSettingsService;
import org.acme.service.settings.ItemSettingsService;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class RequestService {
    @Inject
    AssociationRequestService associationRequestService;
    @Inject
    InventoryRequestService inventoryRequestService;
    @Inject
    ProjectRequestService projectRequestService;


    @Inject
    AssociationSettingsService associationSettingsService;
    @Inject
    ItemSettingsService itemSettingsService;

    private Map<RequestType, RequestInterface> requestHandlers = new HashMap<>();


    @PostConstruct
    void init() {
        // do something
        this.requestHandlers = Map.of(
                RequestType.ASSOCIATION_REQUEST, associationRequestService,
                RequestType.INVENTORY_REQUEST, inventoryRequestService,
                RequestType.PROJECT_REQUEST, projectRequestService
        );
    }

    public RequestListResponse get(RequestFilter filter) {
        List<BaseRequest> allRequests = new ArrayList<>();

        switch(filter.getRequestType()){
            case ASSOCIATION_REQUEST -> allRequests.addAll(associationRequestService.getList(filter));
            case INVENTORY_REQUEST -> allRequests.addAll(inventoryRequestService.getList(filter));
            case PROJECT_REQUEST -> allRequests.addAll(projectRequestService.getList(filter));
            case VIEW_ALL_LIST -> allRequests.addAll(getList(filter));
        };

        RequestListResponse response = new RequestListResponse();

        response.setAllRequests(allRequests); // Assign sorted list
        response.setFilters(filter);

        return response;
    }

    /**
     * Fetches requests based on the filter criteria.
     */
//    public RequestListResponse get(RequestFilter filter) {
//        List<BaseRequest> allRequests = Optional.ofNullable(requestHandlers.get(filter.getRequestType()))
//                .map(handler -> handler.getList(filter))
//                .orElseGet(() -> this.getList(filter)); // If VIEW_ALL_LIST, call getList()
//        var resp = new RequestListResponse();
//        resp.setFilters(filter);
//        resp.setAllRequests(allRequests);
//        return resp;
//    }

    /**
     * TODO: CONVERT TO PASS ALSO REQUEST TYPE INSTEAD OF ONLY ID
     */
    public BaseRequest getByRequestId(String id) {
        if(id.startsWith("IR")){
            return inventoryRequestService.findByRequestId(id);
        }else if(id.startsWith("JR")){
            return associationRequestService.findByRequestId(id);
        }else if(id.startsWith("PR")){
            return projectRequestService.findByRequestId(id);
        }
        else{
            return null;
        }
    }


//    public List<? extends BaseRequest> getList(RequestFilter filter) {
//
//        List<BaseRequest> merge = new ArrayList<>();
//
//        // Collect all request types into the allRequests list
//        for (RequestType type : RequestType.values()) {
//            switch (type) {
//                case ASSOCIATION_REQUEST -> merge.addAll(associationRequestService.getList(filter));
//                case INVENTORY_REQUEST -> merge.addAll(inventoryRequestService.getList(filter));
//                case PROJECT_REQUEST -> merge.addAll(inventoryRequestService.getList(filter));
//            }
//        }
//
//        // Sort all requests by timestamp in descending order
//        merge = merge.stream()
//                .sorted(Comparator.comparing(
//                        r -> Instant.parse(r.getUpdatedAt()), // Convert to Instant (handles "Z" automatically)
//                        Comparator.reverseOrder()) // Sort descending (newest first)
//                )
//                .collect(Collectors.toList());
//
//        return merge;
//    }


    public List<? extends BaseRequest> getList(RequestFilter filter) {
        return requestHandlers.values().stream()
                .flatMap(handler -> handler.getList(filter).stream())
                .sorted(Comparator.comparing(
                        r -> Instant.parse(r.getUpdatedAt()), Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

//    public String approveRequest(RequestCommand command) {
//        return switch(command.getRequestType()){
//            case ASSOCIATION_REQUEST -> associationRequestService.approveRequest(command);
//            case INVENTORY_REQUEST -> inventoryRequestService.approveRequest(command);
//            case PROJECT_REQUEST -> inventoryRequestService.approveRequest(command);
//            case VIEW_ALL_LIST -> null;
//        };
//    }
    /**
     * Approves a request based on its type.
     */
    public String approveRequest(RequestCommand command) {
        return Optional.ofNullable(requestHandlers.get(command.getRequestType()))
                .map(handler -> handler.approveRequest(command))
                .orElse(null);
    }

    public RequestCommonData getCommonData() {
        var commonData = new RequestCommonData();
        commonData.setAssociationsList(associationSettingsService.listAll());
        commonData.setItemsList(itemSettingsService.getItems());
        return commonData;
    }


//    public RequestListResponse addRequest(RequestCommand command) {
//        return switch(command.getRequestType()){
//            case ASSOCIATION_REQUEST -> associationRequestService.add(command.getRequest());
//            case INVENTORY_REQUEST -> inventoryRequestService.add(command.getRequest());
//            case PROJECT_REQUEST -> projectRequestService.add(ProjectRequestDTO.toEntity());
//            case VIEW_ALL_LIST -> null;
//        };
//    }
    /**
     * Adds a new request of a specific type.
     */
    public RequestListResponse addRequest(RequestCommand command) {
        return Optional.ofNullable(requestHandlers.get(command.getRequestType()))
                .map(handler -> handler.add(command.getRequest()))
                .orElse(null);
    }
}
