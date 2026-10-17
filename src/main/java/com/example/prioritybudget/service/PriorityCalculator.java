package com.example.prioritybudget.service;

import com.example.prioritybudget.model.BudgetItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PriorityCalculator {

    public record Allocation(
            Long itemId,
            String name,
            int priority,
            BigDecimal requestedAmount,
            BigDecimal allocatedAmount,
            String status
    ) {}

    public record AllocationResult(
            BigDecimal availableBudget,
            BigDecimal totalAllocated,
            BigDecimal remainingBudget,
            List<Allocation> allocations
    ) {}

    public AllocationResult calculate(
            BigDecimal available,
            List<BudgetItem> items) {

        List<BudgetItem> sorted = new ArrayList<>(items);

        sorted.sort(
                Comparator.comparingInt(BudgetItem::getPriority)
                        .thenComparing(BudgetItem::getId)
        );

        BigDecimal remaining = available;
        List<Allocation> results = new ArrayList<>();

        for (BudgetItem item : sorted) {

            BigDecimal allocated =
                    remaining.min(item.getAmount());

            remaining = remaining.subtract(allocated);

            String status;

            if (allocated.compareTo(item.getAmount()) == 0) {
                status = "Fully Funded";
            } else if (allocated.signum() > 0) {
                status = "Partially Funded";
            } else {
                status = "Not Funded";
            }

            results.add(new Allocation(
                    item.getId(),
                    item.getName(),
                    item.getPriority(),
                    item.getAmount(),
                    allocated,
                    status
            ));
        }

        return new AllocationResult(
                available,
                available.subtract(remaining),
                remaining,
                results
        );
    }
}