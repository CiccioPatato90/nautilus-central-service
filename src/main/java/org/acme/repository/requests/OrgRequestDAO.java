package org.acme.repository.requests;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.dto.requests.RequestFilter;
import org.acme.model.requests.JoinRequest;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.Map;
import java.util.HashMap;

import java.util.*;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;

@ApplicationScoped
public class OrgRequestDAO implements PanacheMongoRepository<JoinRequest> {

//    public String addRequest(JoinRequest req) {
//        JoinRequestMapper mapper = Mappers.getMapper(JoinRequestMapper.class);
//        var entity = mapper.dtoToEntity(req);
//        persist(entity);
//        return entity.getRequestId().toString();
//        ObjectId id = ObjectId.get();
//        req.setRequestId(id);
//        persist(req);
//        return "YES";
//    }
//
//
//    public List<JoinRequest> getReq(RequestFilter filter) {
//        if (filter == null || filter.isEmpty()) {
//            return findAll().list();
//        }
//
//        // Build the query dynamically using a Map
//        Map<String, Object> queryMap = new HashMap<>();
//
//        addFilter(queryMap, "associationName", filter.getAssociationName(), true); // Regex filter
//        addFilter(queryMap, "category", filter.getRequestType(), false);
//        addFilter(queryMap, "urgency", filter.getUrgency(), false);
//        addFilter(queryMap, "status", filter.getStatus(), false);
//        addFilter(queryMap, "assignedAdmin", filter.getAssignedAdmin(), false);
//        addFilter(queryMap, "location", filter.getLocation(), false);
//
//        // Tags filter (Array field)
//        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
//            queryMap.put("tags", Map.of("$in", filter.getTags()));
//        }
//
//        // Date range filter
//        if (filter.getDateFrom() != null && filter.getDateTo() != null) {
//            queryMap.put("date", Map.of("$gte", filter.getDateFrom(), "$lte", filter.getDateTo()));
//        }
//
//        Bson bsonQuery = new Document(queryMap);
//
//        var filteredList = find(bsonQuery).list();
//        return filteredList;
//    }
//
//
//    private void addFilter(Map<String, Object> queryMap, String field, String value, boolean useRegex) {
//        if (!isNullOrEmpty(value)) {
//            if (useRegex) {
//                queryMap.put(field, Map.of("$regex", value, "$options", "i")); // Case-insensitive regex
//            } else {
//                queryMap.put(field, value);
//            }
//        }
//    }
}
