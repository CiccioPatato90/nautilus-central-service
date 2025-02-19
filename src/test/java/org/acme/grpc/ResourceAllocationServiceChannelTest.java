package org.acme.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.acme.model.requests.project.ProjectAllocatedResources;
import org.acme.model.requests.project.ProjectRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import resourceallocation.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class ResourceAllocationServiceChannelTest {
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

    @Test
    public void testAllocateResources() {

        AllocationResponse response = MutinyResourceAllocationServiceGrpc.newMutinyStub(channel)
                .allocateResources(AllocationRequest.newBuilder()
                        .addProjects(Project.newBuilder()
                                .setId("proj1")
                                .setName("Project 1")
                                .putRequirements("res1", 2)
                                .putRequirements("res4", 2)
                                .setPriority(1)
                                .build())
//                        .addProjects(Project.newBuilder()
//                                .setId("proj2")
//                                .setName("Project 2")
//                                .putRequirements("res1", 1)
//                                .putRequirements("res2", 4)
//                                .putRequirements("res3", 2)
//                                .setPriority(1)
//                                .build())
                        .addResources(Resource.newBuilder()
                                .setId("res1")
                                .setName("ResourceA")
                                .setCapacity(3)
                                .setCost(10.0)
                                .build())
                        .addResources(Resource.newBuilder()
                                .setId("res2")
                                .setName("ResourceB")
                                .setCapacity(2)
                                .setCost(1.0)
                                .build())
                        .setStrategy(AllocationStrategy.newBuilder()
                                .setStrategyName("default")
                                .build())
                        .build()).await().indefinitely();

        ProjectRequest req = new ProjectRequest();
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
        assertEquals(AllocationStatus.COMPLETED, response.getStatus());
        assertEquals(true, response.getProjectAllocationsMap().containsKey("50.0"));
    }
}