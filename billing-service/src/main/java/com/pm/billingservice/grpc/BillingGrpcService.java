package com.pm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createBillingAccount(BillingRequest request,
                                     StreamObserver<BillingResponse> responseObserver) {

        log.info("========================================");
        log.info("BILLING SERVICE: gRPC request received!");
        log.info("========================================");
        log.info("Patient ID: {}", request.getPatientId());
        log.info("Name: {}", request.getName());
        log.info("Email: {}", request.getEmail());

        try {
            // Generate account ID
            String accountId = "ACC-" + System.currentTimeMillis();

            // Build response
            BillingResponse response = BillingResponse.newBuilder()
                    .setAccountId(accountId)
                    .setStatus("ACTIVE")
                    .build();

            log.info("Sending response: accountId={}, status={}", accountId, "ACTIVE");

            // Send response
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("Response sent successfully!");
            log.info("========================================");

        } catch (Exception e) {
            log.error("Error processing billing request", e);
            responseObserver.onError(e);
        }
    }
}