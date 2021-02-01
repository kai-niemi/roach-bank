package io.roach.bank.client.command;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import io.roach.bank.api.support.Money;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.support.Console;
import io.roach.bank.client.util.ByteFormat;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class GenerateCSV extends RestCommandSupport {
    private static int spinner = 0;

    private static void tick(String prefix) {
        System.out.printf(Locale.US, "\r%30s (%s)", prefix, "|/-\\".toCharArray()[spinner++ % 4]);
    }

    @Autowired
    private Console console;

    @ShellMethod(value = "Generate account import files in CSV format", key = {"gen-csv"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void generateCSV(
            @ShellOption(help = "file destination path", defaultValue = ".data") String destination,
            @ShellOption(help = "file suffix", defaultValue = "") String suffix,
            @ShellOption(help = "initial account balance in regional currency", defaultValue = "1000.00")
                    String initialBalance,
            @ShellOption(help = "number of CSV files per table", defaultValue = "10") int numFiles,
            @ShellOption(help = "number of accounts in total", defaultValue = "5000") String accounts,
            @ShellOption(help = "number of transactions per account", defaultValue = "0") int transactionsPerAccount,
            @ShellOption(help = "number of legs per transaction (multiple of 2)", defaultValue = "2")
                    int legsPerTransaction,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions
    ) {
        final Map<String, Currency> regionMap = lookupRegions(regions);
        if (regionMap.isEmpty()) {
            console.warn("No matching regions");
            return;
        }

        Path path = Paths.get(destination);
        if (!path.toFile().isDirectory()) {
            if (!path.toFile().mkdirs()) {
                console.error("Unable to create destination dir: " + destination);
                return;
            }
        }

        if (transactionsPerAccount > 0 && legsPerTransaction % 2 != 0) {
            console.error("Transaction legs must be multiple of two: " + legsPerTransaction);
            return;
        }

        final List<WriterGroup> writers = new LinkedList<>();

        final String finalSuffix;
        if (StringUtils.hasLength(suffix) && !suffix.startsWith("-")) {
            finalSuffix = "-" + suffix;
        } else
            finalSuffix= suffix;

        IntStream.rangeClosed(1, numFiles).forEach(value -> writers.add(WriterGroup.of(
                path.resolve("account-" + value + finalSuffix + ".csv"),
                path.resolve("transaction-" + value + finalSuffix + ".csv"),
                path.resolve("transaction-item-" + value + finalSuffix + ".csv")
        )));

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            final int nAccounts = Integer.parseInt(accounts.replace("_", ""));
            final int accountsPerRegion = nAccounts / regionMap.size();

            console.info(">> Generating CSV import files");
            console.info("%d regions: %s", regionMap.size(), regionMap.keySet());
            console.info("%,d accounts total ", nAccounts);
            console.info("%,d accounts per region ", accountsPerRegion);
            console.info("%,d transactions total", nAccounts * transactionsPerAccount);
            console.info("%,d transaction legs total", nAccounts * transactionsPerAccount * legsPerTransaction);

            CsvGenerator generator = new CsvGenerator();
            generator.writers = writers;
            generator.regionMap = regionMap;
            generator.initialBalance = initialBalance;
            generator.accountsPerRegion = accountsPerRegion;
            generator.transactionsPerAccount = transactionsPerAccount;
            generator.legsPerTransaction = legsPerTransaction;

            Future<Long> future = executor.submit(generator);
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            console.error("fatal: " + e.getCause());
            e.getCause().printStackTrace();

            writers.forEach(w -> w.wipe(console));
        } finally {
            executor.shutdownNow();

            writers.forEach(w -> {
                try {
                    w.close();
                    w.printSummary(console);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static class WriterGroup implements Closeable {
        static WriterGroup of(Path... p) {
            return new WriterGroup(p);
        }

        List<Path> paths;

        List<PrintWriter> writers;

        public WriterGroup(Path... paths) {
            this.paths = Arrays.asList(paths);
            this.writers = new ArrayList<>(paths.length);
        }

        public PrintWriter writer(int i) throws IOException {
            if (writers.size() <= i) {
                this.writers.add(new PrintWriter(new BufferedWriter(new FileWriter(paths.get(i).toFile()), 32768)));
            }
            return writers.get(i);
        }

        @Override
        public void close() throws IOException {
            writers.forEach(writer -> {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            });
        }

        public void wipe(Console console) {
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            paths.forEach(p -> {
                if (p != null && p.toFile().isFile()) {
                    console.info("Deleting %s", p.toAbsolutePath());
                    if (!p.toFile().delete()) {
                        p.toFile().deleteOnExit();
                    }
                }
            });
        }

        public void printSummary(Console console) {
            paths.forEach(p -> {
                if (p != null && p.toFile().isFile()) {
                    try {
                        console.info("Created %s (%s)", p.toAbsolutePath(),
                                ByteFormat.byteCountToDisplaySize(Files.size(p)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private static class CsvGenerator implements Callable<Long> {
        List<WriterGroup> writers = new LinkedList<>();

        Map<String, Currency> regionMap;

        String initialBalance;

        int accountsPerRegion;

        int transactionsPerAccount;

        int legsPerTransaction;

        @Override
        public Long call() throws Exception {
            for (final String region : regionMap.keySet()) {
                final Money balance = Money.of(initialBalance, regionMap.get(region));

                final String tsISO = LocalDateTime.now()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ"));

                final String dateISO = LocalDateTime.now()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                final List<UUID> accountIds = new ArrayList<>(accountsPerRegion);

                for (int i = 1; i <= accountsPerRegion; i++) {
                    final UUID accountId = UUID.randomUUID();

                    accountIds.add(accountId);

                    WriterGroup group = writers.get((i - 1) % writers.size());

                    group.writer(0).printf("%s,%s,%s,%s,%s:%04d,%s,%s,%s,%d,%s\n",
                            accountId, // id
                            region, // region iso code
                            balance.getAmount(), // amount
                            balance.getCurrency(), // currency code
                            "user", // name
                            i, // name
                            "", // desc
                            "A", // type
                            false, // closed
                            0, // allow_neg
                            tsISO);

                    if (i % 1000 == 0) {
                        tick(region + String.format(" %,d ", i)
                                + Math.round((0f + i) / accountsPerRegion * 100.0) + "%");
                    }

                    for (int t = 1; accountIds.size() >= legsPerTransaction && t <= transactionsPerAccount; t++) {
                        final UUID txId = UUID.randomUUID();

                        final AtomicReference<Money> balanceRef = new AtomicReference<>(
                                RandomData.randomMoneyBetween("0.50", "1.00", balance.getCurrency()));

                        group.writer(1).printf("%s,%s,%s,%s,GEN\n",
                                txId, // id
                                region,
                                dateISO,
                                dateISO);

                        RandomData.selectRandomUnique(accountIds, legsPerTransaction).forEach((k) -> {
                            try {
                                group.writer(2).printf("%s,%s,%s,%s,%s,%s,%s,%s\n",
                                        txId, // tx id
                                        region, // tx region
                                        k, // account id
                                        region, // account region
                                        balanceRef.get().getAmount(), // amount
                                        balanceRef.get().getCurrency(), // currency
                                        "(empty)", // note
                                        balance.getAmount().subtract(balanceRef.get().getAmount()) // running balance
                                );
                                balanceRef.set(balanceRef.get().negate());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
                System.out.println();
            }

            return null;
        }
    }
}
