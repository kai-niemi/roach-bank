package io.roach.bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.roach.bank.service.AccountPlanBuilder;

@Configuration
@EnableAutoConfiguration(exclude = {
        TransactionAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        ErrorMvcAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaRepositories(basePackages = {"io.roach"}, enableDefaultTransactions = false)
@ComponentScan(basePackages = "io.roach")
@ServletComponentScan
public class ServerApplication implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        new SpringApplicationBuilder(ServerApplication.class)
                .logStartupInfo(false)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

    @Autowired
    private AccountPlanBuilder accountPlanBuilder;

    @Override
    public void run(ApplicationArguments args) {
        accountPlanBuilder.buildAccountPlan();
        logger.info("RoachBank is now open for business - lets invent some $$");
    }
}
