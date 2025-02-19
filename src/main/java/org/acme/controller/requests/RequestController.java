package org.acme.controller.requests;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.requests.association.AssociationRequest;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.requests.base.BaseRequest;
import org.acme.model.requests.common.RequestCommand;
import org.acme.model.response.requests.RequestCommonData;
import org.acme.model.response.requests.RequestListResponse;
import org.acme.service.requests.RequestService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/api/requests")
public class RequestController {

    @Inject
    RequestService requestService;

    @POST
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public RequestListResponse list(@RequestBody RequestFilter filters) {
        var res = requestService.get(filters);
        return res;
    }

    @GET
    @Path("common")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public RequestCommonData common() {
        var res = requestService.getCommonData();
        return res;
    }


    @POST
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public String addRequest(@RequestBody RequestCommand command) {
//        TODO: FIX THIS!!!
        var reqId = requestService.addRequest(command);
        return " FAKE ";
    }


    @POST
    @Path("get/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public BaseRequest getRequest(@PathParam("id") String id) {
        var req = requestService.getByRequestId(id);
        return req;
    }


    @POST
    @Path("approve")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response approveRequest(@RequestBody RequestCommand command) {
        var req = requestService.approveRequest(command);
        return Response.ok(req).build();
    }
}
