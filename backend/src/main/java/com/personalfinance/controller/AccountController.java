package com.personalfinance.controller;

import com.personalfinance.config.SecurityUtils;
import com.personalfinance.dto.request.CreateAccountRequest;
import com.personalfinance.dto.request.UpdateAccountRequest;
import com.personalfinance.dto.response.AccountResponse;
import com.personalfinance.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@Tag(
        name = "Accounts",
        description = "Operations for managing user accounts"
)
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final SecurityUtils securityUtils;

    @Operation(
            summary = "Get all accounts",
            description = "Returns all accounts belonging to the authenticated user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Accounts successfully retrieved"
    )
    @GetMapping
    public List<AccountResponse> getAll() {
        return accountService.getAll(securityUtils.getCurrentUserId());
    }

    @Operation(
            summary = "Get account by ID",
            description = "Returns a single account by its identifier"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    @GetMapping("/{id}")
    public AccountResponse getById(
            @Parameter(
                    description = "Account UUID",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id) {
        return accountService.getById(securityUtils.getCurrentUserId(), id);
    }

    @Operation(
            summary = "Create account",
            description = "Creates a new account for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Account successfully created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Account creation data",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = CreateAccountRequest.class
                            )
                    )
            )
            @Valid
            @RequestBody CreateAccountRequest request) {
        return accountService.create(securityUtils.getCurrentUserId(), request);
    }

    @Operation(
            summary = "Update account",
            description = "Updates an existing account"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account updated"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    @PutMapping("/{id}")
    public AccountResponse update(
            @Parameter(
                    description = "Account UUID"
            )
            @PathVariable UUID id,
            @Valid
            @RequestBody UpdateAccountRequest request) {
        return accountService.update(securityUtils.getCurrentUserId(), id, request);
    }

    @Operation(
            summary = "Delete account",
            description = "Deletes account by UUID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Account deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(
                    description = "Account UUID"
            )
            @PathVariable UUID id) {
        accountService.delete(securityUtils.getCurrentUserId(), id);
    }
}
