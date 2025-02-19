package org.acme.controller.settings;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.virtual_warehouse.item.InventoryItem;
import org.acme.model.response.settings.ItemMgmtResponse;
import org.acme.service.settings.ItemSettingsService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/api/settings/item-mgmt")
public class ItemSettingsController {
    @Inject
    ItemSettingsService itemSettingsService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ItemMgmtResponse list() {
        var categories = itemSettingsService.getCategories();
        var items = itemSettingsService.getItems();
        var res = new ItemMgmtResponse();
        res.setCategories(categories);
        res.setItems(items);
        return res;
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addInventoryItem(@RequestBody InventoryItem item) {
        itemSettingsService.addItem(item);
        return Response.ok().build();
    }

    @POST
    @Path("/delete/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteInventoryItem(@PathParam("id") int id) {
        itemSettingsService.deleteItem(id);
        return Response.ok().build();
    }
}
