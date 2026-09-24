package com.splitsettle.expenseservice.controller;

import com.splitsettle.expenseservice.dto.AddExpenseRequest;
import com.splitsettle.expenseservice.dto.ExpenseResponse;
import com.splitsettle.expenseservice.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/{groupId}/expenses")
    public ResponseEntity<ExpenseResponse> addExpense(@PathVariable Long groupId, @Valid @RequestBody AddExpenseRequest request) {
        ExpenseResponse response = expenseService.addExpense(groupId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(@PathVariable Long groupId) {
        List<ExpenseResponse> response = expenseService.getGroupExpenses(groupId);
        return ResponseEntity.ok(response);
    }
}
