package io.roach.bank.client.command;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.roach.bank.api.support.Money;
import io.roach.bank.client.util.ByteFormat;
import io.roach.bank.client.support.Console;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class GenerateSQL extends RestCommandSupport {
    @Autowired
    protected Console console;

    @ShellMethod(value = "Generate account plan in SQL format", key = {"gen-sql"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void generateSQL(
            @ShellOption(help = "output path", defaultValue = ".data") String output,
            @ShellOption(help = "initial account balance in regional currency", defaultValue = "1000.00") String balance,
            @ShellOption(help = "number of accounts per region", defaultValue = "50") int accountsPerRegion,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions
    ) throws IOException {
        final Map<String, Currency> regionMap = lookupRegions(regions);
        if (regionMap.isEmpty()) {
            console.warn("No matching regions");
            return;
        }

        Path path = Paths.get(output);
        if (!path.toFile().isDirectory()) {
            if (!path.toFile().mkdirs()) {
                console.error("Unable to create destination dir: " + output);
                return;
            }
        }

        path = path.resolve("load-accounts.sql");

        try (BufferedWriter writer = Files
                .newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("-- balance per account: " + balance + "\n");
            writer.write("-- accounts per region: " + accountsPerRegion + "\n");
            writer.write("-- accounts total: " + accountsPerRegion * regionMap.size() + "\n");
            writer.write("-- regions: " + regionMap.keySet() + "\n");
            writer.newLine();
            writer.write("-- TRUNCATE TABLE transaction_item CASCADE;\n");
            writer.write("-- TRUNCATE TABLE transaction CASCADE;\n");
            writer.write("-- TRUNCATE TABLE account CASCADE;\n");
            writer.newLine();

            for (String region : regionMap.keySet()) {
                final Currency currency = regionMap.get(region);
                final Money money = Money.of(balance, currency);

                writer.write(String.format("-- %s | %s\n", region, money.getCurrency()));
                writer.write(
                        "INSERT INTO account (id,region,balance,currency,name,type,closed,allow_negative,updated) VALUES");

                for (int i = 1; i <= accountsPerRegion; i++) {
                    writer.write(String.format(
                            "\t(gen_random_uuid(), '%s', '%s', '%s', 'user:%04d', 'A', false, 0, clock_timestamp())",
                            region,
                            money.getAmount(),
                            money.getCurrency().getCurrencyCode(),
                            i));
                    if (i < accountsPerRegion) {
                        writer.write(",");
                    }
                    writer.newLine();
                }
                writer.write(";");
            }

            writer.newLine();
        }
        console.info("Created %s (%s)", path, ByteFormat.byteCountToDisplaySize(Files.size(path)));
    }
}
