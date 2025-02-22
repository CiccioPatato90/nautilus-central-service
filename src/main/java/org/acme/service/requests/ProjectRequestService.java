package org.acme.service.requests;

import io.grpc.ManagedChannelBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dao.settings.InventoryItemDAO;
import org.acme.dto.ProjectRequestDTO;
import org.acme.model.enums.projects.ProjectStatus;
import org.acme.model.enums.requests.RequestStatus;
import org.acme.model.requests.common.RequestCommand;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.requests.project.ProjectAllocatedResources;
import org.acme.model.requests.project.ProjectItem;
import org.acme.model.requests.project.ProjectRequest;
import org.acme.dao.requests.ProjectRequestDAO;
import org.acme.model.virtual_warehouse.item.InventoryItem;
import org.acme.pattern.context.BaseTransactionContext;
import org.acme.pattern.handlers.DatabaseHandler;
import org.acme.pattern.pipeline.Pipeline;
import org.acme.service.pipeline.ProjectAllocationCall;
import org.acme.service.pipeline.ProjectRequestApprovalPipeline;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import resourceallocation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;

@ApplicationScoped
public class ProjectRequestService{
    @Inject
    ProjectRequestDAO projectRequestDAO;
    @Inject
    InventoryItemDAO inventoryItemDAO;
    @Inject
    CommonRequestService commonRequestService;
    @Inject
    AssociationRequestService associationRequestService;

