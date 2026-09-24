package com.splitsettle.expenseservice.dto;

import com.splitsettle.expenseservice.entity.SplitType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExpenseResponse {

    private Long id;
    private Long groupId;
    private Long paidByUserId;
    private BigDecimal amount;
    private String description;
    private SplitType splitType;
    private List<ExpenseSplitResponse> splits;
    private LocalDateTime createdAt;

    public ExpenseResponse() {
    }

    public ExpenseResponse(Long id, Long groupId, Long paidByUserId, BigDecimal amount, String description, SplitType splitType, List<ExpenseSplitResponse> splits, LocalDateTime createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.paidByUserId = paidByUserId;
        this.amount = amount;
        this.description = description;
        this.splitType = splitType;
        this.splits = splits;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getPaidByUserId() {
        return paidByUserId;
    }

    public void setPaidByUserId(Long paidByUserId) {
        this.paidByUserId = paidByUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public void setSplitType(SplitType splitType) {
        this.splitType = splitType;
    }

    public List<ExpenseSplitResponse> getSplits() {
        return splits;
    }

    public void setSplits(List<ExpenseSplitResponse> splits) {
        this.splits = splits;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
