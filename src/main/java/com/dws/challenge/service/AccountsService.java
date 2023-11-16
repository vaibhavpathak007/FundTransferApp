package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.AmountTransferRequest;
import com.dws.challenge.exception.InsufficientAccountBalanceException;
import com.dws.challenge.exception.TransactionTimeoutException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.dws.challenge.domain.Constants.*;

@Service
@Slf4j
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    private final NotificationService notificationService;

    Lock lock = new ReentrantLock();

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    public void transferAmount(AmountTransferRequest amountTransferRequest) throws TransactionTimeoutException,
            InterruptedException, InsufficientAccountBalanceException {

        log.info("Getting Accounts from memory.");
        // Get all account info from repository
        Account fromAccount = accountsRepository.getAccount(amountTransferRequest.getAccountFromId());
        Account toAccount = accountsRepository.getAccount(amountTransferRequest.getAccountToId());
        BigDecimal transferAmount = amountTransferRequest.getTransferAmount();

        try {
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                log.info("Performing amount transfer");
                withdraw(fromAccount, transferAmount);
                credit(toAccount, transferAmount);
            } else {
                throw new TransactionTimeoutException(TRANSACTION_TIMEOUT);
            }
        }finally {
            lock.unlock();
        }

        log.info("Successfully withdrawn amount");

        notifyAccounts(amountTransferRequest, fromAccount, toAccount);
    }

    private void notifyAccounts(AmountTransferRequest amountTransferRequest, Account fromAccount, Account toAccount) {
        log.info("Notifying account involved in transfer");
        notificationService.notifyAboutTransfer(fromAccount,
                String.format(WITHDRAWN_INFO_MSG, amountTransferRequest.getTransferAmount(), amountTransferRequest.getAccountToId()));
        notificationService.notifyAboutTransfer(toAccount,
                String.format(CREDIT_INFO_MSG, amountTransferRequest.getTransferAmount(), amountTransferRequest.getAccountFromId()));
    }

    private void withdraw(Account fromAccount, BigDecimal amount) throws InsufficientAccountBalanceException {
        log.info("Performing withdrawal form " + fromAccount.getAccountId());
        BigDecimal subtractedAmount = fromAccount.getBalance().subtract(amount);
        if(subtractedAmount.signum() < 0) {
            throw new InsufficientAccountBalanceException(String.format(TRANSACTION_OVERDRAFT, fromAccount.getAccountId()));
        }
        fromAccount.setBalance(subtractedAmount);
    }

    private void credit(Account toAccount, BigDecimal amount) {
        log.info("Performing credit to " + toAccount.getAccountId());
        BigDecimal addAmount = toAccount.getBalance().add(amount);
        toAccount.setBalance(addAmount);
    }


}
