package org.acme.dao.requests;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.enums.requests.RequestStatus;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.requests.inventory.InventoryRequest;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class InventoryRequestDAO implements PanacheMongoRepository<InventoryRequest> {
    public Object addRequest(InventoryRequest inventoryRequest) {
        return null;
    }
    public List<InventoryRequest> getReq(RequestFilter filters) {
        return null;
    }
    public InventoryRequest findByRequestId(String requestId) {
        return find("requestId", requestId).firstResult();
    }
    public InventoryRequest findByObjectId(ObjectId objId) {
        return find("_id", objId).firstResult();
    }

    public List<InventoryRequest> findPendingRequestsByAssociationID(String associationRequestId) {
        return find("status = ?1 and associationReqId = ?2", RequestStatus.PENDING, associationRequestId).list();
    }
}
