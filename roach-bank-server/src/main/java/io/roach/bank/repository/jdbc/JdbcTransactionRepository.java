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
import io.roach.bank.annotation.TransactionControlService;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.repository.TransactionRepository;

@Repository
@TransactionControlService
@Profile(ProfileNames.NOT_JPA)
public class JdbcTransactionRepository implements TransactionRepository {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Transaction create(Transaction transaction) {
        final LocalDate bookingDate = transaction.getBookingDate();
        final LocalDate transferDate = transaction.getTransferDate();

        jdbcTemplate.update("INSERT INTO transaction "
                        + "(id,region,booking_date,transfer_date,transaction_type) "
                        + "VALUES(?, ?, ?, ?, ?)",
                transaction.getUUID(),
                transaction.getRegion(),
                bookingDate != null ? bookingDate : LocalDate.now(),
                transferDate != null ? transferDate : LocalDate.now(),
                transaction.getTransactionType()
        );

        final List<TransactionItem> items = transaction.getItems();

        jdbcTemplate.batchUpdate(
                "INSERT INTO transaction_item "
                        + "(transaction_region, transaction_id, account_region, account_id, amount, currency, note, running_balance) "
                        + "VALUES(?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TransactionItem transactionLeg = items.get(i);
                        ps.setString(1, transactionLeg.getId().getTransactionRegion());
                        ps.setObject(2, transactionLeg.getId().getTransactionId());
                        ps.setString(3, transactionLeg.getId().getAccountRegion());
                        ps.setObject(4, transactionLeg.getId().getAccountId());
                        ps.setBigDecimal(5, transactionLeg.getAmount().getAmount());
                        ps.setString(6, transactionLeg.getAmount().getCurrency().getCurrencyCode());
                        ps.setString(7, transactionLeg.getNote());
                        ps.setBigDecimal(8, transactionLeg.getRunningBalance().getAmount());
                    }

                    @Override
                    public int getBatchSize() {
                        return items.size();
                    }
                });

        return transaction;
    }

    @Override
    public Transaction findById(Transaction.Id id) {
        List<Transaction> list = this.jdbcTemplate.query(
                "SELECT * FROM transaction WHERE region=? and id=?",
                (rs, rowNum) -> mapToTransaction(rs),
                id.getRegion(), id.getUUID());
        return DataAccessUtils.singleResult(list);
    }

    private Transaction mapToTransaction(ResultSet rs) throws SQLException {
        UUID transactionId = (UUID) rs.getObject("id");
        String region = rs.getString("region");
        String transactionType = rs.getString("transaction_type");
        LocalDate bookingDate = rs.getDate("booking_date").toLocalDate();
        LocalDate transferDate = rs.getDate("transfer_date").toLocalDate();
        return Transaction.builder()
                .withId(Transaction.Id.of(transactionId, region))
                .withTransactionType(transactionType)
                .withBookingDate(bookingDate)
                .withTransferDate(transferDate)
                .build();
    }

    @Override
    public Page<Transaction> findAll(Pageable pageable) {
        int count = countAllTransactions();
        List<Transaction> content = this.jdbcTemplate.query(
                "SELECT * FROM transaction ORDER BY transfer_date LIMIT ? OFFSET ?",
                new Object[] {pageable.getPageSize(), pageable.getOffset()},
                (rs, rowNum) -> mapToTransaction(rs));
        return new PageImpl<>(content, pageable, count);
    }

    private Integer countAllTransactions() {
        List<Integer> results = this.jdbcTemplate.query(
                "SELECT count(id) FROM transaction",
                (rs, rowNum) -> rs.getInt(1));
        return DataAccessUtils.singleResult(results);
    }

    @Override
    public Page<TransactionItem> findItems(Transaction.Id transactionId, Pageable pageable) {
        long count = countItemsByTransactionId(transactionId);

        List<TransactionItem> content = this.jdbcTemplate.query(
                "SELECT * FROM transaction_item WHERE transaction_id=? and transaction_region=?",
                (rs, rowNum) -> readTransactionItem(rs),
                transactionId.getUUID(), transactionId.getRegion()
        );

        return new PageImpl<>(content, pageable, count);
    }

    private Long countItemsByTransactionId(Transaction.Id id) {
        List<Long> results =
                this.jdbcTemplate.query(
                        "SELECT count(transaction_id) FROM transaction_item WHERE transaction_id=? and transaction_region=?",
                        (rs, rowNum) -> rs.getLong(1),
                        id.getUUID(), id.getRegion()
                );
        return DataAccessUtils.singleResult(results);
    }

    @Override
    public TransactionItem getItemById(TransactionItem.Id id) {
        List<TransactionItem> list = this.jdbcTemplate.query(
                "SELECT * FROM transaction_item WHERE transaction_id=? "
                        + "AND transaction_region=? "
                        + "AND account_id=? "
                        + "AND account_region=?",
                new Object[] {
                        id.getTransactionId(),
                        id.getTransactionRegion(),
                        id.getAccountId(),
                        id.getAccountRegion()
                },
                (rs, rowNum) -> readTransactionItem(rs));
        return DataAccessUtils.requiredSingleResult(list);
    }

    private TransactionItem readTransactionItem(ResultSet rs) throws SQLException {
        UUID accountId = (UUID) rs.getObject("account_id");
        UUID transactionId = (UUID) rs.getObject("transaction_id");
        String transactionRegion = rs.getString("transaction_region");
        String accountRegion = rs.getString("account_region");
        Money runningBalance = Money.of(rs.getString("running_balance"), rs.getString("currency"));
        Money amount = Money.of(rs.getString("amount"), rs.getString("currency"));
        String note = rs.getString("note");

        return Transaction.builder()
                .withId(Transaction.Id.of(transactionId, transactionRegion))
                .andItem()
                .withAccount(Account.builder().withId(accountId, accountRegion).build())
                .withRunningBalance(runningBalance)
                .withAmount(amount)
                .withNote(note)
                .then()
                .build()
                .getItems()
                .get(0);
    }
}
