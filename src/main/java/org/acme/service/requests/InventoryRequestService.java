package org.acme.service.requests;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.enums.requests.RequestStatus;
import org.acme.model.requests.base.BaseRequest;
import org.acme.model.requests.common.RequestCommand;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.requests.inventory.InventoryChange;
import org.acme.model.requests.inventory.InventoryRequest;
import org.acme.model.response.requests.RequestListResponse;
import org.acme.model.virtual_warehouse.box.InventoryBox;
import org.acme.model.virtual_warehouse.box.InventoryBoxSize;
import org.acme.dao.AssociationDAO;
import org.acme.dao.requests.InventoryRequestDAO;
import org.acme.dao.requests.AssociationRequestDAO;
import org.acme.dao.settings.InventoryItemDAO;
import org.acme.dao.virtual_warehouse.InventoryBoxDAO;
import org.acme.dao.virtual_warehouse.InventoryBoxSizeDAO;
import org.acme.dao.virtual_warehouse.WarehouseDAO;
import org.acme.service.virtual_warehouse.VirtualWarehouseService;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.acme.model.enums.requests.RequestType.VIEW_ALL_LIST;

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
    AssociationRequestDAO associationRequestDAO;

    @Override
    public List<? extends BaseRequest> getList(RequestFilter filter) {
        if (filter == null || filter.isEmpty() || filter.getRequestType().equals(VIEW_ALL_LIST)) {
            var list = inventoryRequestDAO.findAll().list();
            return list;
        }

        // Build the query dynamically using a Map
        Map<String, Object> queryMap = new HashMap<>();

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

        return inventoryRequestDAO.find(bsonQuery).list();
    }

    @Override
    public RequestListResponse add(Class<? extends BaseRequest> request) {
        return null;
    }

    @Override
    public BaseRequest findByRequestId(String id) {
        var req = inventoryRequestDAO.findByRequestId(id);
        if (req.getAssociationName() == null && req.getAssociationReqId() != null) {
            var assocReq = associationRequestDAO.findByRequestId(req.getAssociationReqId());
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
//          1. GET REQUEST FROM MONGO, SAVE IT IN CONTEXT
//          <STRING><INVENTORY_REQUEST> -> GetInventoryRequest()
        var req = inventoryRequestDAO.findByRequestId(command.getRequestMongoId());
//        now we have to extract from teh request the association

//        3. FROM THE REQUEST CALCULATE THE NUMBER OF NEW ITEMS WE ARE REQUESTED TO INSERT
//        <INVENTORY_REQUEST><STRING> (SAVE REQ IN CONTEXT) -> CaclulateRequestedChange()
        int totalRequested = req.getInventoryChanges().stream()
                .mapToInt(ch -> ch.requestedQuantity)
                .sum();
//        4. EXTRACT ASSOCIATION FROM MONGO DOCUMENT USING SQL ID
//        <STRING><ASSOCIATION> -> GetAssociationModel()
        var assoc = associationDAO.findById(Long.valueOf(req.associationSQLId));
//        5. FROM ASSOCIATION GET INVENTORY BOX ASSIGNED -> GetAssignedBox()
//        <ASSOCIATION><INVENTORY_BOX ? NULL>
        InventoryBox assigned = assoc.getInventoryBoxes().stream()
                .filter(box -> !box.getFull() && (box.getFkSize().getMaxSize() - box.getCurrentSize()) >= totalRequested)
                .findFirst()
                .orElse(null);
//        6. <INVENTORY_BOX><INVENTORY_BOX> -> CreatNewBoxOrPassAlong
//        since if null we create a new inventory box and if not null we simply return what we already have
//        we save it in context anyway, but we need also a flag saying if persistence for it is needed
        Integer assignedId = 0;
        if(assigned == null){
        // if we don't find any available inventoryBoxes, we create a new one
//            6.1 create a new box and save it in context -> assigned
            assigned = new InventoryBox();
            assigned.setFkAssociation(assoc);

            var def_warehouse = warehouseDAO.findById(1L);
            var box_size = getMinimumNeededSize(totalRequested);
            assigned.setFkWarehouse(def_warehouse);
            assigned.setFkSize(box_size);
        }

        //  6.2 <INVENTORY_BOX><STRING> ---> sqlPersistInventoryBoxHandler
        //  IF THE CONTEXT PERSISTENCE FLAG IS TRUE, THEN WE PERSIST AND WE RETURN ID, OTHERWISE WE ONLY RETURN BOX ID
//        ATTENTION TO WHEN DO YOU SET THE ASSIGNED_ID SINCE IT MIGHT ROLLBACK BOXES THAT ARE FULL WITH OTHER ITEMS

//        ????? CAN ASSIGN ALL TOGETHER ??????


//        inventoryBoxDAO.persist(assigned);
//        assignedId = assigned.getId();


        // 7. <STRING><INVENTORY_BOX> --> FillBox
//        GET REQUEST FROM CONTEXT, DO COMPUTATIONS AND UPDATE BOX
        for(InventoryChange itemRequest : req.getInventoryChanges()){
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
//           8. <INVENTORY_BOX><STRING> -> ROLLBACK BY DELETING THE BOX WITH ASSIGNED ID
            inventoryBoxDAO.persist(assigned);
        }catch (Exception e){
            System.out.println("Exception when allocating Item Box: " + e);
        }

//            9. <STRING><INVENTORY_REQUEST> --> UpdateMongoRequest()
//        UPDATE SOME REQUEST FIELDS
        String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        req.setUpdatedAt(now);
        req.setStatus(RequestStatus.APPROVED);

        try {
//            10. <INVENTORY_REQUEST><STRING> --> MongoPersistRequestHandler
//            UPDATE REQUEST AND RETURN ITS ID
            inventoryRequestDAO.persistOrUpdate(req);
        } catch (Exception e) {
            // At this point, MySQL is updated but MongoDB failed.
            // You can implement a compensating action for MySQL or flag the inconsistency.
            inventoryBoxDAO.deleteById(Long.valueOf(assignedId));
            throw new RuntimeException("MongoDB update failed. Rolled back MySQL.", e);
        }
        return "Everything OK";
    }


    private InventoryBoxSize getMinimumNeededSize(int requiredSize) {
        return inventoryBoxSizeDAO.listAll().stream()
                .filter(box -> box.getMaxSize() >= requiredSize)
                .min(Comparator.comparing(InventoryBoxSize::getMaxSize))
                .orElse(null);
    }


    public void persist(InventoryRequest request){
        this.inventoryRequestDAO.persistOrUpdate(request);
//        this.inventoryRequestDAO.persist(request);
    }
    public void delete(InventoryRequest request){
        this.inventoryRequestDAO.delete(request);
    }

}
