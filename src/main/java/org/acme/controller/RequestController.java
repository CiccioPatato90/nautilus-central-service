package org.acme.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.requests.JoinRequestDto;
import org.acme.dto.requests.RequestFilter;
import org.acme.model.requests.JoinRequest;
import org.acme.service.JoinRequestService;
import org.acme.service.RequestService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Set;

@Path("/api/requests")
public class RequestController {

//    @RestClient
//    RequestService service;

    @Inject
    JoinRequestService joinRequestService;

    @POST
    @Path("add")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public String add(@RequestBody JoinRequest joinRequest) {
        var reqId = joinRequestService.add(joinRequest);
        return "{\"processed\": " + reqId + "}";
    }


    @POST
    @Path("get")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public List<JoinRequest> getRequests(@RequestBody RequestFilter filters) {
        return joinRequestService.getReq(filters);
    }
}
