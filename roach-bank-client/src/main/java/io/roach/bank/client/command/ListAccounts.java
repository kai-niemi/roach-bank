package io.roach.bank.client.command;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.BankLinkRelations;

import static io.roach.bank.api.BankLinkRelations.ACCOUNT_LIST_REL;
import static io.roach.bank.api.BankLinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.BankLinkRelations.withCurie;
import static io.roach.bank.client.command.Constants.ACCOUNT_MODEL_PTR;

@ShellComponent
@ShellCommandGroup(Constants.API_COMMANDS)
public class ListAccounts extends RestCommandSupport {
    @ShellMethod(value = "List accounts using pagination")
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listAccounts(@ShellOption(help = "page number", defaultValue = "0") int page,
                             @ShellOption(help = "page size", defaultValue = "20") int pageSize,
                             @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions) {
        final Map<String, Currency> regionMap = lookupRegions(regions);
        if (regionMap.isEmpty()) {
            console.warn("No matching regions found for: %s ", regions);
            return;
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", page);
        parameters.put("size", pageSize);
        parameters.put("regions", StringUtils.collectionToCommaDelimitedString(regionMap.keySet()));

        PagedModel<AccountModel> accountPage = traverson.fromRoot()
                .follow(withCurie(ACCOUNT_REL))
                .follow(withCurie(ACCOUNT_LIST_REL))
                .withTemplateParameters(parameters)
                .toObject(ACCOUNT_MODEL_PTR);

        outer_loop:
        for (; ; ) {
            console.warn(">> Page %s", Objects.requireNonNull(accountPage).getMetadata());

            console.info("%15s|%10s|%8s|%15s|%10s|%10s|%s | %s",
                    "Name",
                    "Desc",
                    "Type",
                    "Balance",
                    "Status",
                    "Weight",
                    "Self",
                    "Transactions");

            for (AccountModel accountModel : accountPage) {
                console.info("%15s|%10s|%8s|%15s|%10s|%10s|%s | %s",
                        accountModel.getName(),
                        accountModel.getDescription(),
                        accountModel.getAccountType(),
                        accountModel.getBalance(),
                        accountModel.getStatus(),
                        accountModel.getWeight(),
                        accountModel.getLink(IanaLinkRelations.SELF).orElse(Link.of("n/a")).getHref(),
                        accountModel.getLink(withCurie(BankLinkRelations.TRANSACTION_LIST_REL)).orElse(Link.of("n/a"))
                                .getHref());
            }

            StringBuilder sb = new StringBuilder("Continue to");
            accountPage.getPreviousLink().ifPresent(link -> sb.append(" P)rev"));
            accountPage.getNextLink().ifPresent(link -> sb.append(" N)ext"));
            accountPage.getLink(IanaLinkRelations.LAST).ifPresent(link -> sb.append(" F)irst"));
            accountPage.getLink(IanaLinkRelations.FIRST).ifPresent(link -> sb.append(" L)ast"));
            sb.append(" page or C)ancel?");

            innner_loop:
            for (; ; ) {
                console.info(sb.toString());
                Scanner s = new Scanner(System.in);
                if (s.hasNext()) {
                    switch (s.next()) {
                        case "N":
                        case "n":
                            accountPage = traverson.follow(accountPage.getNextLink())
                                    .toObject(ACCOUNT_MODEL_PTR);
                            break innner_loop;
                        case "P":
                        case "p":
                            accountPage = traverson.follow(accountPage.getPreviousLink())
                                    .toObject(ACCOUNT_MODEL_PTR);
                            break innner_loop;
                        case "F":
                        case "f":
                            accountPage = traverson.follow(accountPage.getLink(IanaLinkRelations.FIRST))
                                    .toObject(ACCOUNT_MODEL_PTR);
                            break innner_loop;
                        case "L":
                        case "l":
                            accountPage = traverson.follow(accountPage.getLink(IanaLinkRelations.LAST))
                                    .toObject(ACCOUNT_MODEL_PTR);
                            break innner_loop;
                        case "C":
                        case "c":
                            console.info("Cancelling");
                            break outer_loop;
                        default:
                            console.info("What?");
                            break;
                    }
                }
            }
        }
    }
}
