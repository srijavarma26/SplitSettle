package com.splitsettle.expenseservice.dto;

import com.splitsettle.expenseservice.entity.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class AddExpenseRequest {

    @NotNull(message = "Paid by user ID is required")
    private Long paidByUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Split type is required")
    private SplitType splitType;

    @Valid
    private List<ExpenseSplitRequest> splits;

    public AddExpenseRequest() {
    }

    public AddExpenseRequest(Long paidByUserId, BigDecimal amount, String description, SplitType splitType, List<ExpenseSplitRequest> splits) {
        this.paidByUserId = paidByUserId;
        this.amount = amount;
        this.description = description;
        this.splitType = splitType;
        this.splits = splits;
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

    public List<ExpenseSplitRequest> getSplits() {
        return splits;
    }

    public void setSplits(List<ExpenseSplitRequest> splits) {
        this.splits = splits;
    }
}
