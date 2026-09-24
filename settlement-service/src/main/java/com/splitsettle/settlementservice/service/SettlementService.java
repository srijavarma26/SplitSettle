package com.splitsettle.settlementservice.service;

import com.splitsettle.settlementservice.client.ExpenseClient;
import com.splitsettle.settlementservice.dto.DebtTransactionDTO;
import com.splitsettle.settlementservice.dto.GroupBalanceDTO;
import com.splitsettle.settlementservice.dto.SettlementPlanResponse;
import com.splitsettle.settlementservice.entity.SettlementRecord;
import com.splitsettle.settlementservice.entity.SettlementStatus;
import com.splitsettle.settlementservice.exception.SettlementNotFoundException;
import com.splitsettle.settlementservice.repository.SettlementRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SettlementService {

    private final SettlementRecordRepository settlementRecordRepository;
    private final DebtSimplifierService debtSimplifierService;
    private final ExpenseClient expenseClient;

    public SettlementService(SettlementRecordRepository settlementRecordRepository,
                             DebtSimplifierService debtSimplifierService,
                             ExpenseClient expenseClient) {
        this.settlementRecordRepository = settlementRecordRepository;
        this.debtSimplifierService = debtSimplifierService;
        this.expenseClient = expenseClient;
    }

    @Transactional
    public SettlementPlanResponse getSettlementPlanForGroup(Long groupId) {
        GroupBalanceDTO balanceDTO = expenseClient.getGroupBalances(groupId);
        Map<Long, java.math.BigDecimal> balances = (balanceDTO != null) ? balanceDTO.getNetBalances() : Map.of();

        List<DebtTransactionDTO> calculatedTxs = debtSimplifierService.simplifyDebts(balances);

        // Fetch existing recorded settlements for group
        List<SettlementRecord> existingRecords = settlementRecordRepository.findByGroupId(groupId);

        // Save new calculated pending transactions if not existing
        List<DebtTransactionDTO> resultTxs = new ArrayList<>();
        for (DebtTransactionDTO dto : calculatedTxs) {
            SettlementRecord record = existingRecords.stream()
                    .filter(r -> r.getPayerUserId().equals(dto.getPayerUserId())
                            && r.getPayeeUserId().equals(dto.getPayeeUserId())
                            && r.getAmount().compareTo(dto.getAmount()) == 0)
                    .findFirst()
                    .orElseGet(() -> {
                        SettlementRecord newRec = new SettlementRecord(
                                groupId,
                                dto.getPayerUserId(),
                                dto.getPayeeUserId(),
                                dto.getAmount(),
                                SettlementStatus.PENDING
                        );
                        return settlementRecordRepository.save(newRec);
                    });

            resultTxs.add(new DebtTransactionDTO(
                    record.getId(),
                    record.getPayerUserId(),
                    record.getPayeeUserId(),
                    record.getAmount(),
                    record.getStatus().name()
            ));
        }

        return new SettlementPlanResponse(groupId, resultTxs.size(), resultTxs);
    }

    @Transactional
    public DebtTransactionDTO markSettlementAsPaid(Long settlementId) {
        SettlementRecord record = settlementRecordRepository.findById(settlementId)
                .orElseThrow(() -> new SettlementNotFoundException("Settlement record not found with id: " + settlementId));

        record.setStatus(SettlementStatus.PAID);
        record.setSettledAt(LocalDateTime.now());
        SettlementRecord updated = settlementRecordRepository.save(record);

        return new DebtTransactionDTO(
                updated.getId(),
                updated.getPayerUserId(),
                updated.getPayeeUserId(),
                updated.getAmount(),
                updated.getStatus().name()
        );
    }
}
