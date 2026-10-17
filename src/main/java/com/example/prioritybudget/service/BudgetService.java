package com.example.prioritybudget.service;

import com.example.prioritybudget.model.*;
import com.example.prioritybudget.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BudgetService {

    private final UserService userService;
    private final BudgetRepository budgets;
    private final BudgetItemRepository items;
    private final PriorityCalculator calculator;

    public BudgetService(
            UserService userService,
            BudgetRepository budgets,
            BudgetItemRepository items,
            PriorityCalculator calculator) {

        this.userService = userService;
        this.budgets = budgets;
        this.items = items;
        this.calculator = calculator;
    }

    private Budget getUserBudget(String username) {

        AppUser user = userService.getUser(username);

        return budgets.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalStateException("Budget not found"));
    }

    public BigDecimal getBudget(String username) {
        return getUserBudget(username).getTotalBudget();
    }

    @Transactional
    public BigDecimal setBudget(
            String username, BigDecimal amount) {

        validateAmount(amount);

        Budget budget = getUserBudget(username);
        budget.setTotalBudget(amount);

        budgets.save(budget);

        return amount;
    }

    public List<BudgetItem> getItems(String username) {

        Budget budget = getUserBudget(username);

        return items.findByBudgetIdOrderByPriorityAscIdAsc(
                budget.getId());
    }

    @Transactional
    public BudgetItem addItem(
            String username,
            String name,
            String category,
            BigDecimal amount,
            int priority) {

        validateItem(name, category, amount, priority);

        BudgetItem item = new BudgetItem();

        item.setBudget(getUserBudget(username));
        item.setName(name);
        item.setCategory(category);
        item.setAmount(amount);
        item.setPriority(priority);

        return items.save(item);
    }

    public BudgetItem getItem(String username, Long id) {

        Budget budget = getUserBudget(username);

        return items.findByIdAndBudgetId(id, budget.getId())
                .orElse(null);
    }

    @Transactional
    public BudgetItem updateItem(
            String username,
            Long id,
            String name,
            String category,
            BigDecimal amount,
            int priority) {

        BudgetItem item = getItem(username, id);

        if (item == null) {
            return null;
        }

        validateItem(name, category, amount, priority);

        item.setName(name);
        item.setCategory(category);
        item.setAmount(amount);
        item.setPriority(priority);

        return items.save(item);
    }

    @Transactional
    public boolean deleteItem(String username, Long id) {

        BudgetItem item = getItem(username, id);

        if (item == null) {
            return false;
        }

        items.delete(item);
        return true;
    }

    public BigDecimal getTotalExpenses(String username) {

        return getItems(username).stream()
                .map(BudgetItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public PriorityCalculator.AllocationResult allocate(
            String username) {

        return calculator.calculate(
                getBudget(username),
                getItems(username)
        );
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.signum() < 0 ||
                amount.scale() > 2 ||
                amount.precision() - amount.scale() > 13) {

            throw new IllegalArgumentException(
                    "Amount must be nonnegative, with up to "
                            + "13 whole digits and 2 decimal places."
            );
        }
    }

    private void validateItem(
            String name,
            String category,
            BigDecimal amount,
            int priority) {

        if (name == null || name.isBlank() ||
                category == null || category.isBlank() ||
                name.length() > 255 ||
                category.length() > 255 ||
                priority < 1 || priority > 5) {

            throw new IllegalArgumentException(
                    "Enter a valid name, category and "
                            + "priority between 1 and 5."
            );
        }

        validateAmount(amount);
    }
}