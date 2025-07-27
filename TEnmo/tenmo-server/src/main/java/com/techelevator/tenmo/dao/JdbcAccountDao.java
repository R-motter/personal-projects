package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;

@Service
public class JdbcAccountDao implements AccountDao {


    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public List<Account> getAccounts() {
        List<Account> accountList = new ArrayList<>();
        String sql = "SELECT account_id, user_id, balance FROM account;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                Account account = mapRowToAccount(results);
                accountList.add(account);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataAccessException e) {
            throw new DaoException("Data Access Error", e);
        }
        return accountList;
    }

    @Override
    public Account getAccountsByUserId(int userId) {

        Account account = new Account();

        String sql = "SELECT * FROM account WHERE user_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            if (results.next()) {
                account = mapRowToAccount(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return account;
    }

    @Override
    public Account getAccountById(int accountId) {

        Account account = null;
        String sql = "SELECT * FROM account WHERE account_id = ?";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);
            if (results.next()) {
                account = mapRowToAccount(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return account;
    }

    public BigDecimal getBalance(int userId) {

        try {
            String sql = "SELECT balance FROM account WHERE user_id = ?";
            return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }

    @Override
    public boolean send(Transfer xfer) throws IllegalArgumentException {
        boolean success = false;
        Account account = new Account();


        String sql = "INSERT INTO transfer(transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?, ?, ?, ?, ?)";

        if (xfer.getAmount().doubleValue() <= 0.00) {
            throw new IllegalArgumentException("Value must be greater than 0.00");
        }
        if (xfer.getAccountFrom() == xfer.getAccountTo()) {
            throw new IllegalArgumentException("Cannot send funds to yourself");
        }

        String fromBalanceSql = "SELECT balance FROM account WHERE account_id = ?";
        BigDecimal balance = jdbcTemplate.queryForObject(fromBalanceSql, BigDecimal.class, xfer.getAccountFrom());

        if (balance.doubleValue() - xfer.getAmount().doubleValue() < 0.00) {
            throw new IllegalArgumentException("Not enough funds.");
        }


        try {
            jdbcTemplate.update(sql, xfer.getTransferTypeId(), xfer.getTransferStatusId(), xfer.getAccountFrom(), xfer.getAccountTo(), xfer.getAmount());
            success = true;
        } catch (DataAccessException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        updateBalance(xfer);

        return success;
    }

    @Override
    public boolean request(Transfer xfer) {
        boolean success = false;
        Account account = new Account();


        String sql = "INSERT INTO transfer(transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?, ?, ?, ?, ?)";

        if (xfer.getAmount().doubleValue() <= 0.00) {
            throw new IllegalArgumentException("Value must be greater than 0.00");
        }
        if (xfer.getAccountFrom() == xfer.getAccountTo()) {
            throw new IllegalArgumentException("Cannot send funds to yourself");
        }

        String fromBalanceSql = "SELECT balance FROM account WHERE account_id = ?";
        BigDecimal balance = jdbcTemplate.queryForObject(fromBalanceSql, BigDecimal.class, xfer.getAccountFrom());


        if (balance.doubleValue() - xfer.getAmount().doubleValue() < 0.00) {
            throw new IllegalArgumentException("Not enough funds.");
        }


        try {

            jdbcTemplate.update(sql, xfer.getTransferTypeId(), xfer.getTransferStatusId(), xfer.getAccountFrom(), xfer.getAccountTo(), xfer.getAmount());
            success = true;
        } catch (DataAccessException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return success;
    }

    @Override
    public void updateBalance(Transfer xfer) {
        Account account = new Account();

        int accountFromId = xfer.getAccountFrom();
        int accountToId = xfer.getAccountTo();
        BigDecimal amount = xfer.getAmount();
        try {
            String subtractSql = "UPDATE account SET balance = balance - ? WHERE account_id = ?";
            jdbcTemplate.update(subtractSql, amount, accountFromId);

            String addSql = "UPDATE account SET balance = balance + ? WHERE account_id = ?";
            jdbcTemplate.update(addSql, amount, accountToId);
        } catch (DataAccessException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }

    @Override
    public List<Transfer> getTransfers(){
        List<Transfer> transferList = new ArrayList<>();

        String sql = "SELECT * FROM transfer";
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql);
            while (result.next()) {
                Transfer transfer = mapRowToTransfer(result);
                transferList.add(transfer);
            }
        }catch(CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return transferList;
    }

    @Override
    public List<Transfer> getAllPendingTransfers() {
        List<Transfer> transfers = new ArrayList<>();

        String sql = "SELECT * FROM transfer WHERE transfer_status_id = 1";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                Transfer transfer = mapRowToTransfer(results);
                transfers.add(transfer);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataAccessException e) {
            throw new DaoException("Data Access Error", e);
        }
        return transfers;
    }

    @Override
    public List<Transfer> pending(String username) {

        String getpendingSql = "SELECT t.* FROM transfer t " +
                "JOIN account a ON t.account_from = a.account_id " +
                "JOIN tenmo_user tu ON a.user_id = tu.user_id " +
                "WHERE tu.username = ? AND t.transfer_status_id = 1;";

        List<Transfer> pendingTransfers = new ArrayList<>();

        SqlRowSet results = jdbcTemplate.queryForRowSet(getpendingSql, username);

        while (results.next()) {
            pendingTransfers.add(mapRowToTransfer(results));
        }

        return pendingTransfers;
    }

    @Override
    public void updateTransferStatus(Transfer xfer) {

        int transferStatusId = xfer.getTransferStatusId();
        int transferId = xfer.getTransferId();

        if (transferStatusId == 1) {

            String sql1 = "UPDATE transfer SET transfer_status_id = 1 WHERE transfer_id = ?;";
            jdbcTemplate.update(sql1, transferId);
        }
        else if (transferStatusId == 2) {

            String sql2 = "UPDATE transfer SET transfer_status_id = 2 WHERE transfer_id = ?;";
            jdbcTemplate.update(sql2, transferId);

            updateBalance(xfer);
        }
        else if (transferStatusId == 3) {

            String sql3 = "UPDATE transfer SET transfer_status_id = 3 WHERE transfer_id = ?;";
            jdbcTemplate.update(sql3, transferId);
        }
        else {
            throw new IllegalArgumentException("Invalid transfer status ID: " + transferStatusId);
        }

    }


    @Override
    public List<Transfer> getTransferByTransferId(int transferId) {

        String sql = "SELECT * FROM transfer WHERE transfer_id = ?";

        List<Transfer> transferByTransferId = new ArrayList<>();

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);

        while  (results.next()) {
            transferByTransferId.add(mapRowToTransfer(results));
        }

        return transferByTransferId;
    }

    @Override
    public List<Transfer> getAllCurrentUserTransfers(int userId) {

        String sql = "SELECT * FROM transfer t JOIN account a ON (a.account_id = t.account_from" +
                " OR a.account_id = t.account_to) WHERE a.user_id = ?;";

        List<Transfer> currentUserTransfers = new ArrayList<>();

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);

        while (results.next()) {
            currentUserTransfers.add(mapRowToTransfer(results));
        }

        return currentUserTransfers;

    }





    private Account mapRowToAccount(SqlRowSet rs) {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setBalance(rs.getBigDecimal("balance"));

        return account;
    }

    private Transfer mapRowToTransfer(SqlRowSet rs){
        Transfer transfer = new Transfer();
        transfer.setTransferId(rs.getInt("transfer_id"));
        transfer.setTransferTypeId(rs.getInt("transfer_type_id"));
        transfer.setTransferStatusId(rs.getInt("transfer_status_id"));
        transfer.setAccountFrom(rs.getInt("account_from"));
        transfer.setAccountTo(rs.getInt("account_to"));
        transfer.setAmount(rs.getBigDecimal("amount"));

        return transfer;
    }



}
