package org.acme.controller.settings;


import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.model.settings.Tab;
import org.acme.service.settings.SettingsService;
import java.util.Set;

@Path("/api/settings")
public class SettingsController {
    @Inject
    SettingsService settingsService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Set<Tab> list() {
        return settingsService.getTabs();
    }
}
