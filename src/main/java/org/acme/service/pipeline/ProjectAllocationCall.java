package org.acme.service.pipeline;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import org.acme.pattern.Handler;
import org.acme.pattern.context.BaseTransactionContext;
import resourceallocation.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Singleton
public class ProjectAllocationCall implements Handler<AllocationRequest, AllocationResponse> {
    //    @Inject
    //    ResourceAllocationServiceChannel resourceAllocationServiceChannel;

    private BaseTransactionContext context;

    public ManagedChannel initChannel(){
        return ManagedChannelBuilder.forAddress("localhost", 8082).usePlaintext().build();
    }

    public void deinitChannel(ManagedChannel channel){
        channel.shutdown();
    }

    @Override
    public AllocationResponse process(AllocationRequest input) {

        //var stub = MutinyResourceAllocationServiceGrpc.newMutinyStub(resourceAllocationServiceChannel.getChannel());
        var channel = initChannel();
        var stub = MutinyResourceAllocationServiceGrpc.newMutinyStub(channel);

        //find a way to deinit channel!!!!!
        AllocationResponse response = stub.allocateResources(input)
                .onItem().invoke(resp -> context.logStep("Received response with status: " + resp.getStatus().name()))
                .await().indefinitely();

        channel.shutdown();

        return response;


//        return stub.allocateResources(input)
//                .flatMap(initialResponse -> {
//                    context.logStep("Service fist response received.");
//                    if (initialResponse.getStatus() != AllocationStatus.PENDING) {
//                        return Uni.createFrom().item(initialResponse);
//                    }
//                    // Poll for status every second until status != PENDING.
//                    return Uni.createFrom().item(initialResponse)
//                            .onItem().delayIt().by(Duration.ofSeconds((long) 0.5))
//                            .flatMap(resp -> {
//                                context.logStep("Polling: " + resp.getStatus().name());
//
//                                return stub.getAllocationStatus(
//                                        StatusRequest.newBuilder()
//                                                .setAllocationId(resp.getAllocationId())
//                                                .build());
//                            })
//                            .repeat().until(response -> response.getStatus() != AllocationStatus.PENDING)
//                            .collect().last()
//                            // Convert the final StatusResponse to AllocationResponse.
//                            .map(statusResponse -> AllocationResponse.newBuilder()
//                                    .setAllocationId(statusResponse.getAllocationId())
//                                    .setStatus(statusResponse.getStatus())
//                                    .putAllProjectAllocations(statusResponse.getProjectAllocationsMap())
//                                    .build());
//                })
//                .await().indefinitely();
    }

    @Override
    public void setContext(BaseTransactionContext context) {
        this.context = context;
    }
}
