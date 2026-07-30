package com.personalfinance.controller;

import com.personalfinance.config.SecurityUtils;
import com.personalfinance.dto.request.CompleteOccurrenceRequest;
import com.personalfinance.dto.request.CreateRecurringExpenseRequest;
import com.personalfinance.dto.request.UpdateRecurringExpenseRequest;
import com.personalfinance.dto.response.CalendarItemResponse;
import com.personalfinance.dto.response.RecurringExpenseResponse;
import com.personalfinance.dto.response.UpcomingPaymentResponse;
import com.personalfinance.service.CalendarService;
import com.personalfinance.service.RecurringExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recurring-expenses")
@Tag(name = "Recurring expenses", description = "Rules for repeating expenses and their planned payments")
public class RecurringExpenseController {

    private final RecurringExpenseService recurringExpenseService;
    private final CalendarService calendarService;
    private final SecurityUtils securityUtils;

    public RecurringExpenseController(
            RecurringExpenseService recurringExpenseService,
            CalendarService calendarService,
            SecurityUtils securityUtils) {
        this.recurringExpenseService = recurringExpenseService;
        this.calendarService = calendarService;
        this.securityUtils = securityUtils;
    }

    @Operation(summary = "List rules", description = "Returns all recurring expense rules of the authenticated user")
    @GetMapping
    public List<RecurringExpenseResponse> getAll() {
        return recurringExpenseService.getAll(securityUtils.getCurrentUserId());
    }

    @Operation(summary = "Get rule by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule found"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @GetMapping("/{id}")
    public RecurringExpenseResponse getById(@PathVariable UUID id) {
        return recurringExpenseService.getById(securityUtils.getCurrentUserId(), id);
    }

    @Operation(
            summary = "Create rule",
            description = "Creates a rule and immediately generates its planned payments for the configured horizon")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rule created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error, category is not an EXPENSE, or currency differs from the account")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringExpenseResponse create(@Valid @RequestBody CreateRecurringExpenseRequest request) {
        return recurringExpenseService.create(securityUtils.getCurrentUserId(), request);
    }

    @Operation(
            summary = "Update rule",
            description = "Updates the rule and regenerates future planned payments. Completed and skipped payments are preserved")
    @PutMapping("/{id}")
    public RecurringExpenseResponse update(
            @PathVariable UUID id, @Valid @RequestBody UpdateRecurringExpenseRequest request) {
        return recurringExpenseService.update(securityUtils.getCurrentUserId(), id, request);
    }

    @Operation(
            summary = "Delete rule",
            description = "Deletes the rule outright when it has no completed payments; "
                    + "otherwise deactivates it and drops only future planned payments, keeping history intact")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rule deleted or archived"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        recurringExpenseService.delete(securityUtils.getCurrentUserId(), id);
    }

    @Operation(summary = "Upcoming payments", description = "Nearest planned payments, used by the dashboard")
    @GetMapping("/upcoming")
    public List<UpcomingPaymentResponse> getUpcoming(
            @Parameter(description = "How many payments to return", example = "5")
            @RequestParam(defaultValue = "5") int limit) {
        return recurringExpenseService.getUpcoming(securityUtils.getCurrentUserId(), limit);
    }

    @Operation(
            summary = "Complete a planned payment",
            description = "Creates a real transaction with the actual date and amount and links it to the planned payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment completed and transaction created"),
            @ApiResponse(responseCode = "400", description = "Account currency differs from the rule currency"),
            @ApiResponse(responseCode = "409", description = "Payment is already completed or skipped"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PostMapping("/occurrences/{id}/complete")
    public CalendarItemResponse complete(
            @PathVariable UUID id, @Valid @RequestBody CompleteOccurrenceRequest request) {
        return calendarService.complete(securityUtils.getCurrentUserId(), id, request);
    }

    @Operation(summary = "Skip a planned payment", description = "Marks the payment as SKIPPED without creating a transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment skipped"),
            @ApiResponse(responseCode = "409", description = "Payment is already completed or skipped")
    })
    @PostMapping("/occurrences/{id}/skip")
    public CalendarItemResponse skip(@PathVariable UUID id) {
        return calendarService.skip(securityUtils.getCurrentUserId(), id);
    }

    @Operation(summary = "Get a single planned payment")
    @GetMapping("/occurrences/{id}")
    public CalendarItemResponse getOccurrence(@PathVariable UUID id) {
        return calendarService.getOccurrence(securityUtils.getCurrentUserId(), id);
    }
}
