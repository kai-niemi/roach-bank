package io.roach.bank.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.roach.bank.api.support.LocalDateDeserializer;
import io.roach.bank.api.support.LocalDateSerializer;
import io.roach.bank.api.support.Money;

import static io.roach.bank.api.LinkRelations.CURIE_PREFIX;
import static io.roach.bank.api.LinkRelations.TRANSFER_FORM_REL;

/**
 * Request form with a list of account forming a balanced multi-legged monetary transaction.
 * A transaction request must have at least two entries, called legs, a region code, a client
 * generated transaction reference for idempotency and a transaction type.
 * <p>
 * Each leg points to a single account by id and region, and includes an amount that is either
 * positive (credit) or negative (debit).
 * <p>
 * It is possible to have legs with different account regions and currencies, as long as the
 * total balance for entries with the same currency is zero.
 */
@Relation(value = CURIE_PREFIX + TRANSFER_FORM_REL)
@JsonPropertyOrder({"links"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionForm extends RepresentationModel<TransactionForm> {
    public static Builder builder() {
        return new Builder();
    }

    @NotNull
    private UUID uuid;

    @NotBlank
    private String city;

    @NotBlank
    private String transactionType;

    private boolean fake;

    @NotNull
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate bookingDate;

    @NotNull
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate transferDate;

    private final List<AccountItem> accountLegs = new ArrayList<>();

    protected TransactionForm() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getCity() {
        return city;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public List<AccountItem> getAccountLegs() {
        return Collections.unmodifiableList(accountLegs);
    }

    public boolean isFake() {
        return fake;
    }

    public static class Builder {
        private final TransactionForm instance = new TransactionForm();

        public Builder withUUID(UUID uuid) {
            this.instance.uuid = uuid;
            return this;
        }

        public Builder withCity(String city) {
            this.instance.city = city;
            return this;
        }

        public Builder withTransactionType(String transactionType) {
            this.instance.transactionType = transactionType;
            return this;
        }

        public Builder withBookingDate(LocalDate bookingDate) {
            this.instance.bookingDate = bookingDate;
            return this;
        }

        public Builder withTransferDate(LocalDate transferDate) {
            this.instance.transferDate = transferDate;
            return this;
        }

        public Builder withFakeFlag() {
            this.instance.fake = true;
            return this;
        }

        public AccountItemBuilder addLeg() {
            return new AccountItemBuilder(this, instance.accountLegs::add);
        }

        public TransactionForm build() {
            if (instance.accountLegs.size() < 2) {
                throw new IllegalStateException("At least 2 legs are required");
            }
            return instance;
        }
    }

    public static class AccountItemBuilder {
        private final AccountItem instance = new AccountItem();

        private final Builder parentBuilder;

        private final Consumer<AccountItem> callback;

        private AccountItemBuilder(Builder parentBuilder, Consumer<AccountItem> callback) {
            this.parentBuilder = parentBuilder;
            this.callback = callback;
        }

        public AccountItemBuilder withId(UUID id) {
            this.instance.id = id;
            return this;
        }

        public AccountItemBuilder withAmount(Money amount) {
            this.instance.amount = amount;
            return this;
        }

        public AccountItemBuilder withNote(String note) {
            this.instance.note = note;
            return this;
        }

        public Builder then() {
            if (instance.id == null) {
                throw new IllegalStateException("id is required");
            }
            if (instance.amount == null) {
                throw new IllegalStateException("amount is required");
            }
            callback.accept(instance);
            return parentBuilder;
        }
    }

    public static class AccountItem {
        private UUID id;

        private Money amount;

        private String note;

        public UUID getId() {
            return id;
        }

        public Money getAmount() {
            return amount;
        }

        public String getNote() {
            return note;
        }
    }
}
