package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;

import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.security.SecurityUtils;
import jakarta.validation.Valid;

import com.techelevator.tenmo.exception.DaoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.security.jwt.TokenProvider;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
@RestController
@RequestMapping("/account")
public class AccountController {

    private AccountDao accountDao;
    private UserDao userDao;

    public AccountController(DataSource datasource, AccountDao accountDao, UserDao userDao) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    @GetMapping("")
    public List<Account> listAccounts() {

        return accountDao.getAccounts();

    }

    @GetMapping("/{accountId}")
    public Account getAccountById(@PathVariable int accountId) {

        return accountDao.getAccountById(accountId);
    }

    @GetMapping("/{userId}/balance")
    public BigDecimal getBalance(@PathVariable int userId) {

        BigDecimal balance = accountDao.getBalance(userId);

        return balance;
    }

    @PostMapping("/send")
    public boolean send(@RequestBody Transfer xfer, Principal principal) {

        boolean success = false;

        int userId = userDao.getUserByUsername(principal.getName()).getId();

        int fromAccountId = accountDao.getAccountsByUserId(userId).getAccountId();

        int toAccountId = accountDao.getAccountsByUserId(xfer.getAccountTo()).getAccountId();

        xfer.setAccountTo(toAccountId);

        xfer.setAccountFrom(fromAccountId);

        try {

            success = accountDao.send(xfer);

        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "CheckIt " + e.getMessage());
        }

        return success;
    }

    @PostMapping("/request")
    public boolean request(@RequestBody Transfer xfer, Principal principal) {

        boolean success = false;

        int userId = userDao.getUserByUsername(principal.getName()).getId();

        int toAccountId = accountDao.getAccountsByUserId(userId).getAccountId();

        int fromAccountId = accountDao.getAccountsByUserId(xfer.getAccountFrom()).getAccountId();

        xfer.setAccountTo(toAccountId);

        xfer.setAccountFrom(fromAccountId);

        try {

            success = accountDao.request(xfer);

        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return success;
    }

    @PutMapping("/transfers/updateBalance")
    public void updateBalance(Transfer xfer) {

        try {
            accountDao.updateBalance(xfer);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

    }

    @GetMapping("/transfers/pending")
    public List<Transfer> getAllPendingTransfers() {
        return accountDao.getAllPendingTransfers();
    }


    @GetMapping("/transfers/pending/user/username")
    public List<Transfer> pending(Principal principal) {

        String username = principal.getName();
        return accountDao.pending(username);

    }

    @PutMapping("transfers/update/transferStatusId")
    public void updateTransferStatus(@RequestBody Transfer xfer) {

        try {
            accountDao.updateTransferStatus(xfer);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
    }


    @GetMapping("/transfers")
    public List<Transfer> getTransfers() {
        List<Transfer> transferList;
        transferList = accountDao.getTransfers();
        return transferList;
    }


    @GetMapping("/{userId}/transfers")
    public List<Transfer> getAllCurrentUserTransfers(@PathVariable int userId) {
        List<Transfer> currentUsersTransfers;
        currentUsersTransfers = accountDao.getAllCurrentUserTransfers(userId);

        return currentUsersTransfers;
    }

    @GetMapping("transfers/{transferId}")
    public List<Transfer> getTransferByTransferId(@PathVariable int transferId) {
        List<Transfer> transferByTransferId;
        transferByTransferId = accountDao.getTransferByTransferId(transferId);

        return transferByTransferId;
    }

}
