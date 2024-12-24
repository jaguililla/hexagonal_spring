package com.github.jaguililla.appointments.notifiers;

import com.github.jaguililla.appointments.domain.AppointmentsNotifier;
import com.github.jaguililla.appointments.domain.Event;
import com.github.jaguililla.appointments.domain.model.Appointment;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaTemplateAppointmentsNotifier implements AppointmentsNotifier {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(KafkaTemplateAppointmentsNotifier.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String notifierTopic;
    private final String createMessage;
    private final String deleteMessage;

    public KafkaTemplateAppointmentsNotifier(
        final KafkaTemplate<String, String> kafkaTemplate,
        final String notifierTopic,
        final String createMessage,
        final String deleteMessage
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.notifierTopic = notifierTopic;
        this.createMessage = createMessage;
        this.deleteMessage = deleteMessage;
    }

    @Override
    public void notify(final Event event, final Appointment appointment) {
        final var message = event == Event.CREATED ? createMessage : deleteMessage;

        try {
            kafkaTemplate
                .send(notifierTopic, message.formatted(appointment.start()))
                .whenComplete((result, e) -> {
                    if (e == null) {
                        final var metadata = result.getRecordMetadata();
                        LOGGER.info("Message: '{}' offset: {}", message, metadata.offset());
                    }
                    else {
                        LOGGER.info("Message: '{}' FAILED due to: {}", message, e.getMessage());
                    }
                })
                .get();
        }
        catch (InterruptedException | ExecutionException e) {
            var id = appointment.id();
            var errorMessage = "Error sending notification for appointment: %s".formatted(id);
            throw new IllegalStateException(errorMessage, e);
        }
    }
}
