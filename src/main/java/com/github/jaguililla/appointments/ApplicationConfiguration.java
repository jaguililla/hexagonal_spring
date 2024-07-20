package com.github.jaguililla.appointments;

import com.github.jaguililla.appointments.output.notifiers.KafkaAppointmentsNotifier;
import com.github.jaguililla.appointments.output.stores.SqlAppointmentsRepository;
import com.github.jaguililla.appointments.output.stores.SqlUsersRepository;
import com.github.jaguililla.appointments.domain.*;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
class ApplicationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfiguration.class);

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;
    @Value(value = "${notifierTopic}")
    private String notifierTopic;
    @Value(value = "${createMessage}")
    private String createMessage;
    @Value(value = "${deleteMessage}")
    private String deleteMessage;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress));
    }

    @Bean
    public NewTopic appointmentsTopic() {
        return new NewTopic("appointments", 1, (short) 1);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        ));
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress,
            ConsumerConfig.GROUP_ID_CONFIG, "group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        ));
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(
        final ProducerFactory<String, String> producerFactory,
        final ConsumerFactory<String, String> consumerFactory
    ) {
        final var kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setConsumerFactory(consumerFactory);
        return kafkaTemplate;
    }

    @Bean
    AppointmentsRepository appointmentsStore(final DataSource dataSource) {
        final var type = SqlAppointmentsRepository.class.getSimpleName();
        LOGGER.info("Creating Appointments Store: {}", type);
        return new SqlAppointmentsRepository(dataSource);
    }

    @Bean
    UsersRepository usersStore(final DataSource dataSource) {
        final var type = SqlUsersRepository.class.getSimpleName();
        LOGGER.info("Creating Users Store: {}", type);
        return new SqlUsersRepository(dataSource);
    }

    @Bean
    AppointmentsNotifier appointmentsNotifier(final KafkaTemplate<String, String> kafkaTemplate) {
        final var type = KafkaAppointmentsNotifier.class.getSimpleName();
        LOGGER.info("Creating Appointments Notifier: {}", type);
        return new KafkaAppointmentsNotifier(
            kafkaTemplate, notifierTopic, createMessage, deleteMessage
        );
    }

    @Bean
    Service service(
        final AppointmentsRepository appointmentsRepository,
        final UsersRepository usersRepository,
        final AppointmentsNotifier appointmentsNotifier
    ) {
        return new Service(appointmentsRepository, usersRepository, appointmentsNotifier);
    }
}
