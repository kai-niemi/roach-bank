package io.roach.bank.changefeed.ingress;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.roach.bank.ProfileNames;
import io.roach.bank.changefeed.model.AccountPayload;
import io.roach.bank.changefeed.model.TransactionChangeEvent;
import io.roach.bank.changefeed.model.TransactionItemChangeEvent;

@EnableKafka
@EnableConfigurationProperties(KafkaProperties.class)
@Configuration
@Profile(ProfileNames.CDC_KAFKA)
public class KafkaConsumerConfig implements KafkaListenerConfigurer {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Autowired
    private KafkaProperties properties;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return props;
    }

    private ObjectMapper lenientObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
    }

    @Bean
    public ConsumerFactory<String, AccountPayload> accountConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                new JsonDeserializer<>(AccountPayload.class, lenientObjectMapper())
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AccountPayload> accountListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AccountPayload> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(accountConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, TransactionChangeEvent> transactionConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                new JsonDeserializer<>(TransactionChangeEvent.class, lenientObjectMapper())
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionChangeEvent> transactionListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransactionChangeEvent> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(transactionConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, TransactionItemChangeEvent> transactionLegConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                new JsonDeserializer<>(TransactionItemChangeEvent.class, lenientObjectMapper())
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionItemChangeEvent> transactionLegListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransactionItemChangeEvent> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(transactionLegConsumerFactory());
        return factory;
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        KafkaAdmin kafkaAdmin = new KafkaAdmin(this.properties.buildAdminProperties());
        kafkaAdmin.setFatalIfBrokerNotAvailable(this.properties.getAdmin().isFailFast());
        return kafkaAdmin;
    }

    @Bean
    public AdminClient kafkaAdminClient() {
        return AdminClient.create(kafkaAdmin().getConfigurationProperties());
    }
}
