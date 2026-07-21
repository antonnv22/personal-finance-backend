package com.personalfinance.controller;

import com.personalfinance.config.SecurityUtils;
import com.personalfinance.dto.response.CategoryExpenseResponse;
import com.personalfinance.dto.response.MonthlyReportResponse;
import com.personalfinance.dto.response.SummaryReportResponse;
import com.personalfinance.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(
        name = "Reports",
        description = "Endpoints for generating financial reports"
)
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtils securityUtils;


    @Operation(
            summary = "Get monthly report",
            description = "Returns financial report for a specific month including income, expenses and balance"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Monthly report successfully generated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = MonthlyReportResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid year or month parameters",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            )
    })
    @GetMapping("/monthly")
    public MonthlyReportResponse getMonthlyReport(

            @Parameter(
                    description = "Report year",
                    example = "2026"
            )
            @RequestParam int year,


            @Parameter(
                    description = "Report month (1-12)",
                    example = "7"
            )
            @RequestParam int month
    ) {
        return reportService.getMonthlyReport(
                securityUtils.getCurrentUserId(),
                year,
                month
        );
    }


    @Operation(
            summary = "Get expenses by category",
            description = "Returns total expenses grouped by categories for the specified date range"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expenses successfully calculated",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = CategoryExpenseResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date range",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            )
    })
    @GetMapping("/expenses-by-category")
    public List<CategoryExpenseResponse> getExpensesByCategory(

            @Parameter(
                    description = "Start date of report period",
                    example = "2026-01-01"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,


            @Parameter(
                    description = "End date of report period",
                    example = "2026-07-21"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        return reportService.getExpensesByCategory(
                securityUtils.getCurrentUserId(),
                from,
                to
        );
    }


    @Operation(
            summary = "Get financial summary",
            description = "Returns income, expenses and balance summary for the specified date range"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Summary successfully generated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = SummaryReportResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date range",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            )
    })
    @GetMapping("/summary")
    public SummaryReportResponse getSummary(

            @Parameter(
                    description = "Start date of report period",
                    example = "2026-01-01"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,


            @Parameter(
                    description = "End date of report period",
                    example = "2026-07-21"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        return reportService.getSummary(
                securityUtils.getCurrentUserId(),
                from,
                to
        );
    }
}