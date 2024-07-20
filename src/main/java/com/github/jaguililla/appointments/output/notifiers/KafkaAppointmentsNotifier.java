package com.github.jaguililla.appointments.output.notifiers;

import com.github.jaguililla.appointments.domain.AppointmentsNotifier;
import com.github.jaguililla.appointments.domain.Event;
import com.github.jaguililla.appointments.domain.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaAppointmentsNotifier implements AppointmentsNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAppointmentsNotifier.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String notifierTopic;
    private final String createMessage;
    private final String deleteMessage;

    public KafkaAppointmentsNotifier(
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
            });
    }
}
