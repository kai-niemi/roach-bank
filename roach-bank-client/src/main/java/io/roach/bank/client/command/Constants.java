package io.roach.bank.client.command;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedModel;

import io.roach.bank.api.AccountModel;

public abstract class Constants {
    public static final String API_MAIN_COMMANDS = "Main Commands";

    public static final String API_REPORTING_COMMANDS = "Reporting Commands";

    public static final String ADMIN_COMMANDS = "Admin Commands";

    public static final String DEFAULT_DURATION = "60m";

    public static final String EMPTY = "";

    public static final String CONNECTED_CHECK = "connectedCheck";

    public static final String DURATION_HELP = "execution duration (expression)";

    public static final String REGIONS_HELP = "comma separated list of regions or region groups";

    public static final String CONC_HELP = "number of concurrent workers per region";

    public static final String ACCOUNT_LIMIT_HELP = "max number of accounts per region to query (negative value means server default)";

    public static final String DEFAULT_ACCOUNT_LIMIT = "-1";

    public static final ParameterizedTypeReference<PagedModel<AccountModel>> ACCOUNT_MODEL_PTR
            = new ParameterizedTypeReference<PagedModel<AccountModel>>() {
    };

    private Constants() {
    }
}
