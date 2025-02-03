package org.acme.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.model.settings.Tab;
import org.acme.service.UtenteService;
import org.acme.service.auth.AuthService;

import java.util.Set;

@Path("/api/auth")
public class AuthController {
    @Inject
    AuthService authService; // Dependency injection can replace this.
    @Inject
    UtenteService utenteService; // Dependency injection can replace this.


    @GET
    @Path("init")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Set<Tab> getTabs() {
        return utenteService.getUserTabs();
    }
}
