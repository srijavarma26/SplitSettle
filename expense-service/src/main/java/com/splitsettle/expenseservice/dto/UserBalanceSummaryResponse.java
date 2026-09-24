package com.splitsettle.expenseservice.dto;

import java.math.BigDecimal;

public class UserBalanceSummaryResponse {

    private Long userId;
    private BigDecimal totalOwed;     // Total amount user owes to others
    private BigDecimal totalOwedTo;   // Total amount owed to this user by others
    private BigDecimal netBalance;    // netBalance = totalOwedTo - totalOwed

    public UserBalanceSummaryResponse() {
    }

    public UserBalanceSummaryResponse(Long userId, BigDecimal totalOwed, BigDecimal totalOwedTo, BigDecimal netBalance) {
        this.userId = userId;
        this.totalOwed = totalOwed;
        this.totalOwedTo = totalOwedTo;
        this.netBalance = netBalance;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalOwed() {
        return totalOwed;
    }

    public void setTotalOwed(BigDecimal totalOwed) {
        this.totalOwed = totalOwed;
    }

    public BigDecimal getTotalOwedTo() {
        return totalOwedTo;
    }

    public void setTotalOwedTo(BigDecimal totalOwedTo) {
        this.totalOwedTo = totalOwedTo;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }
}
