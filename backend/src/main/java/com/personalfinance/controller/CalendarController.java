package com.personalfinance.controller;

import com.personalfinance.config.SecurityUtils;
import com.personalfinance.dto.response.CalendarMonthResponse;
import com.personalfinance.dto.response.PlannedVsActualResponse;
import com.personalfinance.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calendar")
@Tag(name = "Calendar", description = "Monthly view of planned, completed and skipped payments")
public class CalendarController {

    private final CalendarService calendarService;
    private final SecurityUtils securityUtils;

    public CalendarController(CalendarService calendarService, SecurityUtils securityUtils) {
        this.calendarService = calendarService;
        this.securityUtils = securityUtils;
    }

    @Operation(
            summary = "Payments for a month",
            description = "Returns every payment of the month plus planned/actual totals. "
                    + "Missing payments for active rules are generated on the fly, so browsing "
                    + "ahead never shows an empty month.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Calendar returned"),
            @ApiResponse(responseCode = "400", description = "Month is outside 1..12")
    })
    @GetMapping("/{year}/{month}")
    public CalendarMonthResponse getMonth(
            @Parameter(description = "Four-digit year", example = "2026") @PathVariable int year,
            @Parameter(description = "Month, 1-12", example = "8") @PathVariable int month) {
        return calendarService.getMonth(securityUtils.getCurrentUserId(), year, month);
    }

    @Operation(summary = "Planned vs actual totals for a month", description = "Used by the dashboard block")
    @GetMapping("/{year}/{month}/summary")
    public PlannedVsActualResponse getSummary(@PathVariable int year, @PathVariable int month) {
        return calendarService.getPlannedVsActual(securityUtils.getCurrentUserId(), year, month);
    }
}
