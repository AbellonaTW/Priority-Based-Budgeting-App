package com.example.prioritybudget.repository;

import com.example.prioritybudget.model.BudgetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BudgetItemRepository
        extends JpaRepository<BudgetItem, Long> {

    List<BudgetItem> findByBudgetIdOrderByPriorityAscIdAsc(
            Long budgetId);

    Optional<BudgetItem> findByIdAndBudgetId(
            Long id, Long budgetId);
}