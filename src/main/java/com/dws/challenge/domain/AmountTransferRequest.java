package com.dws.challenge.domain;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class AmountTransferRequest {

  @NotNull
  @NotEmpty
  private final String accountFromId;

  @NotNull
  @NotEmpty
  private final String accountToId;

  @NotNull
  @Min(value = 1, message = "Transfer amount must be greater than zero.")
  private BigDecimal transferAmount;

  public AmountTransferRequest(String accountFromId, String accountToId, BigDecimal transferAmount) {
    this.accountFromId = accountFromId;
    this.accountToId = accountToId;
    this.transferAmount = transferAmount;
  }

}
