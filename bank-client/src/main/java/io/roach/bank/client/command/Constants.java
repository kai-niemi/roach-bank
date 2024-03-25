package io.roach.bank.client.command;

import io.roach.bank.api.AccountModel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;

public abstract class Constants {
    public static final String ADMIN_COMMANDS = "1. Admin Commands";

    public static final String POOL_COMMANDS = "2. Resource Pool";

    public static final String CONFIG_COMMANDS = "3. Region Commands";

    public static final String REPORTING_COMMANDS = "4. Reporting Commands";

    public static final String WORKLOAD_COMMANDS = "5. Workload Commands";

    public static final String DEFAULT_DURATION = "180m";

    public static final String CONNECTED_CHECK = "connectedCheck";

    public static final String DURATION_HELP = "Execution duration";

    public static final String REGIONS_HELP = "Name of account regions."
            + "\n'gateway' refers to CRDB gateway nodee region."
            + "\n'all' includes all regions.";

    public static final String ACCOUNT_LIMIT_HELP = "Number of accounts per city (-1 server default)";

    public static final String DEFAULT_ACCOUNT_LIMIT = "-1";

    public static final String DEFAULT_REGION = "gateway";

    public static final ParameterizedTypeReference<CollectionModel<AccountModel>> ACCOUNT_MODEL_PTR
            = new ParameterizedTypeReference<>() {
    };

    public static final ParameterizedTypeReference<PagedModel<AccountModel>> ACCOUNT_PAGE_MODEL_PTR
            = new ParameterizedTypeReference<>() {
    };

    private Constants() {
    }
}
