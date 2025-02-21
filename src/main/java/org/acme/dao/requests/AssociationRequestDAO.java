package org.acme.dao.requests;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.requests.association.AssociationRequest;

@ApplicationScoped
public class AssociationRequestDAO implements PanacheMongoRepository<AssociationRequest> {
    public AssociationRequest findByRequestId(String requestId) {
        return find("requestId", requestId).firstResult();
    }

    public AssociationRequest findBySqlId(String sqlId) {
        return find("associationSQLId", sqlId).firstResult();
    }
}
