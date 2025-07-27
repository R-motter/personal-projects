package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.RegisterUserDto;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

public interface AccountDao {

    List<Account> getAccounts();

    Account getAccountsByUserId(int userId);

    Account getAccountById(int accountId);

    boolean request(Transfer xfer);

    void updateBalance(Transfer xfer);

    BigDecimal getBalance(int userId);

    boolean send(Transfer xfer) throws IllegalArgumentException;

    List<Transfer> pending(String username);
    List<Transfer> getTransfers();

    List<Transfer> getAllPendingTransfers();

    List<Transfer> getTransferByTransferId(int transferId);

    List<Transfer> getAllCurrentUserTransfers(int userId);

    void updateTransferStatus(Transfer xfer);




}
