package org.acme.service.requests;

import com.mongodb.client.MongoClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.requests.RequestFilter;
import org.acme.model.Association;
import org.acme.model.enums.RequestStatus;
import org.acme.model.requests.BaseRequest;
import org.acme.model.requests.JoinRequest;
import org.acme.model.requests.RequestCommand;
import org.acme.model.response.RequestListResponse;
import org.acme.repository.AssociationDAO;
import org.acme.repository.requests.OrgRequestDAO;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static org.acme.model.enums.RequestType.VIEW_ALL_LIST;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@ApplicationScoped
public class OrgRequestService implements RequestInterface {
    @Inject
    OrgRequestDAO orgRequestDAO;
    @Inject
    AssociationDAO associationDAO;

    @Inject
    MongoClient mongoClient;

    @Override
    public List<? extends BaseRequest> getList(RequestFilter filter) {
        if (filter == null || filter.isEmpty() || filter.getRequestType().equals(VIEW_ALL_LIST)) {
            var list = orgRequestDAO.findAll().list();
            return list;
//            return mapToResponse(list, filter);
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

//        var filteredList = orgRequestDAO.find(bsonQuery).list();
        return orgRequestDAO.find(bsonQuery).list();
    }

    @Override
    public RequestListResponse add(Class<? extends BaseRequest> filter) {
//        return List.of();
        return null;
    }

    @Override
    public BaseRequest findByRequestId(String id) {
        var req = orgRequestDAO.findByRequestId(id);
        req.setAssociationConfirmed(true);
        return req;
    }


    @Override
    public String approveRequest(RequestCommand command) {
        // 1. Retrieve the request from MongoDB
//        var req = orgRequestDAO.findById(new ObjectId(command.getObjectMongoId()));
        var req = orgRequestDAO.findByRequestId(command.getRequestMongoId());
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
            assocSQLId = associationDAO.addAssociation(model);
            req.setAssociationSQLId(String.valueOf(assocSQLId));
        } catch (Exception e) {
            // Handle MySQL error
            throw new RuntimeException("Failed to add association to MySQL", e);
        }

        // 4. Update MongoDB (non-transactional)
        try {

//            CodecRegistry pojoCodecRegistry = fromRegistries(
//                    mongoClient.getDatabase("db_requests").getCodecRegistry(),
//                    fromProviders(PojoCodecProvider.builder().automatic(true).build())
//            );
//            var database = mongoClient.getDatabase("db_requests").withCodecRegistry(pojoCodecRegistry);
//            var collection = database.getCollection("associations_requests", JoinRequest.class);
//
//            var session = mongoClient.startSession();
//            session.withTransaction(() -> collection.insertOne(session, req));
            orgRequestDAO.persistOrUpdate(req);
        } catch (Exception e) {
            // At this point, MySQL is updated but MongoDB failed.
            // You can implement a compensating action for MySQL or flag the inconsistency.
            associationDAO.deleteAssociation(assocSQLId);
            throw new RuntimeException("MongoDB update failed. Rolled back MySQL.", e);

        }

        return "Request approved and migrated.";
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

    private Association reqToModel(JoinRequest req){
        var model = new Association();
        model.setName(req.getAssociationName());
        model.setAddress(req.getLocation());
        model.setEmail(req.getContactInfo().getEmail());
//        model.setWebsite(req.get());
        model.setPhone(req.getContactInfo().getEmail());
//        model.setRemarks();
        return model;
    }
}
