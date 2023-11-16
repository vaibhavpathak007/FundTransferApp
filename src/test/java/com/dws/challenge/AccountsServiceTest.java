package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.AmountTransferRequest;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientAccountBalanceException;
import com.dws.challenge.exception.TransactionTimeoutException;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(TestConfig.class)
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @BeforeEach
  void prepareData() {

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
    Account account = new Account("test-1");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);
    Account account2 = new Account("test-2");
    account2.setBalance(new BigDecimal(100));
    this.accountsService.createAccount(account2);
  }

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  void transferBalance() throws TransactionTimeoutException, InsufficientAccountBalanceException, InterruptedException {

    AmountTransferRequest transferRequest =
            new AmountTransferRequest("test-2", "test-1", new BigDecimal(100));

    this.accountsService.transferAmount(transferRequest);

    Account account1 = accountsService.getAccount("test-1");
    assertThat(account1.getBalance()).isEqualTo(new BigDecimal(1100));

    Account account2 = accountsService.getAccount("test-2");
    assertThat(account2.getBalance()).isEqualTo(new BigDecimal(0));

  }

  @Test
  void transferBalance_Insufficient_balance() throws TransactionTimeoutException, InsufficientAccountBalanceException, InterruptedException {

    AmountTransferRequest transferRequest =
            new AmountTransferRequest("test-2", "test-1", new BigDecimal(100));

    this.accountsService.transferAmount(transferRequest);

    try {
      this.accountsService.transferAmount(transferRequest);
    } catch (InsufficientAccountBalanceException e) {
      assertThat(e.getMessage()).isEqualTo("Insufficient account balance in account: "+ transferRequest.getAccountFromId());
    }
  }

}
