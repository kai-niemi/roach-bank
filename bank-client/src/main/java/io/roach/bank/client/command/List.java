package io.roach.bank.client.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.roach.bank.api.AccountModel;
import io.roach.bank.client.support.Console;

import static io.roach.bank.api.BankLinkRelations.ACCOUNT_LIST_REL;
import static io.roach.bank.api.BankLinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.BankLinkRelations.withCurie;
import static io.roach.bank.client.command.Constants.ACCOUNT_MODEL_PTR;

@ShellComponent
@ShellCommandGroup(Constants.API_MAIN_COMMANDS)
public class List extends RestCommandSupport {
    @Autowired
    private Console console;

    @ShellMethod(value = "List accounts using pagination", key = {"list", "l"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listAccounts(@ShellOption(help = "page number", defaultValue = "0") int page,
                             @ShellOption(help = "page size", defaultValue = "20") int pageSize,
                             @ShellOption(help = Constants.CITIES_HELP, defaultValue = Constants.EMPTY)
                                     String cities) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", page);
        parameters.put("size", pageSize);
        parameters.put("cities", cities);

        PagedModel<AccountModel> accountPage = traverson.fromRoot()
                .follow(withCurie(ACCOUNT_REL))
                .follow(withCurie(ACCOUNT_LIST_REL))
                .withTemplateParameters(parameters)
                .toObject(ACCOUNT_MODEL_PTR);

        outer_loop:
        for (; ; ) {
            console.cyan(">> Page %s\n", Objects.requireNonNull(accountPage).getMetadata());

            console.yellow("%15s|%10s|%8s|%15s|%10s|%10s| %s\n",
                    "Name",
                    "Desc",
                    "Type",
                    "Balance",
                    "Status",
                    "Weight",
                    "Link");

            for (AccountModel accountModel : accountPage) {
                console.yellow("%15s|%10s|%8s|%15s|%10s|%10s| %s\n",
                        accountModel.getName(),
                        accountModel.getDescription(),
                        accountModel.getAccountType(),
                        accountModel.getBalance(),
                        accountModel.getStatus(),
                        accountModel.getWeight(),
                        accountModel.getLink(IanaLinkRelations.SELF).orElse(Link.of("n/a")).getHref());
            }

            StringBuilder sb = new StringBuilder("Continue to");
            accountPage.getPreviousLink().ifPresent(link -> sb.append(" P)rev"));
            accountPage.getNextLink().ifPresent(link -> sb.append(" N)ext"));
            accountPage.getLink(IanaLinkRelations.LAST).ifPresent(link -> sb.append(" F)irst"));
            accountPage.getLink(IanaLinkRelations.FIRST).ifPresent(link -> sb.append(" L)ast"));
            sb.append(" page or C)ancel?");

            innner_loop:
            for (; ; ) {
                console.cyan("%s\n", sb.toString());
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
                            break outer_loop;
                        default:
                            break;
                    }
                }
            }
        }
    }
}