package org.acme.service.ext;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
@Singleton
public class ResourceAllocationServiceChannel {

    private ManagedChannel channel;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress("localhost", 8082).usePlaintext().build();
    }

    @PreDestroy
    public void cleanup() throws InterruptedException {
        channel.shutdown();
        channel.awaitTermination(10, TimeUnit.SECONDS);
    }

}