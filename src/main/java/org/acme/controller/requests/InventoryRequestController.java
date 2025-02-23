package org.acme.controller.requests;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.model.response.requests.inventory.SimulateCommand;
import org.acme.model.response.requests.inventory.SimulateRequestResponse;
import org.acme.service.requests.InventoryRequestService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/api/requests/inv")
public class InventoryRequestController {
    @Inject
    InventoryRequestService inventoryRequestService;


    @POST
    @Path("sim")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public SimulateRequestResponse approveRequest(@RequestBody SimulateCommand command) {
//        var res = inventoryRequestService.simulateRequestLinearProgramming(command);
        var res = inventoryRequestService.simulateRequestGreedy(command);
        return res;
    }
}
