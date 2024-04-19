package io.roach.bank.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.TableModel;

import io.roach.bank.api.AccountModel;
import io.roach.bank.client.support.HypermediaClient;
import io.roach.bank.client.support.TableUtils;

import static io.roach.bank.api.LinkRelations.ACCOUNT_LIST_REL;
import static io.roach.bank.api.LinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.LinkRelations.withCurie;
import static io.roach.bank.client.Constants.ACCOUNT_PAGE_MODEL_PTR;

@ShellComponent
@ShellCommandGroup(Constants.REPORTING_COMMANDS)
public class ListAccounts extends AbstractCommand {
    @Autowired
    private HypermediaClient bankClient;

    public static String printContentTable(List<AccountModel> pageContent) {
        return TableUtils.prettyPrint(
                new TableModel() {
                    @Override
                    public int getRowCount() {
                        return pageContent.size() + 1;
                    }

                    @Override
                    public int getColumnCount() {
                        return 7;
                    }

                    @Override
                    public Object getValue(int row, int column) {
                        if (row == 0) {
                            switch (column) {
                                case 0 -> {
                                    return "#";
                                }
                                case 1 -> {
                                    return "Name";
                                }
                                case 2 -> {
                                    return "City";
                                }
                                case 3 -> {
                                    return "Type";
                                }
                                case 4 -> {
                                    return "Balance";
                                }
                                case 5 -> {
                                    return "Status";
                                }
                                case 6 -> {
                                    return "Link";
                                }
                            }
                            return "??";
                        }

                        AccountModel account = pageContent.get(row - 1);
                        switch (column) {
                            case 0 -> {
                                return row;
                            }
                            case 1 -> {
                                return account.getName();
                            }
                            case 2 -> {
                                return account.getCity();
                            }
                            case 3 -> {
                                return account.getAccountType();
                            }
                            case 4 -> {
                                return account.getBalance();
                            }
                            case 5 -> {
                                return account.getStatus();
                            }
                            case 6 -> {
                                return account.getRequiredLink(IanaLinkRelations.SELF).getHref();
                            }
                        }
                        return "??";
                    }
                });
    }

    @ShellMethod(value = "List accounts using pagination", key = {"list-accounts", "la"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listAccounts(
            @ShellOption(help = Constants.REGIONS_HELP,
                    defaultValue = Constants.DEFAULT_REGION,
                    valueProvider = RegionProvider.class) String region,
            @ShellOption(help = "page number", defaultValue = "0") int page,
            @ShellOption(help = "page size", defaultValue = "10") int pageSize) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", page);
        parameters.put("size", pageSize);
        parameters.put("region", region);

        PagedModel<AccountModel> accountPage = bankClient.fromRoot()
                .follow(withCurie(ACCOUNT_REL))
                .follow(withCurie(ACCOUNT_LIST_REL))
                .withTemplateParameters(parameters)
                .toObject(ACCOUNT_PAGE_MODEL_PTR);

        outer_loop:
        for (; ; ) {
            console.success(printContentTable(new ArrayList<>(accountPage.getContent())));

            StringBuilder sb = new StringBuilder("Continue to");

            accountPage.getPreviousLink().ifPresent(link -> sb.append(" P)rev"));
            accountPage.getNextLink().ifPresent(link -> sb.append(" N)ext"));
            accountPage.getLink(IanaLinkRelations.LAST).ifPresent(link -> sb.append(" F)irst"));
            accountPage.getLink(IanaLinkRelations.FIRST).ifPresent(link -> sb.append(" L)ast"));

            sb.append(" page or C)ancel?");

            innner_loop:
            for (; ; ) {
                PagedModel.PageMetadata metadata = accountPage.getMetadata();
                console.success("Page %d of %d with %d items total",
                        metadata.getNumber(),
                        metadata.getTotalPages(),
                        metadata.getTotalElements());
                console.info("%s", sb.toString());

                Scanner s = new Scanner(System.in);
                if (s.hasNext()) {
                    switch (s.next()) {
                        case "N":
                        case "n":
                            accountPage = bankClient.follow(accountPage.getNextLink())
                                    .toObject(ACCOUNT_PAGE_MODEL_PTR);
                            break innner_loop;
                        case "P":
                        case "p":
                            accountPage = bankClient.follow(accountPage.getPreviousLink())
                                    .toObject(ACCOUNT_PAGE_MODEL_PTR);
                            break innner_loop;
                        case "F":
                        case "f":
                            accountPage = bankClient.follow(accountPage.getLink(IanaLinkRelations.FIRST))
                                    .toObject(ACCOUNT_PAGE_MODEL_PTR);
                            break innner_loop;
                        case "L":
                        case "l":
                            accountPage = bankClient.follow(accountPage.getLink(IanaLinkRelations.LAST))
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
