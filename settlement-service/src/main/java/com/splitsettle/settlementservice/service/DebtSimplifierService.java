package com.splitsettle.settlementservice.service;

import com.splitsettle.settlementservice.dto.DebtTransactionDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class DebtSimplifierService {

    private static final BigDecimal EPSILON = new BigDecimal("0.009");

    public static class Participant {
        private final Long userId;
        private BigDecimal amount; // positive value representing magnitude

        public Participant(Long userId, BigDecimal amount) {
            this.userId = userId;
            this.amount = amount;
        }

        public Long getUserId() {
            return userId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    /**
     * Given net balances of group members:
     * - positive balance means user is owed money (Creditor)
     * - negative balance means user owes money (Debtor)
     *
     * Returns a list of simplified transactions required to settle all debts in minimum transactions.
     */
    public List<DebtTransactionDTO> simplifyDebts(Map<Long, BigDecimal> netBalances) {
        if (netBalances == null || netBalances.isEmpty()) {
            return Collections.emptyList();
        }

        // Max heap for debtors (highest amount owed first)
        PriorityQueue<Participant> debtors = new PriorityQueue<>(
                (a, b) -> b.getAmount().compareTo(a.getAmount())
        );

        // Max heap for creditors (highest amount to receive first)
        PriorityQueue<Participant> creditors = new PriorityQueue<>(
                (a, b) -> b.getAmount().compareTo(a.getAmount())
        );

        for (Map.Entry<Long, BigDecimal> entry : netBalances.entrySet()) {
            Long userId = entry.getKey();
            BigDecimal balance = entry.getValue();

            if (balance == null) continue;

            if (balance.compareTo(EPSILON.negate()) < 0) {
                // Negative balance -> Debtor (magnitude is abs(balance))
                debtors.add(new Participant(userId, balance.abs().setScale(2, RoundingMode.HALF_UP)));
            } else if (balance.compareTo(EPSILON) > 0) {
                // Positive balance -> Creditor
                creditors.add(new Participant(userId, balance.setScale(2, RoundingMode.HALF_UP)));
            }
        }

        List<DebtTransactionDTO> simplifiedTransactions = new ArrayList<>();

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Participant debtor = debtors.poll();
            Participant creditor = creditors.poll();

            BigDecimal settleAmount = debtor.getAmount().min(creditor.getAmount())
                    .setScale(2, RoundingMode.HALF_UP);

            if (settleAmount.compareTo(BigDecimal.ZERO) > 0) {
                DebtTransactionDTO tx = new DebtTransactionDTO(
                        null, // ID set when persisted in DB
                        debtor.getUserId(),
                        creditor.getUserId(),
                        settleAmount,
                        "PENDING"
                );
                simplifiedTransactions.add(tx);

                debtor.setAmount(debtor.getAmount().subtract(settleAmount).setScale(2, RoundingMode.HALF_UP));
                creditor.setAmount(creditor.getAmount().subtract(settleAmount).setScale(2, RoundingMode.HALF_UP));
            }

            if (debtor.getAmount().compareTo(EPSILON) > 0) {
                debtors.add(debtor);
            }
            if (creditor.getAmount().compareTo(EPSILON) > 0) {
                creditors.add(creditor);
            }
        }

        return simplifiedTransactions;
    }
}
