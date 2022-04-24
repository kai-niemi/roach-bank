package io.roach.bank.client.config;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.HypermediaRestTemplateConfigurer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import io.roach.bank.client.support.RestCommands;

@Configuration
@EnableHypermediaSupport(type = {
        EnableHypermediaSupport.HypermediaType.HAL_FORMS, EnableHypermediaSupport.HypermediaType.HAL
})
public class RestConfig {
    private static final int ONE_SECOND = 1000;

    private static final int ONE_MINUTE = ONE_SECOND * 60;

    private static final int FIVE_MINUTES = ONE_MINUTE * 5;

    @Value("${roachbank.http.maxTotal}")
    private int maxTotal;

    @Value("${roachbank.http.defaultMaxPerRoute}")
    private int defaultMaxPerRoute;

    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        if (defaultMaxPerRoute <= 0 || maxTotal <= 0) {
            defaultMaxPerRoute = Runtime.getRuntime().availableProcessors() * 4;
            maxTotal = defaultMaxPerRoute * 2;
        }

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(maxTotal);
        manager.setDefaultMaxPerRoute(defaultMaxPerRoute);

        return manager;
    }

    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(10_1000)
                .setConnectTimeout(10_1000)
                .setSocketTimeout(10_000)
                .build();
    }

    @Bean
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager clientConnectionManager,
                                          RequestConfig requestConfig) {
        return HttpClientBuilder
                .create()
                .setConnectionManager(clientConnectionManager)
                .setDefaultRequestConfig(requestConfig)
                .disableAutomaticRetries()
                .build();
    }

    @Bean
    public RestTemplate restTemplate(HttpClient httpClient,
                                     HypermediaRestTemplateConfigurer configurer) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        factory.setBufferRequestBody(true);
        factory.setReadTimeout(FIVE_MINUTES);

        RestTemplate restTemplate = new RestTemplate(factory);
        configurer.registerHypermediaTypes(restTemplate);
        return restTemplate;
    }

    @Bean
    public RestCommands restCommands(RestTemplate restTemplate) {
        return new RestCommands(restTemplate);
    }
}
