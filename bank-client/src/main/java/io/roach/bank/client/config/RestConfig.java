package io.roach.bank.client.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.roach.bank.client.support.TraversonHelper;

@Configuration
public class RestConfig {
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
    @Primary
    public RestTemplate restTemplate(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        factory.setBufferRequestBody(true);
        factory.setReadTimeout(30_000);

        return new RestTemplate(factory);
    }

    @Bean
    public RestTemplate traversonRestTemplate(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        factory.setBufferRequestBody(true);
        factory.setReadTimeout(30_000);

        RestTemplate template = new RestTemplate(factory);
        template.setMessageConverters(getHttpMessageConverters());
        return template;
    }

    private static final List<MediaType> HAL_FLAVORS = Collections.singletonList(MediaTypes.HAL_JSON);

    private static List<HttpMessageConverter<?>> getHttpMessageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));

        List<MediaType> halFlavors = TraversonHelper.ACCEPT_TYPES.stream()
                .filter(HAL_FLAVORS::contains)
                .collect(Collectors.toList());

        if (!halFlavors.isEmpty()) {
            converters.add(getHalConverter(halFlavors));
        }

        return converters;
    }

    private static HttpMessageConverter<?> getHalConverter(List<MediaType> halFlavours) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jackson2HalModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);
        converter.setSupportedMediaTypes(halFlavours);

        return converter;
    }
}
