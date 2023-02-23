package io.roach.bank.repository.jdbc;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;
import io.roach.bank.api.AccountType;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
@Profile(ProfileNames.JDBC)
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
                        + "(id, city, balance, currency, name, description, type, closed, allow_negative, updated) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?)",
                account.getId(),
                account.getCity(),
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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createAccounts(Supplier<Account> factory, int numAccounts, int batchSize) {
        Assert.isTrue(!TransactionSynchronizationManager.isActualTransactionActive(), "Expected no transaction");

        // Implict transactions
        IntStream.rangeClosed(1, numAccounts / batchSize)
                .forEach(batch -> jdbcTemplate.batchUpdate(
                        "INSERT INTO account "
                                + "(city, balance, currency, name, description, type, closed, allow_negative) "
                                + "VALUES(?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                Account account = factory.get();
                                int idx = 1;
                                ps.setString(idx++, account.getCity());
                                ps.setBigDecimal(idx++, account.getBalance().getAmount());
                                ps.setString(idx++, account.getBalance().getCurrency().getCurrencyCode());
                                ps.setString(idx++, account.getName());
                                ps.setString(idx++, account.getDescription());
                                ps.setString(idx++, account.getAccountType().getCode());
                                ps.setBoolean(idx++, account.isClosed());
                                ps.setInt(idx++, account.getAllowNegative());
                            }

                            @Override
                            public int getBatchSize() {
                                return batchSize;
                            }
                        }));
    }

    @Override
    public void updateBalances(List<Pair<UUID, BigDecimal>> balanceUpdates) {
        int[] rowsAffected = jdbcTemplate.batchUpdate(
                "UPDATE account "
                        + "SET "
                        + "   balance = balance + ?,"
                        + "   updated = clock_timestamp() "
                        + "WHERE id = ? "
                        + "   AND closed=false "
                        + "   AND (balance + ?) * abs(allow_negative-1) >= 0",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Pair<UUID, BigDecimal> entry = balanceUpdates.get(i);
                        ps.setBigDecimal(1, entry.getSecond());
                        ps.setObject(2, entry.getFirst());
                        ps.setBigDecimal(3, entry.getSecond());
                    }

                    @Override
                    public int getBatchSize() {
                        return balanceUpdates.size();
                    }
                });

        // Check invariant on neg balance
        Arrays.stream(rowsAffected)
                .filter(i -> i != 1)
                .forEach(i -> {
                    throw new IncorrectResultSizeDataAccessException(1, i);
                });
    }

    @Override
    public void closeAccount(UUID id) {
        int rowsAffected = jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE account "
                                    + "SET closed=true "
                                    + "WHERE id=?");
                    ps.setObject(1, id);
                    return ps;
                });
        if (rowsAffected != 1) {
            throw new IncorrectResultSizeDataAccessException(1, rowsAffected);
        }
    }

    @Override
    public void openAccount(UUID id) {
        int rowsAffected = jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE account "
                                    + "SET closed=false "
                                    + "WHERE id=?");
                    ps.setObject(1, id);
                    return ps;
                });
        if (rowsAffected != 1) {
            throw new IncorrectResultSizeDataAccessException(1, rowsAffected);
        }
    }


    @Override
    public Account getAccountByReference(UUID id) {
        return Account.builder().withId(id).build();
    }

    @Override
    public Optional<Account> getAccountById(UUID id) {
        try {
            return Optional.ofNullable(this.jdbcTemplate.queryForObject(
                    "SELECT * FROM account WHERE id=?",
                    (rs, rowNum) -> readAccount(rs),
                    id
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Money getBalance(UUID id) {
        return this.jdbcTemplate.queryForObject(
                "SELECT balance,currency FROM account WHERE id=?",
                (rs, rowNum) -> Money.of(rs.getString(1), rs.getString(2)),
                id
        );
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Money getBalanceSnapshot(UUID id) {
        return this.jdbcTemplate.queryForObject(
                "SELECT balance,currency "
                        + "FROM account "
                        + "AS OF SYSTEM TIME follower_read_timestamp() "
                        + "WHERE id=?",
                (rs, rowNum) -> Money.of(rs.getString(1), rs.getString(2)),
                id
        );
    }

    private Account readAccount(ResultSet rs) throws SQLException {
        return Account.builder()
                .withId((UUID) rs.getObject("id"))
                .withCity(rs.getString("city"))
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

    @Override
    public Page<Account> findPageByCity(Set<String> cities, Pageable pageable) {
        String sql =
                "SELECT * "
                        + "FROM account "
                        + "WHERE city IN (:cities) "
                        + "ORDER BY id, city "
                        + "LIMIT :limit OFFSET :offset ";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("cities", cities)
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        List<Account> accounts = this.namedParameterJdbcTemplate
                .query(sql, parameters, (rs, rowNum) -> readAccount(rs));

        return new PageImpl<>(accounts, pageable, countAll(parameters));
    }

    private Long countAll(MapSqlParameterSource params) {
        return this.namedParameterJdbcTemplate.queryForObject(
                "SELECT count(id) FROM account WHERE city IN (:cities)",
                params, Long.class);
    }

    @Override
    public List<Account> findByCity(String city, int limit) {
        return this.namedParameterJdbcTemplate.query(
                "SELECT * FROM account WHERE city=:city "
                        + "ORDER BY id,city "
                        + "LIMIT (:limit)",
                new MapSqlParameterSource()
                        .addValue("city", city)
                        .addValue("limit", limit),
                (rs, rowNum) -> readAccount(rs));
    }

    @Override
    public List<Account> findByIDs(Set<UUID> ids, boolean locking) {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "Expected transaction");

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", ids);

        return this.namedParameterJdbcTemplate.query(
                "SELECT * FROM account WHERE id in (:ids)" + (locking ? " FOR UPDATE" : ""),
                parameters,
                (rs, rowNum) -> readAccount(rs));
    }
}
