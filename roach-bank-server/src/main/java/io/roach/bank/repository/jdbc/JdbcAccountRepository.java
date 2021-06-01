package io.roach.bank.repository.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.roach.bank.ProfileNames;
import io.roach.bank.annotation.TransactionControlService;
import io.roach.bank.annotation.TransactionNotAllowed;
import io.roach.bank.api.AccountType;
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.service.NoSuchAccountException;
import io.roach.bank.repository.AccountRepository;

@Repository
@TransactionControlService
@Profile(ProfileNames.NOT_JPA)
public class JdbcAccountRepository implements AccountRepository {
    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Account createAccount(Account account) {
        jdbcTemplate.update("INSERT INTO account "
                        + "(id, region, balance, currency, name, description, type, closed, allow_negative, updated) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?)",
                account.getUUID(),
                account.getRegion(),
                account.getBalance().getAmount(),
                account.getBalance().getCurrency().getCurrencyCode(),
                account.getName(),
                account.getDescription(),
                account.getAccountType().getCode(),
                account.isClosed(),
                account.getAllowNegative(),
                account.getUpdated()
        );
        return account;
    }

    @Override
    public void createAccountBatch(String region, Currency currency, NamingStrategy namingStrategy, int batchSize) {
        final Money initialBalance = Money.of("0.00", currency);

        jdbcTemplate.batchUpdate(
                "INSERT INTO account "
                        + "(id, region, balance, currency, name, description, type, closed, allow_negative, updated) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() { // RETURNING NOTHING
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, UUID.randomUUID());
                        ps.setString(2, region);
                        ps.setBigDecimal(3, initialBalance.getAmount());
                        ps.setString(4, initialBalance.getCurrency().getCurrencyCode());
                        ps.setString(5, namingStrategy.accountName(i));
                        ps.setString(6, CockroachFacts.nextFact(256));
                        ps.setString(7, AccountType.ASSET.getCode());
                        ps.setBoolean(8, false);
                        ps.setInt(9, 0);
                        ps.setTimestamp(10, Timestamp.from(Instant.now()));
                    }

                    @Override
                    public int getBatchSize() {
                        return batchSize;
                    }
                });
    }

    @Override
    public void updateBalances(List<Account> accounts) {
        int[] rowsAffected = jdbcTemplate.batchUpdate(
                "UPDATE account "
                        + "SET "
                        + "   balance = ?,"
                        + "   updated=? "
                        + "WHERE id = ? "
                        + "   AND region=? "
                        + "   AND closed=false "
                        + "   AND currency=? "
                        + "   AND (?) * abs(allow_negative-1) >= 0", // RETURNING NOTHING
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Account account = accounts.get(i);

                        ps.setBigDecimal(1, account.getBalance().getAmount());
                        ps.setObject(2, LocalDateTime.now());
                        ps.setObject(3, account.getUUID());
                        ps.setString(4, account.getRegion());
                        ps.setString(5, account.getBalance().getCurrency().getCurrencyCode());
                        ps.setBigDecimal(6, account.getBalance().getAmount());
                    }

                    @Override
                    public int getBatchSize() {
                        return accounts.size();
                    }
                });

        // Trust but verify
        Arrays.stream(rowsAffected).filter(i -> i != 1).forEach(i -> {
            throw new IncorrectResultSizeDataAccessException(1, i);
        });
    }

    @Override
    public void closeAccount(Account.Id id) {
        int rowsAffected = jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE account "
                                    + "SET closed=true "
                                    + "WHERE id=?");
                    ps.setObject(1, id.getUUID());
                    return ps;
                });
        if (rowsAffected != 1) {
            throw new IncorrectResultSizeDataAccessException(1, rowsAffected);
        }
    }

    @Override
    public void openAccount(Account.Id id) {
        int rowsAffected = jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE account "
                                    + "SET closed=false "
                                    + "WHERE id=?");
                    ps.setObject(1, id.getUUID());
                    return ps;
                });
        if (rowsAffected != 1) {
            throw new IncorrectResultSizeDataAccessException(1, rowsAffected);
        }
    }

    @Override
    public Page<Account> findAccountPage(Set<String> regions, Pageable pageable) {
        String sql =
                "SELECT * "
                        + "FROM account "
                        + "WHERE region IN (:regions) "
                        + "ORDER BY id, region "
                        + "LIMIT :limit OFFSET :offset ";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("regions", regions)
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        List<Account> accounts = this.namedParameterJdbcTemplate
                .query(sql, parameters, (rs, rowNum) -> readAccount(rs));

        return new PageImpl<>(accounts, pageable, countAll(parameters));
    }

    private Long countAll(MapSqlParameterSource params) {
        return this.namedParameterJdbcTemplate.queryForObject(
                "SELECT count(id) FROM account WHERE region IN (:regions)",
                params, Long.class);
    }

    @Override
    public List<Account> findAccountsForUpdate(Set<Account.Id> ids) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        parameters.addValue("ids",
                ids.stream().map(Account.Id::getUUID).collect(Collectors.toSet()));
        parameters.addValue("regions",
                ids.stream().map(Account.Id::getRegion).collect(Collectors.toSet()));

        return this.namedParameterJdbcTemplate.query(
                "SELECT * FROM account WHERE id in (:ids) AND region in (:regions) FOR UPDATE",
                parameters,
                (rs, rowNum) -> readAccount(rs));
    }

    @Override
    public List<Account> findAccountsByRegion(String region, int limit) {
        return this.namedParameterJdbcTemplate.query(
                "SELECT * FROM account WHERE region=:region "
                        + "AND name LIKE 'user:%' "
                        + "ORDER BY name "
                        + "LIMIT (:limit)",
                new MapSqlParameterSource()
                        .addValue("region", region)
                        .addValue("limit", limit),
                (rs, rowNum) -> readAccount(rs));
    }

    @Override
    public Account getAccountById(Account.Id id) {
        try {
            return this.jdbcTemplate.queryForObject(
                    "SELECT * "
                            + "FROM account "
                            + "WHERE id=? AND region=?",
                    (rs, rowNum) -> readAccount(rs),
                    id.getUUID(), id.getRegion()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchAccountException(id.toString());
        }
    }

    @Override
    public Money getBalance(Account.Id id) {
        return this.jdbcTemplate.queryForObject(
                "SELECT balance,currency "
                        + "FROM account "
                        + "WHERE id=? AND region=?",
                (rs, rowNum) -> Money.of(rs.getString(1), rs.getString(2)),
                id.getUUID(), id.getRegion()
        );
    }

    @Override
    @TransactionNotAllowed
    public Money getBalanceSnapshot(Account.Id id) {
        return this.jdbcTemplate.queryForObject(
                "SELECT balance,currency "
                        + "FROM account "
                        + "AS OF SYSTEM TIME experimental_follower_read_timestamp() "
                        + "WHERE id=? AND region=?",
                (rs, rowNum) -> Money.of(rs.getString(1), rs.getString(2)),
                id.getUUID(), id.getRegion()
        );
    }

    private Account readAccount(ResultSet rs) throws SQLException {
        return Account.builder()
                .withId((UUID) rs.getObject("id"), rs.getString("region"))
                .withName(rs.getString("name"))
                .withBalance(Money.of(rs.getString("balance"), rs.getString("currency")))
                .withAccountType(AccountType.of(rs.getString("type")))
                .withDescription(rs.getString("description"))
                .withClosed(rs.getBoolean("closed"))
                .withAllowNegative(rs.getInt("allow_negative") > 0)
                .withUpdated(rs.getTimestamp("updated").toLocalDateTime())
                .build();
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.execute("TRUNCATE TABLE account");
    }
}
