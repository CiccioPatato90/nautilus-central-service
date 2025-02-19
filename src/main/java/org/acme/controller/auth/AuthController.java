package org.acme.controller.auth;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.TabDTO;
import org.acme.service.auth.UtenteService;

import java.util.Set;

@Path("/api/auth")
public class AuthController {
    @Inject
    UtenteService utenteService; // Dependency injection can replace this.

    @GET
    @Path("init")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Set<TabDTO> getTabs() {
        return utenteService.getUserTabs();
    }
}
