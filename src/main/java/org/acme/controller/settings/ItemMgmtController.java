package org.acme.controller.settings;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.InventoryItem;
import org.acme.model.response.settings.ItemMgmtResponse;
import org.acme.service.settings.ItemMgmtService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/api/settings/item-mgmt")
public class ItemMgmtController {
    @Inject
    ItemMgmtService itemMgmtService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ItemMgmtResponse list() {
        var categories = itemMgmtService.getCategories();
        var items = itemMgmtService.getItems();
        var res = new ItemMgmtResponse();
        res.setCategories(categories);
        res.setItems(items);
        return res;
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addInventoryItem(@RequestBody InventoryItem item) {
        itemMgmtService.addItem(item);
        return Response.ok().build();
    }

    @POST
    @Path("/delete/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteInventoryItem(@PathParam("id") int id) {
        itemMgmtService.deleteItem(id);
        return Response.ok().build();
    }
}
