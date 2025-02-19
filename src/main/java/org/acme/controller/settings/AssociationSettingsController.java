package org.acme.controller.settings;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.AssociationDTO;
import org.acme.service.settings.AssociationSettingsService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.util.List;

@Path("/api/settings/assoc-mgmt")
public class AssociationSettingsController {
    @Inject
    AssociationSettingsService associationSettingsService;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public List<AssociationDTO> list() {
        var list = associationSettingsService.listAll();
        return list;
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response addAssociation(@RequestBody AssociationDTO association) {
        var id = associationSettingsService.addAssociation(association);
        return Response.ok().build();
    }

    @POST
    @Path("/edit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response editAssociation(@RequestBody AssociationDTO association) {
        var id = associationSettingsService.editAssociation(association);
        return Response.ok().build();
    }

    @POST
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response deleteAssociation(@PathParam("id") Long id) {
        associationSettingsService.deleteAssociation(id);
        return Response.ok().build();
    }


    @POST
    @Path("/get/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public AssociationDTO getAssociation(@PathParam("id") Long id) {
        var assoc = associationSettingsService.findById(id);
        return assoc;
    }
}
