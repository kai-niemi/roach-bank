package io.roach.bank.api;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.roach.bank.api.support.Money;
import io.roach.bank.api.support.WeightedItem;

import static io.roach.bank.api.LinkRelations.CURIE_PREFIX;

@Relation(value = CURIE_PREFIX + LinkRelations.ACCOUNT_REL,
        collectionRelation = CURIE_PREFIX + LinkRelations.ACCOUNT_LIST_REL)
@JsonPropertyOrder({"links"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountModel extends RepresentationModel<AccountModel> implements WeightedItem {
    private UUID id;

    private String city;

    private String name;

    private String description;

    private AccountType accountType;

    private String currencyCode;

    //    @JsonSerialize(using = MoneySerializer.class)
//    @JsonDeserialize(using = MoneyDeserializer.class)
    private Money balance;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updated;

    private AccountStatus status;

    private boolean allowNegativeBalance;

    @JsonProperty("transactions") // For zoom request
    private CollectionModel<TransactionModel> transactions;

    public AccountModel() {
    }

    @Override
    @JsonIgnore
    public double getWeight() {
        return Math.max(0, balance.getAmount().doubleValue());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Money getBalance() {
        return balance;
    }

    public void setBalance(Money balance) {
        this.balance = balance;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public boolean isAllowNegativeBalance() {
        return allowNegativeBalance;
    }

    public void setAllowNegativeBalance(boolean allowNegativeBalance) {
        this.allowNegativeBalance = allowNegativeBalance;
    }

    public CollectionModel<TransactionModel> getTransactions() {
        return transactions;
    }

    public void setTransactions(CollectionModel<TransactionModel> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "AccountResource{" +
//                "uuid=" + uuid +
                ", region=" + city +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", accountType=" + accountType +
                ", currencyCode='" + currencyCode + '\'' +
                ", balance=" + balance +
                ", updated=" + updated +
                ", status=" + status +
                ", allowNegativeBalance=" + allowNegativeBalance +
                '}';
    }
}
