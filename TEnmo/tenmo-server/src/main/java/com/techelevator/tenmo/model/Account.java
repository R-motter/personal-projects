package com.techelevator.tenmo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class Account {
    @NotBlank
    private BigDecimal balance;

    @Min(value = 1, message = "The amount must be greater than 0.00")
    private BigDecimal send;

    @Min(value = 1, message = "The amount must be greater than 0.00")
    private BigDecimal request;

    @NotBlank
    private int accountId;

    @NotBlank
    private int userId;

    public Account(){

    }

    public Account(BigDecimal currentBalance, int accountId, int userId) {
        this.balance = currentBalance;
        this.accountId = accountId;
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getSend() {
        return send;
    }

    public void setSendAmount(BigDecimal send) {
        this.send = send;
    }
//
//    public BigDecimal getRequest() {
//        return request;
//    }
//
//    public void setRequestedAmount(BigDecimal request) {
//        this.request = request;
//    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
        @Override
        public String toString() {
            return "\n--------------------------------------------" +
                    "\n Account Details" +
                    "\n--------------------------------------------" +
                    "\n Account Id: " + accountId +
                    "\n User Id: " + userId +
                    "\n Balance: " + balance;

    }
}

