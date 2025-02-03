package org.acme.repository.requests;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.dto.requests.RequestFilter;
import org.acme.model.requests.InventoryRequest;

import java.util.List;

@ApplicationScoped
public class InventoryRequestDAO implements PanacheMongoRepository<InventoryRequest> {
    public Object addRequest(InventoryRequest inventoryRequest) {
        return null;
    }

    public List<InventoryRequest> getReq(RequestFilter filters) {
        return null;
    }
}
