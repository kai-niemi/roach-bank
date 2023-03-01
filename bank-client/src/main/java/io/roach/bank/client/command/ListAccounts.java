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
import io.roach.bank.client.command.support.RestCommands;

import static io.roach.bank.api.LinkRelations.ACCOUNT_LIST_REL;
import static io.roach.bank.api.LinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.LinkRelations.withCurie;
import static io.roach.bank.client.command.Constants.ACCOUNT_PAGE_MODEL_PTR;

@ShellComponent
@ShellCommandGroup(Constants.METADATA_COMMANDS)
public class ListAccounts extends AbstractCommand {
    @Autowired
    private RestCommands restCommands;

    @ShellMethod(value = "List accounts using pagination", key = {"list-accounts"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listAccounts(@ShellOption(help = "page number", defaultValue = "0") int page,
                             @ShellOption(help = "page size", defaultValue = "20") int pageSize) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", page);
        parameters.put("size", pageSize);

        PagedModel<AccountModel> accountPage = restCommands.fromRoot()
                .follow(withCurie(ACCOUNT_REL))
                .follow(withCurie(ACCOUNT_LIST_REL))
                .withTemplateParameters(parameters)
                .toObject(ACCOUNT_PAGE_MODEL_PTR);

        outer_loop:
        for (; ; ) {
            console.infof(">> Page %s", Objects.requireNonNull(accountPage).getMetadata());

            console.successf("%15s|%10s|%8s|%15s|%10s|%10s| %s",
                    "Name",
                    "Desc",
                    "Type",
                    "Balance",
                    "Status",
                    "Weight",
                    "Link");

            for (AccountModel accountModel : accountPage) {
                console.successf("%15s|%10s|%8s|%15s|%10s|%10s| %s",
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
                console.infof("%s", sb.toString());
                Scanner s = new Scanner(System.in);
                if (s.hasNext()) {
                    switch (s.next()) {
                        case "N":
                        case "n":
                            accountPage = restCommands.follow(accountPage.getNextLink())
                                    .toObject(ACCOUNT_PAGE_MODEL_PTR);
                            break innner_loop;
                        case "P":
                        case "p":
                            accountPage = restCommands.follow(accountPage.getPreviousLink())
                                    .toObject(ACCOUNT_PAGE_MODEL_PTR);
                            break innner_loop;
                        case "F":
                        case "f":
                            accountPage = restCommands.follow(accountPage.getLink(IanaLinkRelations.FIRST))
                                    .toObject(ACCOUNT_PAGE_MODEL_PTR);
                            break innner_loop;
                        case "L":
                        case "l":
                            accountPage = restCommands.follow(accountPage.getLink(IanaLinkRelations.LAST))
                                    .toObject(ACCOUNT_PAGE_MODEL_PTR);
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
