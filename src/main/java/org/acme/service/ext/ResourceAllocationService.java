package org.acme.service.ext;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.ProjectRequestDTO;
import org.acme.exceptions.ResourceAllocationException;
import org.acme.model.enums.simulation.GreedyOrder;
import org.acme.model.enums.simulation.GreedyStrategy;
import org.acme.model.requests.project.ProjectAllocatedResources;
import org.acme.model.requests.project.ProjectItem;
import org.acme.model.requests.project.ProjectRequest;
import org.acme.model.response.requests.inventory.SimulateAllocationStats;
import org.acme.model.response.requests.inventory.SimulateRequestResponse;
import org.acme.model.response.requests.inventory.SimulateResourceUsage;
import org.acme.model.virtual_warehouse.item.InventoryItem;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resourceallocation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationScoped
public class ResourceAllocationService {
    private static final Logger logger = LoggerFactory.getLogger(ResourceAllocationService.class);

    private final String host;
    private final int port;

    @Inject
    public ResourceAllocationService(@ConfigProperty(name = "grpc.allocation.host", defaultValue = "localhost") String host,
                                         @ConfigProperty(name = "grpc.allocation.port", defaultValue = "8082") int port) {
        this.host = host;
        this.port = port;
    }

    public AllocationResponse allocateResourcesLinearProgramming(AllocationRequest request) throws ResourceAllocationException {
        ManagedChannel channel = null;
        try {
            channel = createChannel();
            var stub = MutinyResourceAllocationServiceGrpc.newMutinyStub(channel);

            return stub.allocateResourcesLinearProgramming(request)
                    .onItem().invoke(response ->
                            logger.info("Received allocation response - Status: {}, ID: {}",
                                    response.getStatus().name(),
                                    response.getAllocationId()))
                    .onFailure().invoke(throwable ->
                            logger.error("Failed to allocate resources", throwable))
                    .await().indefinitely();

        } catch (Exception e) {
            logger.error("Error during resource allocation", e);
            throw new ResourceAllocationException("Failed to allocate resources", e);
        } finally {
            shutdownChannel(channel);
        }
    }

    public AllocationResponse allocateResourcesGreedy(AllocationRequest request) throws ResourceAllocationException {
        ManagedChannel channel = null;
        try {
            channel = createChannel();
            var stub = MutinyResourceAllocationServiceGrpc.newMutinyStub(channel);

            return stub.allocateResourcesGreedy(request)
                    .onItem().invoke(response ->
                            logger.info("Received GREEDY allocation response - Status: {}, ID: {}",
                                    response.getStatus().name(),
                                    response.getAllocationId()))
                    .onFailure().invoke(throwable ->
                            logger.error("Failed to allocate resources", throwable))
                    .await().indefinitely();

        } catch (Exception e) {
            logger.error("Error during resource allocation", e);
            throw new ResourceAllocationException("Failed to allocate resources", e);
        } finally {
            shutdownChannel(channel);
        }
    }

    private ManagedChannel createChannel() {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    private void shutdownChannel(ManagedChannel channel) {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Channel shutdown interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public static AllocationRequest createAllocationRequestMPMI(List<ProjectRequest> req, GreedyOrder order, GreedyStrategy strategy, Map<Integer, Integer> availableMap) {

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

//        List<Resource> resources = availableItems.stream().map(inventoryItem -> Resource.newBuilder()
//                        .setId(String.valueOf(inventoryItem.getId()))
//                        .setName(inventoryItem.getName())
//                        .setCapacity(inventoryItem.getAvailableQuantity())
//                        .setCost(10.0)
//                        .build())
//                .collect(Collectors.toList());

        List<Resource> resources = availableMap.entrySet().stream()
                .map(inventoryEntry -> Resource.newBuilder()
                        .setId(String.valueOf(inventoryEntry.getKey()))
                        .setName("NO NAME")
                        .setCapacity(inventoryEntry.getValue())
                        .setCost(10.0)
                        .build())
                .collect(Collectors.toList());

        System.out.println("Mapping to Resources List.");

        return AllocationRequest.newBuilder()
                .addAllProjects(projects)
                .addAllResources(resources)
                .setStrategy(AllocationStrategy.newBuilder()
                        .setCriteria(GreedyStrategy.toProto(strategy))
                        .setOrder(GreedyOrder.toProto(order))
                        .build())
                .build();
    }


    public static SimulateRequestResponse processAllocationResponse(List<ProjectRequest> requests, AllocationResponse response) {
        var origCompletionPercentages = new HashMap<String, Double>();
        // Process each project request
        for (ProjectRequest req : requests) {
            // Set allocation ID for all requests
            req.setAllocationId(response.getAllocationId());

            if(req.getAllocatedResources() != null){
                origCompletionPercentages.put(req.get_id().toString(), (double) Math.round(req.getAllocatedResources().getCompletionPercentage()));
            }else{
                origCompletionPercentages.put(req.get_id().toString(), 0.0);
            }

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

        return new SimulateRequestResponse(requests.stream().map(ProjectRequestDTO::fromEntity).toList(), convertProtoStats(response.getGlobalStats()), origCompletionPercentages);
    }


    private static SimulateAllocationStats convertProtoStats(AllocationStats protoStats) {
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
