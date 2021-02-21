package io.roach.bank.event;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import io.roach.bank.ProfileNames;
import io.roach.bank.annotation.TransactionNotAllowed;

@Service
@Profile(ProfileNames.CDC_KAFKA)
public class KafkaChangeFeedConsumer {
    private final static String TOPIC_ACCOUNTS = "account";

    private final static String TOPIC_TRANSACTIONS = "transaction";

    private final static String TOPIC_TRANSACTION_LEGS = "transaction_item";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountChangePublisher changeFeedPublisher;

    @PostConstruct
    public void init() {
        logger.info("Bootstrapping Kafka CDC-sink change feed publisher");
    }

    @KafkaListener(topics = TOPIC_ACCOUNTS, containerFactory = "accountListenerContainerFactory")
    @TransactionNotAllowed
    public void accountChanged(@Payload AccountChangeEvent event,
                               @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                               @Header(KafkaHeaders.OFFSET) int offset) {
        changeFeedPublisher.publish(event);
    }

    @KafkaListener(topics = TOPIC_TRANSACTIONS, containerFactory = "transactionListenerContainerFactory")
    @TransactionNotAllowed
    public void transactionCreated(@Payload TransactionChangeEvent event,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                   @Header(KafkaHeaders.OFFSET) int offset) {
    }

    @KafkaListener(topics = TOPIC_TRANSACTION_LEGS, containerFactory = "transactionLegListenerContainerFactory")
    @TransactionNotAllowed
    public void transactionLegCreated(@Payload TransactionItemChangeEvent event,
                                      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                      @Header(KafkaHeaders.OFFSET) int offset) {
    }
}
