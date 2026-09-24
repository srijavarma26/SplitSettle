package com.splitsettle.settlementservice.service;

import com.splitsettle.settlementservice.dto.DebtTransactionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DebtSimplifierServiceTest {

    private DebtSimplifierService debtSimplifierService;

    @BeforeEach
    void setUp() {
        debtSimplifierService = new DebtSimplifierService();
    }

    @Test
    @DisplayName("Should return 1 transaction for a simple 2-person debt")
    void testSimpleTwoUserDebt() {
        Map<Long, BigDecimal> netBalances = new HashMap<>();
        netBalances.put(1L, new BigDecimal("-50.00")); // User 1 owes 50
        netBalances.put(2L, new BigDecimal("50.00"));  // User 2 is owed 50

        List<DebtTransactionDTO> txs = debtSimplifierService.simplifyDebts(netBalances);

        assertEquals(1, txs.size());
        assertEquals(1L, txs.get(0).getPayerUserId());
        assertEquals(2L, txs.get(0).getPayeeUserId());
        assertEquals(new BigDecimal("50.00"), txs.get(0).getAmount());
    }

    @Test
    @DisplayName("Should return 0 transactions when all net balances are zero")
    void testThreeUserCircularDebt() {
        Map<Long, BigDecimal> netBalances = new HashMap<>();
        netBalances.put(1L, BigDecimal.ZERO);
        netBalances.put(2L, BigDecimal.ZERO);
        netBalances.put(3L, BigDecimal.ZERO);

        List<DebtTransactionDTO> txs = debtSimplifierService.simplifyDebts(netBalances);

        assertTrue(txs.isEmpty());
    }

    @Test
    @DisplayName("Should simplify transitive debt A -> B -> C to direct A -> C")
    void testThreeUserTransitiveDebtSimplification() {
        // User 1 owes 100, User 2 net balance is 0 (owed 100 from 1, owes 100 to 3), User 3 is owed 100
        Map<Long, BigDecimal> netBalances = new HashMap<>();
        netBalances.put(1L, new BigDecimal("-100.00"));
        netBalances.put(2L, BigDecimal.ZERO);
        netBalances.put(3L, new BigDecimal("100.00"));

        List<DebtTransactionDTO> txs = debtSimplifierService.simplifyDebts(netBalances);

        assertEquals(1, txs.size());
        assertEquals(1L, txs.get(0).getPayerUserId());
        assertEquals(3L, txs.get(0).getPayeeUserId());
        assertEquals(new BigDecimal("100.00"), txs.get(0).getAmount());
    }

    @Test
    @DisplayName("Should minimize transactions in complex multi-party debt scenario")
    void testComplexMultiUserDebt() {
        // Net balances:
        // User 1: -150.00 (owes 150)
        // User 2: -50.00  (owes 50)
        // User 3: +100.00 (is owed 100)
        // User 4: +100.00 (is owed 100)
        Map<Long, BigDecimal> netBalances = new HashMap<>();
        netBalances.put(1L, new BigDecimal("-150.00"));
        netBalances.put(2L, new BigDecimal("-50.00"));
        netBalances.put(3L, new BigDecimal("100.00"));
        netBalances.put(4L, new BigDecimal("100.00"));

        List<DebtTransactionDTO> txs = debtSimplifierService.simplifyDebts(netBalances);

        assertEquals(3, txs.size());

        BigDecimal totalSettled = txs.stream()
                .map(DebtTransactionDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("200.00"), totalSettled);
    }

    @Test
    @DisplayName("Should handle empty balance map gracefully")
    void testEmptyOrZeroBalances() {
        List<DebtTransactionDTO> txs = debtSimplifierService.simplifyDebts(new HashMap<>());
        assertTrue(txs.isEmpty());
    }
}
