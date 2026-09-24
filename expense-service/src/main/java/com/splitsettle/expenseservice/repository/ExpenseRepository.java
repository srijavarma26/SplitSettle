package com.splitsettle.expenseservice.repository;

import com.splitsettle.expenseservice.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroupId(Long groupId);
    List<Expense> findByPaidByUserId(Long paidByUserId);
}
