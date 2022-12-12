package io.roach.bank.changefeed.ingress;

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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.roach.bank.ProfileNames;
import io.roach.bank.changefeed.egress.AccountChangeWebSocketPublisher;
import io.roach.bank.changefeed.model.AccountPayload;
import io.roach.bank.changefeed.model.TransactionChangeEvent;
import io.roach.bank.changefeed.model.TransactionItemChangeEvent;

@Service
@Profile(ProfileNames.CDC_KAFKA)
public class KafkaChangeFeedConsumer {
    private final static String TOPIC_ACCOUNTS = "account";

    private final static String TOPIC_TRANSACTIONS = "transaction";

    private final static String TOPIC_TRANSACTION_LEGS = "transaction_item";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountChangeWebSocketPublisher changeFeedPublisher;

    @PostConstruct
    public void init() {
        logger.info("Bootstrapping Kafka CDC-sink change feed publisher");
    }

    @KafkaListener(topics = TOPIC_ACCOUNTS, containerFactory = "accountListenerContainerFactory")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void accountChanged(@Payload AccountPayload event,
                               @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                               @Header(KafkaHeaders.OFFSET) int offset) {
        changeFeedPublisher.publish(event);
    }

    @KafkaListener(topics = TOPIC_TRANSACTIONS, containerFactory = "transactionListenerContainerFactory")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Deprecated
    public void transactionCreated(@Payload TransactionChangeEvent event,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                   @Header(KafkaHeaders.OFFSET) int offset) {
    }

    @KafkaListener(topics = TOPIC_TRANSACTION_LEGS, containerFactory = "transactionLegListenerContainerFactory")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Deprecated
    public void transactionLegCreated(@Payload TransactionItemChangeEvent event,
                                      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                      @Header(KafkaHeaders.OFFSET) int offset) {
    }
}
