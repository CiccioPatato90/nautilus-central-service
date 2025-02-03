package org.acme.service.requests;

import com.fasterxml.jackson.databind.ser.Serializers;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.requests.RequestFilter;
import org.acme.model.requests.BaseRequest;
import org.acme.model.requests.JoinRequest;
import org.acme.model.response.RequestListResponse;
import org.acme.repository.requests.OrgRequestDAO;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static org.acme.model.enums.RequestType.VIEW_ALL_LIST;

@ApplicationScoped
public class OrgRequestService implements RequestInterface {
    @Inject
    OrgRequestDAO orgRequestDAO;

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
        addFilter(queryMap, "status", filter.getStatus(), false);

        // Tags filter (Array field)
        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            queryMap.put("tags", Map.of("$in", filter.getTags()));
        }

        // Date range filter
        if (filter.getDateFrom() != null && filter.getDateTo() != null) {
            queryMap.put("date", Map.of("$gte", filter.getDateFrom(), "$lte", filter.getDateTo()));
        }

        Bson bsonQuery = new Document(queryMap);

        var filteredList = orgRequestDAO.find(bsonQuery).list();
        return orgRequestDAO.find(bsonQuery).list();
//        return mapToResponse(filteredList, filter);
    }

    @Override
    public RequestListResponse add(Class<? extends BaseRequest> filter) {
//        return List.of();
        return null;
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


}
