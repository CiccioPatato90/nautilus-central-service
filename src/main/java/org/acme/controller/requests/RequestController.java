package org.acme.controller.requests;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.requests.common.RequestCommand;
import org.acme.model.response.requests.*;
import org.acme.service.requests.AssociationRequestService;
import org.acme.service.requests.InventoryRequestService;
import org.acme.service.requests.ProjectRequestService;
import org.acme.service.requests.CommonRequestService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/api/requests")
public class RequestController {
    @Inject
    CommonRequestService commonRequestService;
    @Inject
    AssociationRequestService associationRequestService;
    @Inject
    InventoryRequestService inventoryRequestService;
    @Inject
    ProjectRequestService projectRequestService;

    @GET
    @Path("common")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public RequestCommonData common() {
        var res = commonRequestService.getCommonData();
        return res;
    }


    @POST
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public RequestListResponse list(@RequestBody RequestFilter filters) {
        var res = new RequestListResponse();

        switch(filters.getRequestType()){
            case ASSOCIATION_REQUEST -> {
                res.setAssociationRequests(associationRequestService.getList(filters));
            }
            case INVENTORY_REQUEST -> {
                res.setInventoryRequests(inventoryRequestService.getList(filters));
            }
            case PROJECT_REQUEST -> {
                res.setProjectRequests(projectRequestService.getList(filters));
            }
        };

        return res;
    }


    @POST
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public AddRequestResponse addRequest(@RequestBody RequestCommand command) {
        var res = new AddRequestResponse();

        switch(command.getRequestType()){
            case ASSOCIATION_REQUEST -> {
                res.setRequestMongoID(associationRequestService.add(command.getAssociationRequestDTO()));
            }
            case INVENTORY_REQUEST -> {
                res.setRequestMongoID(inventoryRequestService.add(command.getInventoryRequestDTO()));
            }
            case PROJECT_REQUEST -> {
                res.setRequestMongoID(projectRequestService.add(command.getProjectRequestDTO()));
            }
        };

        return res;
    }


    @POST
    @Path("get")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public GetRequestResponse getRequest(@RequestBody RequestCommand command) {

        var res = new GetRequestResponse();

        switch(command.getRequestType()){
            case ASSOCIATION_REQUEST -> {
                res.setAssociationRequestDTO(associationRequestService.findByObjectId(command.getRequestId()));
            }
            case INVENTORY_REQUEST -> {
                res.setInventoryRequestDTO(inventoryRequestService.findByObjectId(command.getRequestId()));
            }
            case PROJECT_REQUEST -> {
                res.setProjectRequestDTO(projectRequestService.findByObjectId(command.getRequestId()));
            }
        };

        return res;
    }


    @POST
    @Path("approve")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public ApproveRequestResponse approveRequest(@RequestBody RequestCommand command) {
        var res = new ApproveRequestResponse();

        switch(command.getRequestType()){
            case ASSOCIATION_REQUEST -> {
                associationRequestService.approveRequest(command);
            }
            case INVENTORY_REQUEST -> {
                inventoryRequestService.approveRequest(command);
            }
            case PROJECT_REQUEST -> {
                projectRequestService.approveRequest(command);
            }
        };

        return res;
    }
}
