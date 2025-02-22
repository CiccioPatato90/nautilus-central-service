package org.acme.service.ext;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.exceptions.ResourceAllocationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resourceallocation.AllocationRequest;
import resourceallocation.AllocationResponse;
import resourceallocation.MutinyResourceAllocationServiceGrpc;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

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

    public AllocationResponse allocateResources(AllocationRequest request) throws ResourceAllocationException {
        ManagedChannel channel = null;
        try {
            channel = createChannel();
            var stub = MutinyResourceAllocationServiceGrpc.newMutinyStub(channel);

            return stub.allocateResources(request)
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
}
