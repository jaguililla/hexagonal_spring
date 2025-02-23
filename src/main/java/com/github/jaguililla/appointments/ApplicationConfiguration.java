package com.github.jaguililla.appointments;

import com.github.jaguililla.appointments.domain.AppointmentsNotifier;
import com.github.jaguililla.appointments.domain.AppointmentsRepository;
import com.github.jaguililla.appointments.domain.AppointmentsService;
import com.github.jaguililla.appointments.domain.UsersRepository;
import com.github.jaguililla.appointments.notifiers.KafkaTemplateAppointmentsNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;

@Configuration
class ApplicationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfiguration.class);

    @Value(value = "${notifierTopic}")
    private String notifierTopic;
    @Value(value = "${createMessage}")
    private String createMessage;
    @Value(value = "${deleteMessage}")
    private String deleteMessage;

    @Bean
    AppointmentsNotifier appointmentsNotifier(final KafkaTemplate<String, String> kafkaTemplate) {
        final var type = KafkaTemplateAppointmentsNotifier.class.getSimpleName();
        LOGGER.info("Creating Appointments Notifier: {}", type);
        return new KafkaTemplateAppointmentsNotifier(
            kafkaTemplate, notifierTopic, createMessage, deleteMessage
        );
    }

    @Bean
    AppointmentsService appointmentsService(
        final AppointmentsRepository appointmentsRepository,
        final UsersRepository usersRepository,
        final AppointmentsNotifier appointmentsNotifier
    ) {
        return
            new AppointmentsService(appointmentsRepository, usersRepository, appointmentsNotifier);
    }
}
