package com.example.prioritybudget.repository;

import com.example.prioritybudget.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BudgetRepository
        extends JpaRepository<Budget, Long> {

    Optional<Budget> findByUserId(Long userId);
}