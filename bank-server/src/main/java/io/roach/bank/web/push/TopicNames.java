package io.roach.bank.web.push;

public abstract class TopicNames {
    private TopicNames() {
    }

    public static final String TOPIC_ACCOUNT_SUMMARY = "/topic/account-summary";

    public static final String TOPIC_TRANSACTION_SUMMARY = "/topic/transaction-summary";

    public static final String TOPIC_REPORT_UPDATE = "/topic/report-update";

    public static final String TOPIC_ACCOUNT_UPDATE = "/topic/account-update";
}
