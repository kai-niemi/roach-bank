package io.roach.bank;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.roach.bank.api.support.LocalDateDeserializer;
import io.roach.bank.api.support.LocalDateSerializer;
import io.roach.bank.api.support.LocalDateTimeDeserializer;
import io.roach.bank.api.support.LocalDateTimeSerializer;

@Configuration
@EnableAutoConfiguration(exclude = {
        TransactionAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        KafkaAutoConfiguration.class,
        ErrorMvcAutoConfiguration.class
})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaRepositories(basePackages = {"io.roach"})
@ComponentScan(basePackages = "io.roach")
@ServletComponentScan
public class Application {
    @Bean
    public Module module() {
        SimpleModule module = new SimpleModule("RoachBankModule", new Version(1, 0, 0, null, null, null));
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        return module;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .logStartupInfo(false)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
