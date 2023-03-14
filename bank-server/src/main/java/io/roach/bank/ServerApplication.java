package io.roach.bank;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration(exclude = {
        TransactionAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        KafkaAutoConfiguration.class,
        ErrorMvcAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaRepositories(basePackages = {"io.roach"})
@ComponentScan(basePackages = "io.roach")
@ServletComponentScan
public class ServerApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ServerApplication.class)
                .logStartupInfo(false)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
