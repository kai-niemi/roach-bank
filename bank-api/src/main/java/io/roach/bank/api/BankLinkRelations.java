package io.roach.bank.api;

/**
 * Defines domain specific web constants such as link relation names and resource names.
 */
public abstract class BankLinkRelations {
    // Transaction history rels
    public static final String TRANSACTION_REL = "transaction";

    public static final String TRANSACTION_LIST_REL = "transaction-list";

    public static final String TRANSACTION_ITEM_REL = "transaction-item";

    public static final String TRANSACTION_ITEMS_REL = "transaction-item-list";

    public static final String TRANSACTION_FORM_REL = "transaction-form";

    // Account rels
    public static final String ACCOUNT_REL = "account";

    public static final String ACCOUNT_LIST_REL = "account-list";

    public static final String ACCOUNT_TOP = "account-top";

    public static final String ACCOUNT_FORM_REL = "account-form";

    public static final String ACCOUNT_BATCH_REL = "account-batch";

    public static final String ACCOUNT_BALANCE_REL = "account-balance";

    public static final String ACCOUNT_BALANCE_SNAPSHOT_REL = "account-balance-snapshot";

    // Reporting link relations

    public static final String REPORTING_REL = "reporting";

    public static final String ACCOUNT_SUMMARY_REL = "account-summary";

    public static final String TRANSACTION_SUMMARY_REL = "transaction-summary";

    // Meta


    public static final String REGION_CITIES_REL = "region-cities";

    public static final String CURRENCIES_REL = "currencies";

    public static final String CITIES_REL = "cities";

    public static final String CITY_CURRENCY_REL = "cities-currency";

    public static final String LOCAL_CITIES_REL = "cities-local";

    public static final String REGIONS_REL = "regions";


    // Generic context-scoped link relations

    public static final String OPEN_REL = "open";

    public static final String CLOSE_REL = "close";

    // Admin

    public static final String ADMIN_REL = "admin";

    public static final String POOL_SIZE_REL = "pool-size";

    public static final String META_REL = "meta";

    public static final String ACTUATOR_REL = "actuator";

    // IANA standard link relations:
    // http://www.iana.org/assignments/link-relations/link-relations.xhtml

    public static final String CURIE_NAMESPACE = "roachbank";

    public static final String CURIE_PREFIX = CURIE_NAMESPACE + ":";

    private BankLinkRelations() {
    }

    public static String withCurie(String rel) {
        return CURIE_NAMESPACE + ":" + rel;
    }
}
