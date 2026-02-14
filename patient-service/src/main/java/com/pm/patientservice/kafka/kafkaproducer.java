package com.pm.patientservice.kafka;

import com.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class kafkaproducer {

    private static final Logger log = LoggerFactory.getLogger(kafkaproducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public kafkaproducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Patient patient) {
        PatientEvent event = PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("PATIENT_CREATED")
                .build();

        try {
            log.info("Sending patient event to Kafka topic 'patient' for patient ID: {}", patient.getId());

            // Wait for the send to complete and get the result
            var future = kafkaTemplate.send("patient", event.toByteArray());

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent patient event. Topic: {}, Partition: {}, Offset: {}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send patient event for patient ID: {}", patient.getId(), ex);
                }
            });

            // Optional: If you want to wait synchronously (blocks until sent)
            // future.get(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("Error while sending Patient Created event for patient: {}", patient.getId(), e);
            throw new RuntimeException("Failed to send Kafka event", e);
        }
    }
}
