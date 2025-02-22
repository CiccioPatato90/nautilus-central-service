package org.acme.dao.requests;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.enums.requests.RequestStatus;
import org.acme.model.requests.inventory.InventoryRequest;
import org.acme.model.requests.project.ProjectRequest;

import java.util.List;

@ApplicationScoped
public class ProjectRequestDAO implements PanacheMongoRepository<ProjectRequest> {
    public List<ProjectRequest> findPendingRequestsByAssociationID(String associationRequestId) {
        return find("status = ?1 and associationReqId = ?2", RequestStatus.PENDING, associationRequestId).list();
    }

    public List<ProjectRequest> findByRequestStatus(RequestStatus requestStatus) {
        return find("status = ?1", requestStatus).list();
    }
}
