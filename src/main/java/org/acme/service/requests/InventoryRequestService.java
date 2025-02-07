package org.acme.service.requests;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.requests.RequestFilter;
import org.acme.model.InventoryItem;
import org.acme.model.enums.RequestStatus;
import org.acme.model.requests.BaseRequest;
import org.acme.model.requests.InventoryRequest;
import org.acme.model.requests.RequestCommand;
import org.acme.model.response.RequestListResponse;
import org.acme.model.virtual_warehouse.InventoryBox;
import org.acme.model.virtual_warehouse.InventoryBoxSize;
import org.acme.model.virtual_warehouse.Warehouse;
import org.acme.repository.AssociationDAO;
import org.acme.repository.requests.InventoryRequestDAO;
import org.acme.repository.requests.OrgRequestDAO;
import org.acme.repository.settings.InventoryItemDAO;
import org.acme.repository.virtual_warehouse.InventoryBoxDAO;
import org.acme.repository.virtual_warehouse.InventoryBoxSizeDAO;
import org.acme.repository.virtual_warehouse.WarehouseDAO;
import org.acme.service.virtual_warehouse.VirtualWarehouseService;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static org.acme.model.enums.RequestType.VIEW_ALL_LIST;

@ApplicationScoped
public class InventoryRequestService implements RequestInterface{
    @Inject
    InventoryRequestDAO inventoryRequestDAO;

    @Inject
    AssociationDAO associationDAO;

    @Inject
    WarehouseDAO warehouseDAO;


//    @RestClient
//    VirtualWarehouseService virtualWarehouseService;
    @Inject
    VirtualWarehouseService virtualWarehouseService;
    @Inject
    InventoryItemDAO inventoryItemDAO;
    @Inject
    InventoryBoxDAO inventoryBoxDAO;
    @Inject
    InventoryBoxSizeDAO inventoryBoxSizeDAO;
    @Inject
    OrgRequestDAO orgRequestDAO;

    @Override
    public List<? extends BaseRequest> getList(RequestFilter filter) {
        if (filter == null || filter.isEmpty() || filter.getRequestType().equals(VIEW_ALL_LIST)) {
            var list = inventoryRequestDAO.findAll().list();
            return list;
//            return mapToResponse(list, filter);
        }

        // Build the query dynamically using a Map
        Map<String, Object> queryMap = new HashMap<>();

//        var assoc = associationDAO.findByName(filter.getAssociationName());

        addFilter(queryMap, "associationId", filter.getAssociationId(), true);
//        addFilter(queryMap, "associationConfirmed", filter.getAssociationConfirmed(), true);
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

//        var filteredList = inventoryRequestDAO.find(bsonQuery).list();
        return inventoryRequestDAO.find(bsonQuery).list();
    }

    @Override
    public RequestListResponse add(Class<? extends BaseRequest> filter) {
        return null;
    }

    @Override
    public BaseRequest findByRequestId(String id) {
        var req = inventoryRequestDAO.findByRequestId(id);
        if (req.getAssociationName() == null && req.getAssociationReqId() != null) {
            var assocReq = orgRequestDAO.findByRequestId(req.getAssociationReqId());
            if (assocReq.getAssociationSQLId() != null) {
                req.setAssociationName(associationDAO.find("id", assocReq.getAssociationSQLId()).firstResult().getName());
                //FLAG TO CHECK IF THE ASSOCIATION ISSUING THE REQUEST HAS ALREADY BEEN CONFIRMED (SAVE IN MYSQL DB)
                req.setAssociationConfirmed(true);
                req.setAssociationSQLId(assocReq.getAssociationSQLId());
                inventoryRequestDAO.update(req);
            }
        }
        return req;
    }

    @Override
    @Transactional
    public String approveRequest(RequestCommand command) {
//        HOW DO WE MANAGE INVENTORY ITEM REQUEST APPROVAL, we send it to virtual
        var req = inventoryRequestDAO.findByRequestId(command.getRequestMongoId());
//        now we have to extract from teh request the association
        var assoc = associationDAO.findById(Long.valueOf(req.associationSQLId));
//        now that we have the association, we can check if there are boxes full
//        we insert the item in the first box that is not full
        Integer assignedId = 0;

        int totalRequested = req.getInventoryChanges().stream()
                .mapToInt(ch -> ch.requestedQuantity)
                .sum();

        InventoryBox assigned = assoc.getInventoryBoxes().stream()
                .filter(box -> !box.getFull() && (box.getFkSize().getMaxSize() - box.getCurrentSize()) >= totalRequested)
                .findFirst()
                .orElse(null);

        if(assigned == null){
        // if we don't find any available inventoryBoxes, we create a new one
            assigned = new InventoryBox();
            assigned.setFkAssociation(assoc);
            var def_warehouse = warehouseDAO.findById(1L);
            var box_size = getMinimumNeededSize(totalRequested);
            assigned.setFkWarehouse(def_warehouse);
            assigned.setFkSize(box_size);
            inventoryBoxDAO.persist(assigned);
            assignedId = assigned.getId();
        }

        for(InventoryRequest.InventoryChange itemRequest : req.getInventoryChanges()){
//            QUICK SOLUTION: LOOKAHEAD COMPUTATION
            var newItem = inventoryItemDAO.findById(Long.valueOf(itemRequest.itemId));
            assigned.getInventoryItems().add(newItem);
            var currentSize = assigned.getCurrentSize();
            assigned.setCurrentSize(currentSize + itemRequest.requestedQuantity);
            if (Objects.equals(assigned.getCurrentSize(), assigned.getFkSize().getMaxSize())){
//                then the box is full and has to be flagged as full
                assigned.setFull(true);
            }
        }

        try{
            inventoryBoxDAO.persist(assigned);
        }catch (Exception e){
            System.out.println("Exception when allocating Item Box: " + e);
        }


        String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        req.setUpdatedAt(now);
        req.setStatus(RequestStatus.APPROVED);

        try {
            inventoryRequestDAO.persistOrUpdate(req);
        } catch (Exception e) {
            // At this point, MySQL is updated but MongoDB failed.
            // You can implement a compensating action for MySQL or flag the inconsistency.
            inventoryBoxDAO.deleteById(Long.valueOf(assignedId));
            throw new RuntimeException("MongoDB update failed. Rolled back MySQL.", e);
        }
        return "Everything OK";
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

    private InventoryBoxSize getMinimumNeededSize(int requiredSize) {
        return inventoryBoxSizeDAO.listAll().stream()
                .filter(box -> box.getMaxSize() >= requiredSize)
                .min(Comparator.comparing(InventoryBoxSize::getMaxSize))
                .orElse(null);
    }

}
