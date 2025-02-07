package org.acme.interfaces;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.Set;

@Path("/api/vw")
@RegisterRestClient(configKey = "virtual-warehouse-api")
public interface VirtualWarehouseService {
    @GET
    @Path("/requests")
    Set<String> getById(@RestQuery String id);
}
