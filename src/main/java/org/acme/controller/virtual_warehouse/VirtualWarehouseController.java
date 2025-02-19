package org.acme.controller.virtual_warehouse;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.model.response.virtual_warehouse.VirtualWarehouseResponse;
import org.acme.service.virtual_warehouse.VirtualWarehouseService;

@Path("/api/vw")
public class VirtualWarehouseController {
    @Inject
    VirtualWarehouseService virtualWarehouseService;

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public VirtualWarehouseResponse list() {
        var response = new VirtualWarehouseResponse();
        var warehouseMap = virtualWarehouseService.getMap();
        var warehouses = virtualWarehouseService.getWarehouseList();
        response.setWarehouseMap(warehouseMap);
        response.setWarehouses(warehouses);
        return response;
    }


}
