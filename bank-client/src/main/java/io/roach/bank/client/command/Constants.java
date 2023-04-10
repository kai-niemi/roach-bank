package io.roach.bank.client.command;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;

import io.roach.bank.api.AccountModel;

public abstract class Constants {
    public static final String ADMIN_COMMANDS = "1. Admin Commands";

    public static final String POOL_COMMANDS = "2. Resource Pool";

    public static final String CONFIG_COMMANDS = "3. Configuration Commands";

    public static final String REPORTING_COMMANDS = "4. Reporting Commands";

    public static final String WORKLOAD_COMMANDS = "5. Workload Commands";

    public static final String DEFAULT_DURATION = "180m";

    public static final String EMPTY = "";

    public static final String CONNECTED_CHECK = "connectedCheck";

    public static final String DURATION_HELP = "execution duration";

    public static final String REGIONS_HELP = "comma separated list of regions (gateway if empty)";

    public static final String ACCOUNT_LIMIT_HELP = "number of accounts per city (-1 means server default)";

    public static final String DEFAULT_ACCOUNT_LIMIT = "-1";

    public static final ParameterizedTypeReference<CollectionModel<AccountModel>> ACCOUNT_MODEL_PTR
            = new ParameterizedTypeReference<>() {
    };

    public static final ParameterizedTypeReference<PagedModel<AccountModel>> ACCOUNT_PAGE_MODEL_PTR
            = new ParameterizedTypeReference<>() {
    };

    private Constants() {
    }
}
