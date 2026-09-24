package com.splitsettle.settlementservice.dto;

import java.math.BigDecimal;

public class DebtTransactionDTO {

    private Long id;
    private Long payerUserId;
    private Long payeeUserId;
    private BigDecimal amount;
    private String status;

    public DebtTransactionDTO() {
    }

    public DebtTransactionDTO(Long id, Long payerUserId, Long payeeUserId, BigDecimal amount, String status) {
        this.id = id;
        this.payerUserId = payerUserId;
        this.payeeUserId = payeeUserId;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPayerUserId() {
        return payerUserId;
    }

    public void setPayerUserId(Long payerUserId) {
        this.payerUserId = payerUserId;
    }

    public Long getPayeeUserId() {
        return payeeUserId;
    }

    public void setPayeeUserId(Long payeeUserId) {
        this.payeeUserId = payeeUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
