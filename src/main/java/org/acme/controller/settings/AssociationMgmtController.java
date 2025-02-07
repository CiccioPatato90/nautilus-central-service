package org.acme.controller.settings;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.Association;
import org.acme.repository.AssociationDAO;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/api/settings/assoc-mgmt")
public class AssociationMgmtController {
    @Inject
    AssociationDAO associationDAO;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public List<Association> list() {
        List<Association> list = (List<Association>) associationDAO.listAll();
        list.stream().forEach(association -> association.setInventoryBoxes(Set.of()));
        return list;
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response addAssociation(@RequestBody Association association) {
        var id = associationDAO.addAssociation(association);
        return Response.ok().build();
    }

    @POST
    @Path("/edit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response editAssociation(@RequestBody Association association) {
        var id = associationDAO.editAssociation(association);
        return Response.ok().build();
    }

    @POST
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response deleteAssociation(@PathParam("id") Long id) {
        associationDAO.deleteAssociation(id);
        return Response.ok().build();
    }


    @POST
    @Path("/get/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Association getAssociation(@PathParam("id") Integer id) {
        var assoc = associationDAO.find("id", id).firstResult();
        return assoc;
    }
}
