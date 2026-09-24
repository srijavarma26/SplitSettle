package com.splitsettle.expenseservice.controller;

import com.splitsettle.expenseservice.dto.UserBalanceSummaryResponse;
import com.splitsettle.expenseservice.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserBalanceController {

    private final ExpenseService expenseService;

    public UserBalanceController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/{userId}/balances")
    public ResponseEntity<UserBalanceSummaryResponse> getUserBalanceSummary(@PathVariable Long userId) {
        UserBalanceSummaryResponse response = expenseService.getUserBalanceSummary(userId);
        return ResponseEntity.ok(response);
    }
}
