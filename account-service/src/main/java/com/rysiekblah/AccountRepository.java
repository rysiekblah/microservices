package com.rysiekblah;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Tomasz_Kozlowski on 11/25/2016.
 */

@Repository
public class AccountRepository {

    private static final String QUERY_FOR_ID = "SELECT ID, NAME, ROLE FROM ACCOUNTS WHERE ID=?";
    private static final String INSERT = "INSERT INTO ACCOUNTS (NAME, ROLE) VALUES (?,?)";
    private static final String QUERY_NAME_LIKE = "SELECT ID, NAME, ROLE FROM ACCOUNTS WHERE NAME LIKE ?";
    private static final String QUERY_ROLE = "SELECT ID, NAME, ROLE FROM ACCOUNTS WHERE ROLE=?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Account findById(long id) throws AccountRepositoryException {
        try {
            return jdbcTemplate.queryForObject(
                    QUERY_FOR_ID,
                    (rs, rowNum) -> new Account(rs.getLong(1), rs.getString(2), rs.getString(3)),
                    id
            );
        } catch (EmptyResultDataAccessException exception) {
            throw new AccountRepositoryException("Account id:" + id + " not found.", exception);
        }
    }

    public List<Account> findByName(String name) {
        return jdbcTemplate.query(
                QUERY_NAME_LIKE,
                (rs, rowNum) -> new Account(rs.getLong(1), rs.getString(2), rs.getString(3)),
                name+"%"
        );
    }

    public CompletableFuture<Account> insert(Account account) {
        return CompletableFuture
                .supplyAsync(() -> account)
                .thenApply(c -> {
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    jdbcTemplate.update(connection -> {
                        PreparedStatement statement = connection.prepareStatement(INSERT, new String[]{"id"});
                        statement.setString(1, account.getName());
                        statement.setString(2, account.getRole());
                        return statement;
                    }, keyHolder);
                    return keyHolder.getKey().intValue();
                })
                .thenApply(id -> jdbcTemplate.queryForObject(
                        QUERY_FOR_ID,
                        (rs, rowNum) -> new Account(rs.getLong(1), rs.getString(2), rs.getString(3)),
                        id
                ));
    }

}
