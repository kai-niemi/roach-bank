package io.roach.bank.client.command;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Currency;
import java.util.Map;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.roach.bank.api.support.Money;
import io.roach.bank.client.support.ByteFormat;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class GenerateSQL extends RestCommandSupport {
    @ShellMethod(value = "Generate account plan in SQL format", key = {"gen-sql"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void generateSQL(
            @ShellOption(help = "output path", defaultValue = ".data") String output,
            @ShellOption(help = "initial account balance in regional currency", defaultValue = "100000.00")
                    String balance,
            @ShellOption(help = "number of accounts per region", defaultValue = "100") int accountsPerRegion
    ) throws IOException {
        final Map<String, Currency> cityCurrencyMap = getCityCurrencyMap();

        Path path = Paths.get(output);
        if (!path.toFile().isDirectory()) {
            if (!path.toFile().mkdirs()) {
                logger.warn("Unable to create destination dir: {}", output);
                return;
            }
        }

        path = path.resolve("V1_3__load_accounts.sql");

        try (BufferedWriter writer = Files
                .newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("-- balance per account: " + balance + "\n");
            writer.write("-- accounts per region: " + accountsPerRegion + "\n");
            writer.write("-- accounts total: " + accountsPerRegion * cityCurrencyMap.size() + "\n");
            writer.write("-- regions: " + cityCurrencyMap.keySet() + "\n");
            writer.newLine();

            cityCurrencyMap.forEach((city, currency) -> {
                final Money money = Money.of(balance, currency);

                try {
                    writer.write(String.format("-- %s | %s\n", city, money.getCurrency()));
                    writer.write("INSERT INTO account (id,city,balance,currency,name,type,closed,allow_negative,updated) VALUES");

                    for (int i = 1; i <= accountsPerRegion; i++) {
                        writer.write(String.format(
                                "\t(gen_random_uuid(), '%s', '%s', '%s', 'user:%04d', 'A', false, 0, clock_timestamp())",
                                city,
                                money.getAmount(),
                                money.getCurrency().getCurrencyCode(),
                                i));
                        if (i < accountsPerRegion) {
                            writer.write(",");
                        }
                        writer.newLine();
                    }
                    writer.write(";");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            writer.newLine();
        }
        logger.info("Created {} ({})", path, ByteFormat.byteCountToDisplaySize(Files.size(path)));
    }
}
