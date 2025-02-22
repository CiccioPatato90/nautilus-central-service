package org.acme.service.requests;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.InventoryChangeDTO;
import org.acme.dto.InventoryItemDTO;
import org.acme.dto.InventoryRequestDTO;
import org.acme.model.enums.projects.ProjectStatus;
import org.acme.model.enums.requests.RequestStatus;
import org.acme.model.requests.common.RequestCommand;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.requests.inventory.InventoryChange;
import org.acme.model.requests.inventory.InventoryRequest;
import org.acme.model.requests.project.ProjectAllocatedResources;
import org.acme.model.requests.project.ProjectItem;
import org.acme.model.requests.project.ProjectRequest;
import org.acme.model.response.requests.inventory.SimulateAllocationStats;
import org.acme.model.response.requests.inventory.SimulateCommand;
import org.acme.model.response.requests.inventory.SimulateRequestResponse;
import org.acme.model.response.requests.inventory.SimulateResourceUsage;
import org.acme.model.virtual_warehouse.box.InventoryBox;
import org.acme.model.virtual_warehouse.box.InventoryBoxSize;
import org.acme.dao.AssociationDAO;
import org.acme.dao.requests.InventoryRequestDAO;
import org.acme.dao.settings.InventoryItemDAO;
import org.acme.dao.virtual_warehouse.InventoryBoxDAO;
import org.acme.dao.virtual_warehouse.InventoryBoxSizeDAO;
import org.acme.dao.virtual_warehouse.WarehouseDAO;
import org.acme.model.virtual_warehouse.item.InventoryItem;
import org.acme.service.ext.ResourceAllocationService;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jboss.logging.Logger;
import resourceallocation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;

@ApplicationScoped
public class InventoryRequestService{
    @Inject
    InventoryRequestDAO inventoryRequestDAO;
    @Inject
    AssociationDAO associationDAO;
    @Inject
    WarehouseDAO warehouseDAO;
    @Inject
    InventoryItemDAO inventoryItemDAO;
    @Inject
    InventoryBoxDAO inventoryBoxDAO;
    @Inject
    InventoryBoxSizeDAO inventoryBoxSizeDAO;
    @Inject
    CommonRequestService commonRequestService;
    @Inject
    AssociationRequestService associationRequestService;
    @Inject
    ResourceAllocationService resourceAllocationService;
    @Inject
    ProjectRequestService projectRequestService;


    public Map<String, List<InventoryRequestDTO>> getList(RequestFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return inventoryRequestDAO.findAll().list()
                    .stream()
                    .map(InventoryRequestDTO::fromEntity)
                    .collect(Collectors.groupingBy(
                            invReq -> associationRequestService.findByObjectId(invReq.getAssociationReqId()).getAssociationName(),
                            Collectors.toList()
                    ));
        }

        // Build the query dynamically using a Map
        Map<String, Object> queryMap = new HashMap<>();

        addFilter(queryMap, "associationId", filter.getAssociationId(), true);
//        addFilter(queryMap, "associationConfirmed", filter.getAssociationConfirmed(), true);
        addFilter(queryMap, "status", filter.getStatus().toString(), false);


        // Date range filter
        if (filter.getDateFrom() != null && filter.getDateTo() != null) {
            queryMap.put("date", Map.of("$gte", filter.getDateFrom(), "$lte", filter.getDateTo()));
        }

        Bson bsonQuery = new Document(queryMap);

