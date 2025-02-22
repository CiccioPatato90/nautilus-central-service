package org.acme.service.requests;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.AssociationDTO;
import org.acme.dto.AssociationRequestDTO;
import org.acme.dto.InventoryRequestDTO;
import org.acme.dto.ProjectRequestDTO;
import org.acme.model.requests.association.AssociationRequest;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.Association;
import org.acme.model.enums.requests.RequestStatus;
import org.acme.model.requests.common.RequestCommand;
import org.acme.dao.requests.AssociationRequestDAO;
import org.acme.service.settings.AssociationSettingsService;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;

@ApplicationScoped
public class AssociationRequestService{
    @Inject
    AssociationRequestDAO associationRequestDAO;
    @Inject
    AssociationSettingsService associationSettingsService;
    @Inject
    CommonRequestService commonRequestService;

    public List<AssociationRequestDTO> getList(RequestFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return associationRequestDAO.findAll().list().stream().map(AssociationRequestDTO::fromEntity).collect(Collectors.toList());
        }

        // Build the query dynamically using a Map
        Map<String, Object> queryMap = new HashMap<>();

        addFilter(queryMap, "associationName", filter.getAssociationName(), true);
        addFilter(queryMap, "location", filter.getLocation(), true);
        addFilter(queryMap, "status", filter.getStatus().toString(), false);

        // Date range filter
        if (filter.getDateFrom() != null && filter.getDateTo() != null) {
            queryMap.put("date", Map.of("$gte", filter.getDateFrom(), "$lte", filter.getDateTo()));
        }

        Bson bsonQuery = new Document(queryMap);

        return associationRequestDAO.find(bsonQuery).list().stream().map(AssociationRequestDTO::fromEntity).collect(Collectors.toList());
    }

    public String add(AssociationRequestDTO request) {
        var req = AssociationRequestDTO.toEntity(request);

        req.createdAt = String.valueOf(Instant.now());
        req.updatedAt = req.createdAt;
        req.status = RequestStatus.PENDING;

        var id = this.persist(req);
        System.out.println("Saved Request with ID: "+ id);
        return id;
    }

    public String persist(AssociationRequest request) {
        this.associationRequestDAO.persist(request);
        return request._id.toString();
    }


    public String approveRequest(RequestCommand command) {
        // 1. Retrieve the request from MongoDB
//        var req = orgRequestDAO.findById(new ObjectId(command.getObjectMongoId()));
        var req = associationRequestDAO.findById(commonRequestService.getObjectId(command.getRequestId()));
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

    private void addFilter(Map<String, Object> queryMap, String field, String value, boolean useRegex) {
        if (!isNullOrEmpty(value)) {
            if (useRegex) {
                queryMap.put(field, Map.of("$regex", value, "$options", "i")); // Case-insensitive regex
            } else {
                queryMap.put(field, value);
            }
        }
    }

    public AssociationRequestDTO findByObjectId(String requestId) {
        var req = associationRequestDAO.findById(commonRequestService.getObjectId(requestId));
        req.setAssociationConfirmed(true);
        return AssociationRequestDTO.fromEntity(req);
    }

    public Boolean checkAssociationConfirmed(String associationReqId) {
        var req = findByObjectId(associationReqId);
        return req.getAssociationConfirmed();
    }

    public String getSqlId(String associationReqId) {
        var req = findByObjectId(associationReqId);
        return req.getAssociationSQLId();
    }

    public String getObjectId(String associationSqlId) {
        var req = associationRequestDAO.findBySqlId(associationSqlId);
        return req.get_id().toString();
    }
}
