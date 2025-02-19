package org.acme.service.requests;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.AssociationDTO;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.Association;
import org.acme.model.enums.requests.RequestStatus;
import org.acme.model.requests.base.BaseRequest;
import org.acme.model.requests.association.AssociationRequest;
import org.acme.model.requests.common.RequestCommand;
import org.acme.model.response.requests.RequestListResponse;
import org.acme.dao.requests.AssociationRequestDAO;
import org.acme.service.settings.AssociationSettingsService;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.acme.model.enums.requests.RequestType.VIEW_ALL_LIST;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@ApplicationScoped
public class AssociationRequestService implements RequestInterface {
    @Inject
    AssociationRequestDAO associationRequestDAO;
    @Inject
    AssociationSettingsService associationSettingsService;

    @Override
    public List<? extends BaseRequest> getList(RequestFilter filter) {
        if (filter == null || filter.isEmpty() || filter.getRequestType().equals(VIEW_ALL_LIST)) {
            var list = associationRequestDAO.findAll().list();
            return list;
        }

        // Build the query dynamically using a Map
        Map<String, Object> queryMap = new HashMap<>();

        addFilter(queryMap, "associationName", filter.getAssociationName(), true);
        addFilter(queryMap, "location", filter.getLocation(), true);
        addFilter(queryMap, "status", filter.getStatus().toString(), false);

        // Tags filter (Array field)
//        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
//            queryMap.put("tags", Map.of("$in", filter.getTags()));
//        }

        // Date range filter
        if (filter.getDateFrom() != null && filter.getDateTo() != null) {
            queryMap.put("date", Map.of("$gte", filter.getDateFrom(), "$lte", filter.getDateTo()));
        }

        Bson bsonQuery = new Document(queryMap);

        return associationRequestDAO.find(bsonQuery).list();
    }

    @Override
    public RequestListResponse add(Class<? extends BaseRequest> request) {
//        return List.of();
        return null;
    }

    public int persist(AssociationRequest request) {
        this.associationRequestDAO.persist(request);
        return 1;
    }

    @Override
    public BaseRequest findByRequestId(String id) {
        var req = associationRequestDAO.findByRequestId(id);
        req.setAssociationConfirmed(true);
        return req;
    }


    @Override
    public String approveRequest(RequestCommand command) {
        // 1. Retrieve the request from MongoDB
//        var req = orgRequestDAO.findById(new ObjectId(command.getObjectMongoId()));
        var req = associationRequestDAO.findByRequestId(command.getRequestMongoId());
        if (req == null) {
            throw new IllegalArgumentException("Request not found");
        }
        var model = reqToModel(req);

        // 2. Update fields in MongoDB object
        req.setAssociationConfirmed(true);
        String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        req.setUpdatedAt(now);
        req.setStatus(RequestStatus.APPROVED);

        // 3. Insert into MySQL
        Long assocSQLId;
        try {
            assocSQLId = associationSettingsService.addAssociation(AssociationDTO.fromEntity(model));
            req.setAssociationSQLId(String.valueOf(assocSQLId));
        } catch (Exception e) {
            // Handle MySQL error
            throw new RuntimeException("Failed to add association to MySQL", e);
        }

        // 4. Update MongoDB (non-transactional)
        try {
            associationRequestDAO.persistOrUpdate(req);
        } catch (Exception e) {
            // At this point, MySQL is updated but MongoDB failed.
            // You can implement a compensating action for MySQL or flag the inconsistency.
            associationSettingsService.deleteAssociation(assocSQLId);
            throw new RuntimeException("MongoDB update failed. Rolled back MySQL.", e);

        }

        return "Request approved and migrated.";
    }


//    private void addFilter(Map<String, Object> queryMap, String field, String value, boolean useRegex) {
//        if (!isNullOrEmpty(value)) {
//            if (useRegex) {
//                queryMap.put(field, Map.of("$regex", value, "$options", "i")); // Case-insensitive regex
//            } else {
//                queryMap.put(field, value);
//            }
//        }
//    }

    private Association reqToModel(AssociationRequest req){
        var model = new Association();
        model.setName(req.getAssociationName());
        model.setAddress(req.getLocation());
        model.setEmail(req.getContactInfo().getEmail());
//        model.setWebsite(req.get());
        model.setPhone(req.getContactInfo().getEmail());
//        model.setRemarks();
        return model;
    }

    public void delete(AssociationRequest req){
        this.associationRequestDAO.delete(req);
    }
}
