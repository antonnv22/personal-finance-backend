package com.personalfinance.controller;

import com.personalfinance.config.SecurityUtils;
import com.personalfinance.domain.TransactionType;
import com.personalfinance.dto.request.CreateTransactionRequest;
import com.personalfinance.dto.response.TransactionResponse;
import com.personalfinance.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(
        name = "Transactions",
        description = "Operations for managing financial transactions"
)
public class TransactionController {

    private final TransactionService transactionService;
    private final SecurityUtils securityUtils;


    @Operation(
            summary = "Get transactions",
            description = "Returns paginated list of transactions for the authenticated user. "
                    + "Supports filtering by account, category, type and date range."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = TransactionResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            )
    })
    @GetMapping
    public Page<TransactionResponse> getAll(

            @Parameter(
                    description = "Filter transactions by account identifier",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestParam(required = false)
            UUID accountId,


            @Parameter(
                    description = "Filter transactions by category identifier",
                    example = "550e8400-e29b-41d4-a716-446655440001"
            )
            @RequestParam(required = false)
            UUID categoryId,


            @Parameter(
                    description = "Filter transactions by type",
                    example = "EXPENSE"
            )
            @RequestParam(required = false)
            TransactionType type,


            @Parameter(
                    description = "Start date of transaction period",
                    example = "2026-01-01"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,


            @Parameter(
                    description = "End date of transaction period",
                    example = "2026-07-21"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,


            @Parameter(
                    description = "Pagination parameters"
            )
            @PageableDefault(size = 20)
            Pageable pageable

    ) {
        return transactionService.getAll(
                securityUtils.getCurrentUserId(),
                accountId,
                categoryId,
                type,
                from,
                to,
                pageable
        );
    }


    @Operation(
            summary = "Get transaction by id",
            description = "Returns a single transaction belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = TransactionResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public TransactionResponse getById(

            @Parameter(
                    description = "Transaction identifier",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id

    ) {
        return transactionService.getById(
                securityUtils.getCurrentUserId(),
                id
        );
    }


    @Operation(
            summary = "Create transaction",
            description = "Creates a new income or expense transaction"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = TransactionResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid transaction data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(

            @Valid
            @RequestBody
            CreateTransactionRequest request

    ) {
        return transactionService.create(
                securityUtils.getCurrentUserId(),
                request
        );
    }


    @Operation(
            summary = "Delete transaction",
            description = "Deletes a transaction belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Transaction successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(

            @Parameter(
                    description = "Transaction identifier",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id

    ) {
        transactionService.delete(
                securityUtils.getCurrentUserId(),
                id
        );
    }
}