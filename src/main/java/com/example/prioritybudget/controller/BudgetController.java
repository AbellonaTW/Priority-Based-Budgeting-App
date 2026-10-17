package com.example.prioritybudget.controller;

import com.example.prioritybudget.model.BudgetItem;
import com.example.prioritybudget.service.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService service;

    public BudgetController(BudgetService service) {
        this.service = service;
    }

    public record BudgetRequest(BigDecimal totalBudget) {}

    public record ItemRequest(
            String name,
            String category,
            BigDecimal amount,
            int priority
    ) {}

    public record ItemResponse(
            Long id,
            String name,
            String category,
            BigDecimal amount,
            int priority
    ) {
        public static ItemResponse from(BudgetItem item) {
            return new ItemResponse(
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    item.getAmount(),
                    item.getPriority()
            );
        }
    }

    @PostMapping("/set")
    public Map<String, BigDecimal> setBudget(
            @RequestBody BudgetRequest request,
            Principal principal) {

        return Map.of(
                "totalBudget",
                service.setBudget(
                        principal.getName(),
                        request.totalBudget()
                )
        );
    }

    @GetMapping
    public Map<String, BigDecimal> getBudget(
            Principal principal) {

        return Map.of(
                "totalBudget",
                service.getBudget(principal.getName())
        );
    }

    @PostMapping("/items")
    public ResponseEntity<ItemResponse> addItem(
            @RequestBody ItemRequest request,
            Principal principal) {

        BudgetItem item = service.addItem(
                principal.getName(),
                request.name(),
                request.category(),
                request.amount(),
                request.priority()
        );

        return ResponseEntity.status(201)
                .body(ItemResponse.from(item));
    }

    @GetMapping("/items")
    public List<ItemResponse> getItems(Principal principal) {

        return service.getItems(principal.getName())
                .stream()
                .map(ItemResponse::from)
                .toList();
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ItemResponse> getItem(
            @PathVariable Long id,
            Principal principal) {

        BudgetItem item = service.getItem(
                principal.getName(), id);

        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ItemResponse.from(item));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @RequestBody ItemRequest request,
            Principal principal) {

        BudgetItem item = service.updateItem(
                principal.getName(),
                id,
                request.name(),
                request.category(),
                request.amount(),
                request.priority()
        );

        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ItemResponse.from(item));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            Principal principal) {

        boolean deleted = service.deleteItem(
                principal.getName(), id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary(
            Principal principal) {

        String username = principal.getName();

        return Map.of(
                "totalBudget", service.getBudget(username),
                "totalExpenses", service.getTotalExpenses(username),
                "numberOfItems", service.getItems(username).size()
        );
    }

    @GetMapping("/allocate")
    public PriorityCalculator.AllocationResult allocate(
            Principal principal) {

        return service.allocate(principal.getName());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidInput(
            IllegalArgumentException ex) {

        return ResponseEntity.badRequest().body(
                Map.of("error", ex.getMessage())
        );
    }
}