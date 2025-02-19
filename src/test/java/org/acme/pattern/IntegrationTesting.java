package org.acme.pattern;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.acme.dto.AssociationDTO;
import org.acme.model.Association;
import org.acme.model.enums.requests.RequestStatus;
import org.acme.model.requests.association.AssociationRequest;
import org.acme.model.requests.inventory.InventoryChange;
import org.acme.model.requests.inventory.InventoryRequest;
import org.acme.model.requests.project.ProjectAllocatedResources;
import org.acme.model.requests.project.ProjectItem;
import org.acme.model.requests.project.ProjectRequest;
import org.acme.model.virtual_warehouse.box.InventoryBox;
import org.acme.model.virtual_warehouse.box.InventoryBoxSize;
import org.acme.model.virtual_warehouse.item.InventoryItem;
import org.acme.pattern.context.BaseTransactionContext;
import org.acme.pattern.exceptions.AssociationNotConfirmedException;
import org.acme.pattern.handlers.DatabaseHandler;
import org.acme.pattern.pipeline.Pipeline;
import org.acme.dao.settings.InventoryItemDAO;
import org.acme.dao.virtual_warehouse.InventoryBoxDAO;
import org.acme.dao.virtual_warehouse.InventoryBoxSizeDAO;
import org.acme.dao.virtual_warehouse.WarehouseDAO;
import org.acme.service.auth.UtenteService;
import org.acme.service.requests.AssociationRequestService;
import org.acme.service.requests.InventoryRequestService;
import org.acme.service.requests.ProjectRequestService;
import org.acme.service.settings.AssociationSettingsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import resourceallocation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class IntegrationTesting {
    @Inject
    AssociationRequestService associationRequestDAO;
    @Inject
    AssociationSettingsService associationSettingsService;
    @Inject
    InventoryRequestService inventoryRequestService;
    @Inject
    ProjectRequestService projectRequestService;
    @Inject
    InventoryBoxDAO inventoryBoxDAO;
    @Inject
    WarehouseDAO warehouseDAO;
    @Inject
    InventoryBoxSizeDAO inventoryBoxSizeDAO;
    @Inject
    InventoryItemDAO inventoryItemDAO;
    @Inject
    UtenteService utenteService;

    private static class UpdateAssociationRequestApprovedHandler implements Handler<AssociationRequest, AssociationRequest> {
        private BaseTransactionContext context;

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }

        @Override
        public AssociationRequest process(AssociationRequest req) {
            req.setAssociationConfirmed(true);
            String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
            req.setUpdatedAt(now);
            req.setStatus(RequestStatus.APPROVED);
            context.put("mongoRequest", req);
            return req;
        }
    }

    private static class MapToModel implements Handler<AssociationRequest, Association> {
        @Override
        public Association process(AssociationRequest req) {
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

    private static class UpdateMongoId implements Handler<Long, AssociationRequest> {
        private BaseTransactionContext context;

        @Override
        public AssociationRequest process(Long input) {
            AssociationRequest previousReq = context.get("mongoRequest");
            if (previousReq != null) {
                previousReq.associationSQLId = input.toString();
            }
            return previousReq;
        }

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

//    @Test
//    void testAssociationRequestPipeline() {
//        final BaseTransactionContext context = new BaseTransactionContext();
//
//        DatabaseHandler<String, AssociationRequest, AssociationRequestService> mongoGetRequestHandler =
//                new DatabaseHandler<>(associationRequestDAO,
//                        (reqId, repository) -> {
//                    var base = repository.findByRequestId(reqId);
//                    return (AssociationRequest) base;
//                }, () -> {
////                    ROLLBACK CALLBACK
//                });
//
//        DatabaseHandler<AssociationRequest, Integer, AssociationRequestService> mongoPersistRequestHandler =
//                new DatabaseHandler<>(associationRequestDAO,
//                (req, repository) -> {
//                    int res = repository.persist(req);
//                    context.put("rollback_mongoRequest", req);
//                    return res;
//                },
//                () -> {
//                    AssociationRequest req = context.get("rollback_mongoRequest");
//                    if (req != null) {
//                        associationRequestDAO.delete(req);
//                        context.logStep("Rolled back Mongo request.");
//                    }
//                });
//
//        DatabaseHandler<Association, Long, AssociationSettingsService> sqlPersistAssociationHandler =
//                new DatabaseHandler<>(associationSettingsService,
//                (model, repository) ->{
//                    Long res = repository.addAssociation(AssociationDTO.fromEntity(model));
//                    context.put("rollback_SQLRequest", res);
//                    return res;
//                },
//                () -> {
//                    Long model_id = context.get("rollback_SQLRequest");
//                    if (model_id != null) {
//                        associationSettingsService.deleteAssociation(model_id);
//                        context.logStep("Rolled back MySQL request.");
//                    }
//                } );
//
//        var pipe = new Pipeline<>(mongoGetRequestHandler, context)
//                .addHandler(new UpdateAssociationRequestApprovedHandler())
//                .addHandler(new MapToModel())
//                .addHandler(sqlPersistAssociationHandler)
//                .addHandler(new UpdateMongoId())
//                .addHandler(mongoPersistRequestHandler);
//
//        Integer result = pipe.execute("JR12345");
//        assertEquals(7, result);
//    }

    private static class CalculateRequestedChange implements Handler<InventoryRequest, String> {
        private BaseTransactionContext context;

        @Override
        public String process(InventoryRequest input) {
            int totalRequested = input.getInventoryChanges().stream()
                    .mapToInt(ch -> ch.requestedQuantity)
                    .sum();

            context.put("requestedQuantity", totalRequested);
            context.put("inventoryRequest", input);
            this.context.logStep("Calculated Request Inventory Change: "+ totalRequested);
            return input.getAssociationSQLId();
        }

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

    private class GetAssociationModel implements Handler<String, Association> {
//        @Inject
//        AssociationSettingsService associationSettingsService;

        private BaseTransactionContext context;


        @Override
        public Association process(String input) {
            var base = AssociationDTO.toEntity(associationSettingsService.findById(Long.valueOf(input)));
            context.put("associationEntity", base);
            this.context.logStep("Fetched Association Model " + base.getId());
            return base;
        }

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

    private static class GetAssignedBox implements Handler<Association, InventoryBox> {

        private BaseTransactionContext context;

        @Override
        public InventoryBox process(Association input) {
            var totalRequested = (int)context.get("requestedQuantity");
            InventoryBox assigned = input.getInventoryBoxes().stream()
                    .filter(box -> !box.getFull() && (box.getFkSize().getMaxSize() - box.getCurrentSize()) >= totalRequested)
                    .findFirst()
                    .orElse(new InventoryBox());
            this.context.logStep("Assigned Box " + assigned.getId());
            return assigned;
        }

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

    private class CreateNewBoxOrPassAlong implements Handler<InventoryBox, InventoryBox> {


        private BaseTransactionContext context;

        @Override
        public InventoryBox process(InventoryBox input) {
            if(input.getId() == null){
                // if we don't find any available inventoryBoxes, we create a new one
//            6.1 create a new box and save it in context -> assigned
                var assigned = new InventoryBox();
                var assoc = (Association) context.get("associationEntity");
                assigned.setFkAssociation(assoc);

                var def_warehouse = warehouseDAO.findById(1L);

                var totalRequested = (int)context.get("requestedQuantity");
                var box_size = getMinimumNeededSize(totalRequested);
                assigned.setFkWarehouse(def_warehouse);
                assigned.setFkSize(box_size);
                context.put("newBoxCreated", true);
                this.context.logStep("Populated New InventoryBox with size " + box_size);
                return assigned;
            }
            else{
                this.context.logStep("Propagated InventoryBox " + input.getId());
                return input;
            }
        }

        private InventoryBoxSize getMinimumNeededSize(int requiredSize) {
            return inventoryBoxSizeDAO.listAll().stream()
                    .filter(box -> box.getMaxSize() >= requiredSize)
                    .min(Comparator.comparing(InventoryBoxSize::getMaxSize))
                    .orElse(null);
        }

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

    private class FillBox implements Handler<InventoryBox, InventoryBox> {

        private BaseTransactionContext context;

        @Override
        public InventoryBox process(InventoryBox input) {
            var req = (InventoryRequest) context.get("inventoryRequest");
            for(InventoryChange itemRequest : req.getInventoryChanges()){
//            QUICK SOLUTION: LOOKAHEAD COMPUTATION
                var newItem = inventoryItemDAO.findById(Long.valueOf(itemRequest.itemId));
                input.getInventoryItems().add(newItem);
                var currentSize = input.getCurrentSize();
                input.setCurrentSize(currentSize + itemRequest.requestedQuantity);
                if (Objects.equals(input.getCurrentSize(), input.getFkSize().getMaxSize())){
//                then the box is full and has to be flagged as full
                    input.setFull(true);
                }
            }
            this.context.logStep("Filled InventoryBox to size" + input.getCurrentSize());
            return input;
        }

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

    private class UpdateMongoRequest implements Handler<InventoryBox, InventoryRequest> {

        private BaseTransactionContext context;

        @Override
        public InventoryRequest process(InventoryBox input) {
            var req = (InventoryRequest) context.get("inventoryRequest");
            var associationModel = (Association) context.get("associationEntity");
            String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
            req.setUpdatedAt(now);
            req.setStatus(RequestStatus.APPROVED);
            req.setAssociationName(associationModel.getName());
            req.setApprovedBy(utenteService.getCurrentUtenteName());
            context.put("inventoryRequest", req);
            this.context.logStep("Updated Mongo Request to " + req.getStatus().toString());
            return req;
        }

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

//    @Test
//    void testInventoryRequestPipeline() {
//        final BaseTransactionContext context = new BaseTransactionContext();
//
//        DatabaseHandler<String, InventoryRequest, InventoryRequestService> mongoGetInventoryRequestHandler =
//                new DatabaseHandler<>(inventoryRequestService,
//                        (reqId, repository) -> {
//                            var base = inventoryRequestService.findByRequestId(reqId);
//                            context.logStep("Fetched Request "+ base.requestId);
//                            return (InventoryRequest) base;
//                        }, () -> {
////                    ROLLBACK CALLBACK
//                });
//
//        DatabaseHandler<InventoryBox, InventoryBox, InventoryBoxDAO> sqlPersistInventoryBoxHandler =
//                new DatabaseHandler<>(inventoryBoxDAO,
//                        (model, repository) ->{
//                            var new_box = (boolean) context.get("newBoxCreated");
//                            if(new_box){
//                                var modelId = repository.persistInventoryBox(model);
//                                context.logStep("Added InventoryBox to MySQL with ID: " + modelId);
//                                context.put("rollback_SQLRequest", modelId);
//                                return model;
//                            }
////                            TODO: FIX THIS IN ORDER TO HAVE NICER RETURN ALSO FOR NULL
//                            return model;
//                        },
//                        () -> {
//                            Integer model_id = context.get("rollback_SQLRequest");
//                            var new_box = (boolean) context.get("newBoxCreated");
//                            if (model_id != null && new_box) {
//                                inventoryBoxDAO.deleteInventoryBox(Long.valueOf(model_id));
//                                context.logStep("Rolled back MySQL request.");
//                            }
//                        } );
//
//
//        DatabaseHandler<InventoryRequest, String, InventoryRequestService> mongoPersistRequestHandler =
//                new DatabaseHandler<>(inventoryRequestService,
//                        (req, repository) -> {
//                            repository.persist(req);
//                            context.put("rollback_mongoRequest", req);
//                            return req.requestId;
//                        },
//                        () -> {
//                            InventoryRequest req = context.get("rollback_mongoRequest");
//                            if (req != null) {
//                                inventoryRequestService.delete(req);
//                                context.logStep("Rolled back Mongo request.");
//                            }
//                        });
//
//
//        var pipe = new Pipeline<>(mongoGetInventoryRequestHandler, context)
//                .addHandler(new CalculateRequestedChange())
//                .addHandler(new GetAssociationModel())
//                .addHandler(new GetAssignedBox())
//                .addHandler(new CreateNewBoxOrPassAlong())
//                .addHandler(new FillBox())
//                .addHandler(sqlPersistInventoryBoxHandler)
//                .addHandler(new UpdateMongoRequest())
//                .addHandler(mongoPersistRequestHandler);
//
//        var result = pipe.execute("IR12555");
//        assertEquals("IR12555", result);
//    }

    private static class CheckAssociationVerified implements Handler<ProjectRequest, ProjectRequest> {
        private BaseTransactionContext context;

        @Override
        public ProjectRequest process(ProjectRequest input) {
            if (input.getAssociationSQLId() == null || !input.associationConfirmed) {
                context.setError(new AssociationNotConfirmedException(input.getRequestId()));
            }
            return input;
        }

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

    private static class CreateResourceAllocationRequest implements Handler<List<InventoryItem>, AllocationRequest> {
        private BaseTransactionContext context;

        @Override
        public AllocationRequest process(List<InventoryItem> input) {
//            project requirements arrive from mongoRequest saved in context
//            here I get as input the list of items available
            ProjectRequest req = context.get("projectRequest");

            Map<String, Integer> requiredItems = req.getRequiredItemsSQLId().stream()
                    .collect(Collectors.toMap(
                            item -> String.valueOf(item.getSqlId()),
                            ProjectItem::getQuantityNeeded
                    ));

            List<Resource> resources = input.stream().map(inventoryItem -> Resource.newBuilder()
                    .setId(String.valueOf(inventoryItem.getId()))
                    .setName(inventoryItem.getName())
                    .setCapacity(inventoryItem.getAvailableQuantity())
                    .setCost(10.0)
                    .build())
                    .collect(Collectors.toList());

            return AllocationRequest.newBuilder().
                    addProjects(Project.newBuilder()
                            .setId(req.getRequestId())
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

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

    private ManagedChannel channel;

    @BeforeEach
    public void init() {
        channel = ManagedChannelBuilder.forAddress("localhost", 8082).usePlaintext().build();
    }
    @AfterEach
    public void cleanup() throws InterruptedException {
        channel.shutdown();
        channel.awaitTermination(10, TimeUnit.SECONDS);
    }

    private class AllocationServiceCall implements Handler<AllocationRequest, AllocationResponse> {
        private BaseTransactionContext context;
        @Override
        public AllocationResponse process(AllocationRequest input) {
            var stub = MutinyResourceAllocationServiceGrpc.newMutinyStub(channel);
            return stub.allocateResources(input)
                    .flatMap(initialResponse -> {
                        context.logStep("Service fist response received.");
                        if (initialResponse.getStatus() != AllocationStatus.PENDING) {
                            return Uni.createFrom().item(initialResponse);
                        }
                        // Poll for status every second until status != PENDING.
                        return Uni.createFrom().item(initialResponse)
                                .onItem().delayIt().by(Duration.ofSeconds((long) 0.5))
                                .flatMap(resp -> {
                                    context.logStep("Polling: " + resp.getStatus().name());

                                    return stub.getAllocationStatus(
                                            StatusRequest.newBuilder()
                                                    .setAllocationId(resp.getAllocationId())
                                                    .build());
                                })
                                .repeat().until(response -> response.getStatus() != AllocationStatus.PENDING)
                                .collect().last()
                                // Convert the final StatusResponse to AllocationResponse.
                                .map(statusResponse -> AllocationResponse.newBuilder()
                                        .setAllocationId(statusResponse.getAllocationId())
                                        .setStatus(statusResponse.getStatus())
                                        .putAllProjectAllocations(statusResponse.getProjectAllocationsMap())
                                        .build());
                    })
                    .await().indefinitely();
        }

        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

    private static class ProcessAllocationResponseSingleProject implements Handler<AllocationResponse, ProjectRequest>{
        private BaseTransactionContext context;


        @Override
        public ProjectRequest process(AllocationResponse input) {
            ProjectRequest req = context.get("projectRequest");
            req.setAllocationId(input.getAllocationId());

            ProjectAllocatedResources allocatedResources = input.getProjectAllocationsMap().entrySet()
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
            req.setStatus(RequestStatus.APPROVED);
            return req;
        }


        @Override
        public void setContext(BaseTransactionContext context) {
            this.context = context;
        }
    }

//    @Test
//    void testProjectRequestPipelineSingleProject(){
////        here we test project allocation when we want to test
////        SINGLE project feasibility against the whole InventoryItem table
//        final BaseTransactionContext context = new BaseTransactionContext();
//
//
//        DatabaseHandler<String, ProjectRequest, ProjectRequestService> mongoGetProjectRequestHandler =
//                new DatabaseHandler<>(projectRequestService,
//                        (reqId, repository) -> {
//                            var base = projectRequestService.findByRequestId(reqId);
//                            context.logStep("Fetched Request "+ base.requestId);
//                            context.put("projectRequest", base);
//                            return (ProjectRequest) base;
//                        }, () -> {});
//
//        DatabaseHandler<ProjectRequest, List<InventoryItem>, InventoryItemDAO> fetchInventoryItemsList =
//                new DatabaseHandler<>(inventoryItemDAO,
//                        (request, repository) -> {
//                            var base = inventoryItemDAO.findAllAvailable();
//                            context.logStep("Found " + base.size() + " available items");
//                            return base;
//                        }, () -> {});
//
//        DatabaseHandler<ProjectRequest, String, ProjectRequestService> mongoPersistRequestHandler =
//                new DatabaseHandler<>(projectRequestService,
//                        (req, repository) -> {
//                            repository.persist(req);
//                            context.put("rollback_mongoRequest", req);
//                            return req.requestId;
//                        },
//                        () -> {
//                            ProjectRequest req = context.get("projectRequest");
//                            if (req != null) {
////                                projectRequestService.persist(req);
//                                context.logStep("Rolled back Mongo request.");
//                            }
//                        });
//
//                //1. fetch request from mongo
//        var pipe = new Pipeline<>(mongoGetProjectRequestHandler, context)
//                //2. check association is verified
//                .addHandler(new CheckAssociationVerified())
//                //3. fetch List<InventoryItem> and save it in context
//                .addHandler(fetchInventoryItemsList)
//                //4. create allocation request
//                .addHandler(new CreateResourceAllocationRequest())
//                //6. call allocationService, wait for allocationService response, in the meantime keep polling for execution status and send it to frontend (????)
//                .addHandler(new AllocationServiceCall())
//                //7. parse the response and update the projects entity
//                .addHandler(new ProcessAllocationResponseSingleProject())
//                //8. update the projectResource allocation map
//                .addHandler(mongoPersistRequestHandler);
//
//        var result = pipe.execute("PR1234");
//        assertEquals("PR1234", result);
//
//    }
}
