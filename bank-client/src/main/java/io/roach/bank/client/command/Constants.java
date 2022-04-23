package io.roach.bank.client.command;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedModel;

import io.roach.bank.api.AccountModel;

public abstract class Constants {
    public static final String ADMIN_COMMANDS = "1. Admin Commands";

    public static final String CONFIG_COMMANDS = "2. Config Commands";

    public static final String WORKLOAD_COMMANDS = "3. Workload Commands";

    public static final String API_REPORTING_COMMANDS = "4. Reporting Commands";

    public static final String API_MAIN_COMMANDS = "5. Main Commands";

    public static final String DEFAULT_DURATION = "60m";

    public static final String EMPTY = "";

    public static final String CONNECTED_CHECK = "connectedCheck";

    public static final String DURATION_HELP = "execution duration (expression)";

    public static final String REGIONS_HELP = "comma separated list of regions";

    public static final String CITIES_HELP = "comma separated list of cities";

    public static final String ACCOUNT_LIMIT_HELP = "max number of accounts per region to query (negative value means server default)";

    public static final String DEFAULT_ACCOUNT_LIMIT = "-1";

    public static final ParameterizedTypeReference<PagedModel<AccountModel>> ACCOUNT_MODEL_PTR
            = new ParameterizedTypeReference<PagedModel<AccountModel>>() {
    };

    private Constants() {
    }
}
