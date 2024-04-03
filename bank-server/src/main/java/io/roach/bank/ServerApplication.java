package io.roach.bank;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.util.StringUtils;

import io.roach.bank.service.AccountPlanBuilder;

@Configuration
@EnableAutoConfiguration(exclude = {
        TransactionAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        ErrorMvcAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
@ConfigurationPropertiesScan(basePackageClasses = ServerApplication.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaRepositories(basePackages = {"io.roach"}, enableDefaultTransactions = false)
@ComponentScan(basePackages = "io.roach")
@ServletComponentScan
public class ServerApplication implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static void printHelpAndExit(String message) {
        System.out.println("Usage: java --jar bank-server.jar <options> [args..]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("--help                    this help");
        System.out.println("--profiles=[profile,..]   spring profiles to activate");
        System.out.println();
        System.out.println("All other options are passed to the shell.");
        System.out.println();
        System.out.println(message);

        System.exit(0);
    }

    public static void main(String[] args) {
        LinkedList<String> argsList = new LinkedList<>(Arrays.asList(args));
        LinkedList<String> passThroughArgs = new LinkedList<>();

        Set<String> profiles =
                StringUtils.commaDelimitedListToSet(System.getProperty("spring.profiles.active", ""));

        while (!argsList.isEmpty()) {
            String arg = argsList.pop();
            if (arg.equals("--help")) {
                printHelpAndExit("");
            } else if (arg.equals("--dev")) {
                profiles.add(ProfileNames.PGJDBC_DEV);
            } else if (arg.startsWith("--profiles") || arg.startsWith("--spring.profiles.active")) {
                String[] parts = arg.split("=");
                if (parts.length != 2) {
                    printHelpAndExit("Expected value");
                }
                profiles.clear();
                profiles.addAll(StringUtils.commaDelimitedListToSet(parts[1].trim()));
            } else {
                passThroughArgs.add(arg);
            }
        }

        if (profiles.stream().filter(string -> string.startsWith("retry-"))
                .findAny().isEmpty()) {
//            System.out.printf("Didnt find retry profile in %s - adding %s\n", profiles, ProfileNames.RETRY_CLIENT);
            profiles.add(ProfileNames.RETRY_CLIENT);
        }

        System.setProperty("spring.profiles.active", StringUtils.collectionToCommaDelimitedString(profiles));

        new SpringApplicationBuilder(ServerApplication.class)
                .web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.CONSOLE)
                .logStartupInfo(true)
                .profiles(profiles.toArray(new String[0]))
                .run(passThroughArgs.toArray(new String[] {}));
    }

    @Autowired
    private AccountPlanBuilder accountPlanBuilder;

    @Override
    public void run(ApplicationArguments args) {
        accountPlanBuilder.buildAccountPlan();
    }
}
