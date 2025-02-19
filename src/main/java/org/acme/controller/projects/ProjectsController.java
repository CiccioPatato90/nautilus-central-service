package org.acme.controller.projects;


import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.model.response.projects.ProjectsListResponse;
import org.acme.service.projects.ProjectsService;

@Path("/api/projects")
public class ProjectsController {

    @Inject
    ProjectsService projectsService;

    @POST
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public ProjectsListResponse list() {
//        @RequestBody ProjectsFilter filters
        var res = new ProjectsListResponse();
        var list = projectsService.list();
        res.setProjectRequestList(list);
        return res;
    }
}
