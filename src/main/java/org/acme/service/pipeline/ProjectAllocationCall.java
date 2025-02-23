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
        AllocationResponse response = stub.allocateResourcesLinearProgramming(input)
                .onItem().invoke(resp -> context.logStep("Received response with status: " + resp.getStatus().name()))
                .await().indefinitely();

        channel.shutdown();

        return response;
    }

    @Override
    public void setContext(BaseTransactionContext context) {
        this.context = context;
    }
}