    public Map<String, List<ProjectRequestDTO>> getList(RequestFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return projectRequestDAO.findAll().list()
                    .stream()
                    .map(ProjectRequestDTO::fromEntity)
                    .collect(Collectors.groupingBy(
                            projReq -> associationRequestService.findByObjectId(projReq.getAssociationReqId()).getAssociationName(),
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

        return projectRequestDAO.find(bsonQuery).list()
                .stream()
                .map(ProjectRequestDTO::fromEntity)
                .collect(Collectors.groupingBy(
                        projReq -> associationRequestService.findByObjectId(projReq.getAssociationReqId()).getAssociationName(),
                        Collectors.toList()
                ));
    }

    public String add(ProjectRequestDTO request) {
//          WE NEED TO ADD A PROJECT REQUEST, GIVEN THAT WE HAVE ITEMS + QUANTITIES NEEDED
//          AND REQUEST ISSUER

        var req = ProjectRequestDTO.toEntity(request);

        var associationRequestId = associationRequestService.getObjectId(String.valueOf(req.getAssociationSqlId()));

        if (associationRequestId != null) {
            req.setAssociationReqId(associationRequestId);
            req.createdAt = String.valueOf(Instant.now());
            req.updatedAt = req.createdAt;
            req.status = RequestStatus.PENDING;
//            here we need to call the grpc service and calculate the feasibility of the project and allocated the resources

            var associationConfirmed = associationRequestService.checkAssociationConfirmed(req.getAssociationReqId());
            if (!associationConfirmed) {
                return null;
            }

            var availableItems = inventoryItemDAO.findAllAvailable();
            System.out.println("Found " + availableItems.size() + " available items");

            req.set_id(new ObjectId());

            var allocationRequest = createAllocationRequest(req, availableItems);

            var channel = ManagedChannelBuilder.forAddress("localhost", 8082).usePlaintext().build();

            var stub = MutinyResourceAllocationServiceGrpc.newMutinyStub(channel);

            //find a way to deinit channel!!!!!
            AllocationResponse response = stub.allocateResources(allocationRequest)
                    .onItem().invoke(resp ->
                            System.out.println("Received status: " + resp.getStatus().name() + " and ID: " + resp.getAllocationId()))
                    .await().indefinitely();

            channel.shutdown();

            var processedRequest = processAllocationResponse(req, response);

            var id = this.persist(processedRequest);
            System.out.println("Saved Request with ID: "+ id);
            return id;
        }else{
            System.out.println("Association not verifided for inventory request: " + req.get_id().toString());
            return null;
        }
    }

    private ProjectRequest processAllocationResponse(ProjectRequest req, AllocationResponse response) {
        req.setAllocationId(response.getAllocationId());

        ProjectAllocatedResources allocatedResources = response.getProjectAllocationsMap().entrySet()
                .stream()
                .findFirst()
                .map(entry -> {
                    ProjectAllocatedResources par = new ProjectAllocatedResources();
                    // Parse the key as the completion percentage.
                    par.setCompletionPercentage(Double.parseDouble(entry.getKey()));
                    // Convert the list of ResourceAllocation messages to a Map<resourceId, allocatedAmount>
                    Map<String, Integer> allocationMap = entry.getValue().getResourceAllocationsList()
                            .stream()
                            .collect(Collectors.toMap(
                                    ResourceAllocation::getResourceId,
                                    ResourceAllocation::getAllocatedAmount
                            ));
                    par.setAllocationMap(allocationMap);
                    return par;
                })
                .orElse(null);

        req.setAllocatedResources(allocatedResources);

        return req;
    }

    private AllocationRequest createAllocationRequest(ProjectRequest req, List<InventoryItem> availableItems) {
        Map<String, Integer> requiredItems = req.getRequiredItemsSQLId().stream()
                .collect(Collectors.toMap(
                        item -> String.valueOf(item.getSqlId()),
                        ProjectItem::getQuantityNeeded
                ));

        System.out.println("Calcolated Required Items.");

        List<Resource> resources = availableItems.stream().map(inventoryItem -> Resource.newBuilder()
                        .setId(String.valueOf(inventoryItem.getId()))
                        .setName(inventoryItem.getName())
                        .setCapacity(inventoryItem.getAvailableQuantity())
                        .setCost(10.0)
                        .build())
                .collect(Collectors.toList());

        System.out.println("Mapping to Resources List.");

        return AllocationRequest.newBuilder().
                addProjects(Project.newBuilder()
                        .setId(req.get_id().toString())
                        .setName(req.getProjectName())
                        .putAllRequirements(requiredItems)
                        .setPriority(1)
                        .build())
                .addAllResources(resources)
                .setStrategy(AllocationStrategy.newBuilder()
                        .setStrategyName("default")
                        .build())
                .build();
    }


    public String approveRequest(RequestCommand command) {
        final BaseTransactionContext context = new BaseTransactionContext();

        DatabaseHandler<String, ProjectRequest, ProjectRequestService> mongoGetProjectRequestHandler =
                new DatabaseHandler<>(this,
                        (reqId, repository) -> {
                            var base = findByObjectId(reqId);
                            context.logStep("Fetched Request: "+ base.get_id().toString());
                            context.put("projectRequest", base);
                            return ProjectRequestDTO.toEntity(base);
                        }, () -> {});

        DatabaseHandler<ProjectRequest, List<InventoryItem>, InventoryItemDAO> fetchInventoryItemsList =
                new DatabaseHandler<>(inventoryItemDAO,
                        (request, repository) -> {
                            var base = inventoryItemDAO.findAllAvailable();
                            context.logStep("Found " + base.size() + " available items");
                            return base;
                        }, () -> {});

        DatabaseHandler<ProjectRequest, String, ProjectRequestService> mongoPersistRequestHandler =
                new DatabaseHandler<>(this,
                        (req, repository) -> {
                            repository.persist(req);
                            context.put("rollback_mongoRequest", req);
                            return req.get_id().toString();
                        },
                        () -> {
                            ProjectRequest req = context.get("projectRequest");
                            if (req != null) {
//                                projectRequestService.persist(req);
                                context.logStep("Rolled back Mongo request.");
                            }
                        });


        //1. fetch request from mongo
        var pipe = new Pipeline<>(mongoGetProjectRequestHandler, context)
        //2. check association is verified
        .addHandler(new ProjectRequestApprovalPipeline.CheckAssociationVerified())
        //3. fetch List<InventoryItem> and save it in context
        .addHandler(fetchInventoryItemsList)
        //4. create allocation request
        .addHandler(new ProjectRequestApprovalPipeline.CreateResourceAllocationRequest())
        //6. call allocationService, wait for allocationService response, in the meantime keep polling for execution status and send it to frontend (????)
        .addHandler(new ProjectAllocationCall())
        //7. parse the response and update the projects entity
        .addHandler(new ProjectRequestApprovalPipeline.ProcessAllocationResponseSingleProject())
        //8. update the projectResource allocation map
        .addHandler(mongoPersistRequestHandler);

        return pipe.execute(command.getRequestId());
    }

    public String persist(ProjectRequest req) {
        projectRequestDAO.persistOrUpdate(req);
        return req.get_id().toString();
    }

    public void delete(ProjectRequest req) {
        projectRequestDAO.delete(req);
    }

    public List<ProjectRequest> getListByProjectStatus(ProjectStatus projectStatus) {
        return null;
    }
    public List<ProjectRequest> getListByRequestStatus(RequestStatus requestStatus) {
        return this.projectRequestDAO.findByRequestStatus(requestStatus);
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

    public ProjectRequestDTO findByObjectId(String requestId) {
        var req = projectRequestDAO.findById(commonRequestService.getObjectId(requestId));
        return ProjectRequestDTO.fromEntity(req);
    }

    public List<ProjectRequestDTO> getPendingProjectRequests(String associationRequestId) {
        return this.projectRequestDAO.findPendingRequestsByAssociationID(associationRequestId)
                .stream()
                .map(ProjectRequestDTO::fromEntity)
                .toList();
    }
}
