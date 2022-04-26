package io.roach.bank.repository.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.roach.bank.ProfileNames;
import io.roach.bank.annotation.TransactionMandatory;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.repository.TransactionRepository;

@Repository
@TransactionMandatory
@Profile(ProfileNames.JDBC)
public class JdbcTransactionRepository implements TransactionRepository {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        final LocalDate bookingDate = transaction.getBookingDate();
        final LocalDate transferDate = transaction.getTransferDate();

        jdbcTemplate.update("INSERT INTO transaction "
                        + "(id,city,booking_date,transfer_date,transaction_type) "
                        + "VALUES(?,?,?,?,?::transaction_type)",
                transaction.getId(),
                transaction.getCity(),
                bookingDate != null ? bookingDate : LocalDate.now(),
                transferDate != null ? transferDate : LocalDate.now(),
                transaction.getTransactionType()
        );

        final List<TransactionItem> items = transaction.getItems();

        jdbcTemplate.batchUpdate(
                "INSERT INTO transaction_item "
                        + "(transaction_id, transaction_city, account_id, amount, currency, note, running_balance) "
                        + "VALUES(?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TransactionItem item = items.get(i);
                        int idx = 1;
                        ps.setObject(idx++, item.getId().getTransactionId());
                        ps.setString(idx++, item.getTransactionCity());
                        ps.setObject(idx++, item.getId().getAccountId());
                        ps.setBigDecimal(idx++, item.getAmount().getAmount());
                        ps.setString(idx++, item.getAmount().getCurrency().getCurrencyCode());
                        ps.setString(idx++, item.getNote());
                        ps.setBigDecimal(idx++, item.getRunningBalance().getAmount());
                    }

                    @Override
                    public int getBatchSize() {
                        return items.size();
                    }
                });

        return transaction;
    }

    @Override
    public Transaction findTransactionById(UUID id) {
        List<Transaction> list = this.jdbcTemplate.query(
                "SELECT * FROM transaction WHERE id=?",
                (rs, rowNum) -> mapToTransaction(rs),
                id);
        return DataAccessUtils.singleResult(list);
    }

    private Transaction mapToTransaction(ResultSet rs) throws SQLException {
        UUID transactionId = (UUID) rs.getObject("id");
        String city = rs.getString("city");
        String transactionType = rs.getString("transaction_type");
        LocalDate bookingDate = rs.getDate("booking_date").toLocalDate();
        LocalDate transferDate = rs.getDate("transfer_date").toLocalDate();
        return Transaction.builder()
                .withId(transactionId)
                .withCity(city)
                .withTransactionType(transactionType)
                .withBookingDate(bookingDate)
                .withTransferDate(transferDate)
                .build();
    }

    @Override
    public Page<Transaction> findTransactions(Pageable pageable) {
        int count = countAllTransactions();
        List<Transaction> content = this.jdbcTemplate.query(
                "SELECT * FROM transaction ORDER BY transfer_date LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapToTransaction(rs),
                pageable.getPageSize(), pageable.getOffset());
        return new PageImpl<>(content, pageable, count);
    }

    private Integer countAllTransactions() {
        List<Integer> results = this.jdbcTemplate.query(
                "SELECT count(id) FROM transaction",
                (rs, rowNum) -> rs.getInt(1));
        return DataAccessUtils.singleResult(results);
    }

    @Override
    public Page<TransactionItem> findTransactionItems(UUID transactionId, Pageable pageable) {
        long count = countItemsByTransactionId(transactionId);

        List<TransactionItem> content = this.jdbcTemplate.query(
                "SELECT * FROM transaction_item WHERE transaction_id=?",
                (rs, rowNum) -> readTransactionItem(rs),
                transactionId
        );

        return new PageImpl<>(content, pageable, count);
    }

    private Long countItemsByTransactionId(UUID id) {
        List<Long> results =
                this.jdbcTemplate.query(
                        "SELECT count(transaction_id) FROM transaction_item WHERE transaction_id=?",
                        (rs, rowNum) -> rs.getLong(1),
                        id
                );
        return DataAccessUtils.singleResult(results);
    }

    @Override
    public TransactionItem getTransactionItemById(TransactionItem.Id id) {
        List<TransactionItem> list = this.jdbcTemplate.query(
                "SELECT * FROM transaction_item WHERE transaction_id=? "
                        + "AND account_id=? ",
                (rs, rowNum) -> readTransactionItem(rs),
                id.getTransactionId(), id.getAccountId()
        );
        return DataAccessUtils.requiredSingleResult(list);
    }

    private TransactionItem readTransactionItem(ResultSet rs) throws SQLException {
        UUID accountId = (UUID) rs.getObject("account_id");
        UUID transactionId = (UUID) rs.getObject("transaction_id");
        String transactionCity = rs.getString("transaction_city");
        Money runningBalance = Money.of(rs.getString("running_balance"), rs.getString("currency"));
        Money amount = Money.of(rs.getString("amount"), rs.getString("currency"));
        String note = rs.getString("note");

        return Transaction.builder()
                .withId(transactionId)
                .withCity(transactionCity)
                .andItem()
                .withAccount(Account.builder().withId(accountId).build())
                .withRunningBalance(runningBalance)
                .withAmount(amount)
                .withNote(note)
                .then()
                .build()
                .getItems()
                .get(0);
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.execute("TRUNCATE TABLE transaction_item");
        jdbcTemplate.execute("TRUNCATE TABLE transaction");
    }
}
