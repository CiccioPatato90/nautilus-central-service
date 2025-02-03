package org.acme.controller.settings;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.Association;
import org.acme.repository.AssociationService;
import org.acme.service.UtenteService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.util.List;

@Path("/api/settings/assoc-mgmt")
public class AssociationMgmtController {
    @Inject
    AssociationService associationService;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public List<Association> list() {
        List<Association> list = (List<Association>) associationService.listAll();
        return list;
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response addAssociation(@RequestBody Association association) {
        var id = associationService.addAssociation(association);
        return Response.ok().build();
    }

    @POST
    @Path("/edit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response editAssociation(@RequestBody Association association) {
        var id = associationService.editAssociation(association);
        return Response.ok().build();
    }

    @POST
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response deleteAssociation(@PathParam("id") Long id) {
        associationService.deleteAssociation(id);
        return Response.ok().build();
    }


    @POST
    @Path("/get/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Association getAssociation(@PathParam("id") Integer id) {
        var assoc = associationService.find("id", id).firstResult();
        return assoc;
    }
}
