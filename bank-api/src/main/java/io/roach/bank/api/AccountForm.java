package io.roach.bank.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.roach.bank.api.support.EnumPattern;

import static io.roach.bank.api.LinkRelations.ACCOUNT_FORM_REL;
import static io.roach.bank.api.LinkRelations.CURIE_PREFIX;

@Relation(value = CURIE_PREFIX + ACCOUNT_FORM_REL)
@JsonPropertyOrder({"links"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountForm extends RepresentationModel<AccountForm> {
    @NotNull
    private String uuid = "auto";

    @NotNull
    @Size(min = 2)
    private String city;

    @NotBlank
    private String name;

    @NotNull
    @Size(min = 2)
    private String currencyCode;

    private String description;

    @NotNull
    @EnumPattern(regexp = "EXPENSE|REVENUE|LIABILITY|ASSET", message = "invalid account type")
    private AccountType accountType;

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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
}
