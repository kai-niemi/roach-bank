package io.roach.bank.health;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.roach.bank.ProfileNames;

@Component
@Profile(ProfileNames.CDC_KAFKA)
public class KafkaHealthIndicator implements HealthIndicator {
    @Autowired
    private AdminClient adminClient;

    private final DescribeClusterOptions describeClusterOptions = new DescribeClusterOptions().timeoutMs(1000);

    @Override
    public Health health() {
        final DescribeClusterResult describeCluster = adminClient.describeCluster(describeClusterOptions);
        try {
            final String clusterId = describeCluster.clusterId().get();
            final int nodeCount = describeCluster.nodes().get().size();
            return Health.up()
                    .withDetail("clusterId", clusterId)
                    .withDetail("nodeCount", nodeCount)
                    .build();
        } catch (InterruptedException | ExecutionException e) {
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
}
