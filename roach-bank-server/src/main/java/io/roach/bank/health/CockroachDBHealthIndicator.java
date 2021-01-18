package io.roach.bank.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.roach.bank.ProfileNames;
import net.minidev.json.JSONArray;

@Component
@Profile(ProfileNames.DB_COCKROACH)
public class CockroachDBHealthIndicator extends AbstractHealthIndicator {
    private final RestTemplate restTemplate = new RestTemplate();

    private final String healthEndpoint;

    private final String statusEndpoint;

    public CockroachDBHealthIndicator(@Value("${roachbank.health.admin-endpoint}") String adminEndpoint) {
        this.healthEndpoint = adminEndpoint + "/health?ready=1";
        this.statusEndpoint = adminEndpoint + "/_status/nodes";
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            ResponseEntity<String> response = restTemplate
                    .exchange(healthEndpoint, HttpMethod.GET, new HttpEntity<>(null), String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                response = restTemplate
                        .exchange(statusEndpoint, HttpMethod.GET, new HttpEntity<>(null), String.class);
                String body = response.getBody();
                DocumentContext ctx = JsonPath.parse(body);
                builder.up().withDetail("nodeIds", ctx.read("$.nodes[*].desc.nodeId", JSONArray.class));
                builder.up().withDetail("buildTags", ctx.read("$.nodes[*].desc.buildTag", JSONArray.class));
                builder.up().withDetail("selectCount",
                        ctx.read("$.nodes[*].metrics.['sql.select.count']", JSONArray.class));

                for (Object id : ctx.read("$.nodes[*].desc.nodeId", JSONArray.class)) {
                    builder.up().withDetail("node." + id + ".href", statusEndpoint + "/" + id);
                }
            } else {
                builder.down().withDetail("error", response.getBody());
            }
        } catch (RestClientException e) {
            builder.unknown().withException(e);
        }
    }
}
