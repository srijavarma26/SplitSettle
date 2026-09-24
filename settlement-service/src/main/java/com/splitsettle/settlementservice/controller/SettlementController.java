package com.splitsettle.settlementservice.controller;

import com.splitsettle.settlementservice.dto.DebtTransactionDTO;
import com.splitsettle.settlementservice.dto.SettlementPlanResponse;
import com.splitsettle.settlementservice.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<SettlementPlanResponse> getSettlementPlan(@PathVariable Long groupId) {
        SettlementPlanResponse response = settlementService.getSettlementPlanForGroup(groupId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{settlementId}/pay")
    public ResponseEntity<DebtTransactionDTO> markPaid(@PathVariable Long settlementId) {
        DebtTransactionDTO response = settlementService.markSettlementAsPaid(settlementId);
        return ResponseEntity.ok(response);
    }
}
