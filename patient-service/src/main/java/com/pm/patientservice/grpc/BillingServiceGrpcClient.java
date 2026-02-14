package com.pm.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

@Service
public class BillingServiceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);

//todo : fix this

//    @Value("${billing.service.address:localhost}")
//    private String serverAddress;
//
//    @Value("${billing.service.grpc.port:9001}")
//    private int serverPort;


    private String serverAddress = "billing-service";  // or "localhost" for local testing
    private int serverPort = 9001;

    private ManagedChannel channel;
    private BillingServiceGrpc.BillingServiceBlockingStub blockingStub;
    private boolean initialized = false;

    public BillingServiceGrpcClient() {
        log.info("BillingServiceGrpcClient bean created");
    }

    private synchronized void initializeChannel() {
        if (!initialized) {
            log.info("========================================");
            log.info("Initializing gRPC channel");
            log.info("Server: {}:{}", serverAddress, serverPort);
            log.info("========================================");

            try {
                channel = ManagedChannelBuilder
                        .forAddress(serverAddress, serverPort)
                        .usePlaintext()
                        .build();

                blockingStub = BillingServiceGrpc.newBlockingStub(channel);
                initialized = true;
                log.info("gRPC channel initialized successfully!");
            } catch (Exception e) {
                log.error("Failed to initialize gRPC channel", e);
                throw new RuntimeException("Cannot initialize gRPC channel", e);
            }
        }
    }

    public BillingResponse createBillingAccount(String patientId, String name, String email) {
        if (!initialized) {
            initializeChannel();
        }

        log.info("========================================");
        log.info("PATIENT SERVICE: Calling billing service");
        log.info("Target: {}:{}", serverAddress, serverPort);
        log.info("Patient: {} - {} - {}", patientId, name, email);

        try {
            BillingRequest request = BillingRequest.newBuilder()
                    .setPatientId(patientId)
                    .setName(name)
                    .setEmail(email)
                    .build();

            log.info("Sending gRPC request...");
            BillingResponse response = blockingStub.createBillingAccount(request);

            log.info("SUCCESS! Account ID: {}, Status: {}",
                    response.getAccountId(), response.getStatus());
            log.info("========================================");

            return response;

        } catch (StatusRuntimeException e) {
            log.error("gRPC call FAILED: {}", e.getStatus());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PreDestroy
    public void cleanup() {
        if (channel != null && !channel.isShutdown()) {
            log.info("Shutting down gRPC channel");
            channel.shutdown();
        }
    }
}