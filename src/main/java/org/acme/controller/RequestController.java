package org.acme.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.requests.RequestFilter;
import org.acme.model.requests.JoinRequest;
import org.acme.model.response.RequestListResponse;
import org.acme.service.requests.RequestService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/api/requests")
public class RequestController {

//    @RestClient
//    RequestService service;

//    @Inject
//    OrgRequestService orgRequestService;
//    @Inject
//    InventoryRequestService inventoryRequestService;

    @Inject
    RequestService requestService;

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @RolesAllowed("user")
//    public RequestListResponse commonData(@RequestBody JoinRequest joinRequest) {
//        return null;
//    }

    @POST
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public RequestListResponse list(@RequestBody RequestFilter filters) {
        var res = requestService.get(filters);
        return res;
    }


    @POST
    @Path("add")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public String addRequest(@RequestBody JoinRequest joinRequest) {
//        var reqId = orgRequestService.addRequest(joinRequest);
        return " FAKE ";
    }
//
//
//    @POST
//    @Path("getOrg")
//    @Produces(MediaType.APPLICATION_JSON)
//    @RolesAllowed("admin")
//    public RequestListResponse getOrg(@RequestBody RequestFilter filters) {
//        var list = orgRequestService.getReq(filters);
//        var res = new RequestListResponse();
//        res.setJoinRequests(list);
//        res.setFilters(filters);
//        return res;
//    }
//
//    @POST
//    @Path("inv")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @RolesAllowed("user")
//    public String addInv(@RequestBody InventoryRequest inventoryRequest) {
//        var reqId = inventoryRequestService.addRequest(inventoryRequest);
//        return "{\"processed\": " + reqId + "}";
//    }
//
//
//    @POST
//    @Path("getinv")
//    @Produces(MediaType.APPLICATION_JSON)
//    @RolesAllowed("admin")
//    public RequestListResponse getInv(@RequestBody RequestFilter filters) {
//        var list = inventoryRequestService.getReq(filters);
//        var res = new RequestListResponse();
//        res.setInventoryRequests(list);
//        res.setFilters(filters);
//        return res;
//    }
}
