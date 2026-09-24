package com.splitsettle.settlementservice.dto;

import java.util.List;

public class SettlementPlanResponse {

    private Long groupId;
    private int totalTransactionsCount;
    private List<DebtTransactionDTO> transactions;

    public SettlementPlanResponse() {
    }

    public SettlementPlanResponse(Long groupId, int totalTransactionsCount, List<DebtTransactionDTO> transactions) {
        this.groupId = groupId;
        this.totalTransactionsCount = totalTransactionsCount;
        this.transactions = transactions;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public int getTotalTransactionsCount() {
        return totalTransactionsCount;
    }

    public void setTotalTransactionsCount(int totalTransactionsCount) {
        this.totalTransactionsCount = totalTransactionsCount;
    }

    public List<DebtTransactionDTO> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<DebtTransactionDTO> transactions) {
        this.transactions = transactions;
    }
}
