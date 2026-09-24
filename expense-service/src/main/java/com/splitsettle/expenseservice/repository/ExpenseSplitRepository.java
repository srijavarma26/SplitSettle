package com.splitsettle.expenseservice.repository;

import com.splitsettle.expenseservice.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    List<ExpenseSplit> findByExpenseId(Long expenseId);
    List<ExpenseSplit> findByUserId(Long userId);
    List<ExpenseSplit> findByExpenseIdIn(List<Long> expenseIds);
}