        return inventoryRequestDAO.find(bsonQuery).list()
                .stream()
                .map(InventoryRequestDTO::fromEntity)
                .collect(Collectors.groupingBy(
                        invReq -> associationRequestService.findByObjectId(invReq.getAssociationReqId()).getAssociationName(),
                        Collectors.toList()
                ));
    }


    public String add(InventoryRequestDTO request) {
        var req = InventoryRequestDTO.toEntity(request);

        var associationRequestId = associationRequestService.getObjectId(String.valueOf(req.getAssociationSqlId()));

        if (associationRequestId != null) {
            req.setAssociationReqId(associationRequestId);
            req.createdAt = String.valueOf(Instant.now());
            req.updatedAt = req.createdAt;
            req.status = RequestStatus.PENDING;

            var id = this.persist(req);
            System.out.println("Saved Request with ID: "+ id);
            return id;
        }else{
            System.out.println("Association not verifided for inventory request: " + req.get_id().toString());
            return null;
        }

    }

    public String approveRequest(RequestCommand command) {
//        HOW DO WE MANAGE INVENTORY ITEM REQUEST APPROVAL, we send it to virtual
//          1. GET REQUEST FROM MONGO, SAVE IT IN CONTEXT
//          <STRING><INVENTORY_REQUEST> -> GetInventoryRequest()
        var req = inventoryRequestDAO.findByObjectId(commonRequestService.getObjectId(command.getRequestId()));
//        now we have to extract from teh request the association

//        3. FROM THE REQUEST CALCULATE THE NUMBER OF NEW ITEMS WE ARE REQUESTED TO INSERT
//        <INVENTORY_REQUEST><STRING> (SAVE REQ IN CONTEXT) -> CaclulateRequestedChange()
        int totalRequested = req.getInventoryChanges().stream()
                .mapToInt(ch -> ch.requestedQuantity)
                .sum();
//        4. EXTRACT ASSOCIATION FROM MONGO DOCUMENT USING SQL ID
//        <STRING><ASSOCIATION> -> GetAssociationModel()
        var assoc = associationDAO.findById(Long.valueOf(req.getAssociationReqId()));
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

    public String persist(InventoryRequest request){
        this.inventoryRequestDAO.persistOrUpdate(request);
        return request._id.toString();
    }

    public void delete(InventoryRequest request){
        this.inventoryRequestDAO.delete(request);
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

    public InventoryRequestDTO findByObjectId(String requestId) {
        var req = inventoryRequestDAO.findById(commonRequestService.getObjectId(requestId));
        return InventoryRequestDTO.fromEntity(req);
    }

    public List<InventoryRequestDTO> getPendingInventoryRequests(String associationRequestId) {
        return this.inventoryRequestDAO.findPendingRequestsByAssociationID(associationRequestId)
                .stream()
                .map(InventoryRequestDTO::fromEntity)
                .toList();
    }

    public Map<Integer,InventoryItemDTO> getItemsMetadata(List<InventoryChangeDTO> inventoryChanges) {
        var itemIds = inventoryChanges.stream()
                .map(InventoryChangeDTO::getItemId).map(Long::valueOf).collect(Collectors.toList());

        return inventoryItemDAO.findIdList(itemIds).stream()
                .map(InventoryItemDTO::fromEntity)
                .collect(Collectors.toMap(InventoryItemDTO::getId, inventoryItemDTO -> inventoryItemDTO));
    }

    public SimulateRequestResponse simulateRequest(SimulateCommand command) {
        List<ProjectRequest> projectRequests = List.of();
        switch(command.getSimulationType()){
            case PENDING_PROJECTS -> {
                projectRequests = projectRequestService.getListByRequestStatus(RequestStatus.PENDING);
            }
            case ALLOCATED_PROJECTS -> {
                projectRequests = projectRequestService.getListByProjectStatus(ProjectStatus.ALLOCATED);
            }
        }

        var availableItems = inventoryItemDAO.findAllAvailable();
        System.out.println("Found " + availableItems.size() + " available items");

        // First, convert changes to a Map for efficient lookup
        Map<Integer, Integer> changesByItemId = command.getChanges().stream()
                .collect(Collectors.toMap(
                        InventoryChangeDTO::getItemId,
                        InventoryChangeDTO::getRequestedQuantity,
                        Integer::sum  // In case there are multiple changes for the same ID
                ));

        // Create new list with augmented quantities
        var augmentedItems = availableItems.stream()
//                PEEK APPLIES A FUNCTION TO THE ELEMENT OF THE STREAMS, IN PLACE
                .peek(item -> {
                    int additionalQuantity = changesByItemId.getOrDefault(item.getId(), 0);
                    item.setAvailableQuantity(item.getAvailableQuantity() + additionalQuantity);
                })
                .toList();

        var req = createAllocationRequestMPMI(projectRequests, augmentedItems);

        var response = resourceAllocationService.allocateResources(req);

        System.out.println(response);

        var result = processAllocationResponse(projectRequests, response);
        return result;
    }

//    MPMI STANDS FOR MULTI PROJECT MULTI ITEM
    private AllocationRequest createAllocationRequestMPMI(List<ProjectRequest> req, List<InventoryItem> availableItems) {

        //<projectRequestId, map of required items>
        Map<ProjectRequest, Map<String, Integer>> var = req.stream()
                .collect(Collectors.toMap(
                        pReq-> pReq,
                        pReq -> pReq.getRequiredItemsSQLId()
                                .stream()
                                .collect(Collectors.toMap(
                                        item -> String.valueOf(item.getSqlId()),
                                        ProjectItem::getQuantityNeeded
                                    ))));

        List<Project> projects = var.entrySet()
                .stream()
                .map(entry -> Project.newBuilder()
                        .setId(entry.getKey().get_id().toString())
                        .setName(entry.getKey().getProjectName())
                        .putAllRequirements(entry.getValue())
                        .setPriority(1)
                        .build())
                .toList();

        System.out.println("Calculated Required Items.");

        List<Resource> resources = availableItems.stream().map(inventoryItem -> Resource.newBuilder()
                        .setId(String.valueOf(inventoryItem.getId()))
                        .setName(inventoryItem.getName())
                        .setCapacity(inventoryItem.getAvailableQuantity())
                        .setCost(10.0)
                        .build())
                .collect(Collectors.toList());

        System.out.println("Mapping to Resources List.");

        return AllocationRequest.newBuilder()
                .addAllProjects(projects)
                .addAllResources(resources)
                .setStrategy(AllocationStrategy.newBuilder()
                        .setStrategyName("default")
                        .build())
                .build();
    }


    public SimulateRequestResponse processAllocationResponse(List<ProjectRequest> requests, AllocationResponse response) {
        // Process each project request
        for (ProjectRequest req : requests) {
            // Set allocation ID for all requests
            req.setAllocationId(response.getAllocationId());

            // Find corresponding project allocation
            ProjectAllocatedResources allocatedResources = response.getProjectAllocationsMap().entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().getProjectId().equals(req._id.toString()))
                    .map(entry -> {
                        ProjectAllocatedResources par = new ProjectAllocatedResources();

                        // Set completion percentage
                        par.setCompletionPercentage(Double.parseDouble(entry.getKey()));

                        // Convert resource allocations to map
                        Map<String, Integer> allocationMap = entry.getValue().getResourceAllocationsList()
                                .stream()
                                .collect(Collectors.toMap(
                                        ResourceAllocation::getResourceId,
                                        ResourceAllocation::getAllocatedAmount
                                ));
                        par.setAllocationMap(allocationMap);

                        return par;
                    })
                    .findFirst()
                    .orElse(null);

            req.setAllocatedResources(allocatedResources);
        }

        return new SimulateRequestResponse(requests, this.convertProtoStats(response.getGlobalStats()));
    }


    private SimulateAllocationStats convertProtoStats(AllocationStats protoStats) {
        SimulateAllocationStats metadata = new SimulateAllocationStats();
        metadata.setTotalResourcesAvailable(protoStats.getTotalResourcesAvailable());
        metadata.setTotalResourcesUsed(protoStats.getTotalResourcesUsed());
        metadata.setAverageResourcesPerProject(protoStats.getAverageResourcesPerProject());
        metadata.setUnusedResources(protoStats.getUnusedResources());

        if (protoStats.hasMostAssignedResource()) {
            SimulateResourceUsage mostAssigned = new SimulateResourceUsage();
            mostAssigned.setResourceId(protoStats.getMostAssignedResource().getResourceId());
            mostAssigned.setUsageCount(protoStats.getMostAssignedResource().getUsageCount());
            metadata.setMostAssignedResource(mostAssigned);
        }

        if (protoStats.hasLeastAssignedResource()) {
            SimulateResourceUsage leastAssigned = new SimulateResourceUsage();
            leastAssigned.setResourceId(protoStats.getLeastAssignedResource().getResourceId());
            leastAssigned.setUsageCount(protoStats.getLeastAssignedResource().getUsageCount());
            metadata.setLeastAssignedResource(leastAssigned);
        }

        return metadata;
    }
}
