package io.roach.bank.client.command;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.roach.bank.api.support.Money;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.support.Console;
import io.roach.bank.client.util.ByteFormat;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class GenerateCSV extends RestCommandSupport {
    private static int spinner = 0;

    private static void tick(String prefix) {
        System.out.printf("\r%10s (%s)", prefix, "|/-\\".toCharArray()[spinner++ % 4]);
    }

    @Autowired
    private Console console;

    @ShellMethod(value = "Generate account import files in CSV format", key = {"gen-csv"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void generateCSV(
            @ShellOption(help = "file destination path", defaultValue = ".data") String destination,
            @ShellOption(help = "initial account balance in regional currency", defaultValue = "1000.00")
                    String initialBalance,
            @ShellOption(help = "number of CSV files per table", defaultValue = "10") int numFiles,
            @ShellOption(help = "number of accounts in total", defaultValue = "5000") int accounts,
            @ShellOption(help = "skip transactions and legs", defaultValue = "false") boolean noLegs,
            @ShellOption(help = "number of transactions per account", defaultValue = "1") int transactionsPerAccount,
            @ShellOption(help = "number of legs per transaction (multiple of 2)", defaultValue = "2")
                    int legsPerTransaction,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions
    ) throws IOException {
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

        List<Path> paths = new LinkedList<>();
        List<List<BufferedWriter>> writers = new LinkedList<>();

        for (int i = 1; i <= numFiles; i++) {
            Path p1 = path.resolve("account-" + i + ".csv");
            Path p2 = path.resolve("transaction-" + i + ".csv");
            Path p3 = path.resolve("transaction-item-" + i + ".csv");

            paths.addAll(Arrays.asList(p1, p2, p3));

            writers.add(Arrays.asList(
                    new BufferedWriter(new FileWriter(p1.toFile()), 32768),
                    new BufferedWriter(new FileWriter(p2.toFile()), 32768),
                    new BufferedWriter(new FileWriter(p3.toFile()), 32768)
            ));
        }

        try {
            final int accountsPerRegion = accounts / regionMap.size();

            if (noLegs) {
                transactionsPerAccount = 0;
                legsPerTransaction = 0;
            }

            console.info(">> Genering CSV import files");
            console.info("%d regions: %s", regionMap.size(), regionMap.keySet());
            console.info("%,d accounts total ", accounts);
            console.info("%,d accounts per region ", accountsPerRegion);
            console.info("%,d transactions total", accounts * transactionsPerAccount);
            console.info("%,d transaction legs total", accounts * transactionsPerAccount * legsPerTransaction);

            UnitOfWork unitOfWork = new UnitOfWork();
            unitOfWork.writers = writers;
            unitOfWork.regionMap = regionMap;
            unitOfWork.initialBalance = initialBalance;
            unitOfWork.accountsPerRegion = accountsPerRegion;
            unitOfWork.transactionsPerAccount = transactionsPerAccount;
            unitOfWork.legsPerTransaction = legsPerTransaction;

            Future<Long> future = Executors.newSingleThreadExecutor().submit(unitOfWork);
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            console.error("fatal: " + e.getMessage());
            e.printStackTrace();
        } finally {
            writers.forEach(bufferedWriters -> bufferedWriters.forEach(writer -> {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            paths.forEach(p -> {
                try {
                    console.info("Created %s (%s)", p, ByteFormat.byteCountToDisplaySize(Files.size(p)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static class UnitOfWork implements Callable<Long> {
        List<List<BufferedWriter>> writers = new LinkedList<>();

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

                    List<BufferedWriter> bufferedWriters = writers.get((i - 1) % writers.size());
                    final BufferedWriter writer1 = bufferedWriters.get(0);
                    final BufferedWriter writer2 = bufferedWriters.get(1);
                    final BufferedWriter writer3 = bufferedWriters.get(2);

                    // import into account(id,region,balance,currency,name,description,type,closed,allow_negative,updated) CSV DATA ('nodelocal://1/account.csv');
                    writer1.write(String.format("%s,%s,%s,%s,%s:%04d,%s,%s,%s,%d,%s\n",
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
                            tsISO));

                    if (i % 1000 == 0) {
                        tick(region + String.format(" %,d ", i)
                                + Math.round((0f + i) / accountsPerRegion * 100.0) + "%");
                    }

                    for (int t = 1; accountIds.size() >= legsPerTransaction && t <= transactionsPerAccount; t++) {
                        final UUID txId = UUID.randomUUID();

                        final AtomicReference<Money> balanceRef = new AtomicReference<>(
                                RandomData.randomMoneyBetween("0.50", "1.00", balance.getCurrency()));

                        // import into transaction(id,region,booking_date,transfer_date,transaction_type)
                        writer2.write(String.format("%s,%s,%s,%s,GEN\n",
                                txId, // id
                                region,
                                dateISO,
                                dateISO));

                        RandomData.selectRandomUnique(accountIds, legsPerTransaction).forEach((k) -> {
                            // import into transaction_item(transaction_id,transaction_region,account_id,account_region,amount,currency,note,running_balance)
                            try {
                                writer3.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                                        txId, // tx id
                                        region, // tx region
                                        k, // account id
                                        region, // account region
                                        balanceRef.get().getAmount(), // amount
                                        balanceRef.get().getCurrency(), // currency
                                        "(empty)", // note
                                        balance.getAmount().subtract(balanceRef.get().getAmount()) // running balance
                                ));
                                balanceRef.set(balanceRef.get().negate());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }

            return null;
        }
    }
}
