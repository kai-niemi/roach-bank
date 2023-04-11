package io.roach.bank.repository.jdbc;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

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
                        + "(id, city, balance, currency, name, description, type, closed, allow_negative) "
                        + "VALUES(?,?,?,?,?,?,?::account_type,?,?)",
                account.getId(),
                account.getCity(),
                account.getBalance().getAmount(),
                account.getBalance().getCurrency().getCurrencyCode(),
                account.getName(),
                account.getDescription(),
                account.getAccountType().getCode(),
                account.isClosed(),
                account.getAllowNegative()
        );
        return account;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<UUID> createAccounts(Supplier<Account> factory, int batchSize) {
        Assert.isTrue(!TransactionSynchronizationManager.isActualTransactionActive(), "Expected no transaction");
        List<UUID> ids = new ArrayList<>();
        // Implict transaction
        jdbcTemplate.batchUpdate(
                "INSERT INTO account "
                        + "(id,city, balance, currency, name, description, type, closed, allow_negative) "
                        + "VALUES(?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Account account = factory.get();
                        int idx = 1;
                        ps.setObject(idx++, account.getId());
                        ps.setString(idx++, account.getCity());
                        ps.setBigDecimal(idx++, account.getBalance().getAmount());
                        ps.setString(idx++, account.getBalance().getCurrency().getCurrencyCode());
                        ps.setString(idx++, account.getName());
                        ps.setString(idx++, account.getDescription());
                        ps.setString(idx++, account.getAccountType().getCode());
                        ps.setBoolean(idx++, account.isClosed());
                        ps.setInt(idx, account.getAllowNegative());
                        ids.add(account.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return batchSize;
                    }
                });
        return ids;
    }

    @Override
    public void updateBalances(List<Pair<UUID, BigDecimal>> balanceUpdates) {
        int rows = jdbcTemplate.update(
                "UPDATE account SET balance = account.balance + data_table.balance, updated_at=clock_timestamp() "
                        + "FROM "
                        + "(select unnest(?) as id, unnest(?) as balance) as data_table "
                        + "WHERE account.id=data_table.id "
                        + "AND account.closed=false "
                        + "AND (account.balance + data_table.balance) * abs(account.allow_negative-1) >= 0",
                ps -> {
                    List<UUID> ids = new ArrayList<>();
                    List<BigDecimal> balances = new ArrayList<>();

                    balanceUpdates.forEach(pair -> {
                        ids.add(pair.getFirst());
                        balances.add(pair.getSecond());
                    });
                    ps.setArray(1, ps.getConnection()
                            .createArrayOf("UUID", ids.toArray()));
                    ps.setArray(2, ps.getConnection()
                            .createArrayOf("DECIMAL", balances.toArray()));
                });

        if (rows != balanceUpdates.size()) {
            throw new IncorrectResultSizeDataAccessException(balanceUpdates.size(), rows);
        }
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
    public Account getAccountReferenceById(UUID id) {
        return Account.builder()
                .withId(id)
                .build();
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
                .withUpdated(rs.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.execute("TRUNCATE TABLE account");
    }

    @Override
    public Page<Account> findByCity(Set<String> cities, Pageable pageable) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("cities", cities)
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        String sql =
                "SELECT * "
                        + "FROM account "
                        + "WHERE city IN (:cities) "
                        + "ORDER BY id, city "
                        + "LIMIT :limit OFFSET :offset ";

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
    public List<Account> findByCity(Set<String> cities, int limit) {
        // Use window function to add limit per city
        String sql = "SELECT a.*" +
                " FROM (select *," +
                "             ROW_NUMBER() over (PARTITION BY city) n" +
                "      from account) a" +
                " WHERE a.city IN (:cities) and n <= :limit" +
                " ORDER BY a.city, a.id";

        return this.namedParameterJdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("cities", cities)
                        .addValue("limit", limit),
                (rs, rowNum) -> readAccount(rs));
    }

    @Override
    public List<Account> findByIDs(Set<UUID> ids, boolean forUpdate) {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "Expected transaction");

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", ids);

        return this.namedParameterJdbcTemplate.query(
                "SELECT * FROM account WHERE id in (:ids)" + (forUpdate ? " FOR UPDATE" : ""),
                parameters,
                (rs, rowNum) -> readAccount(rs));
    }
}
