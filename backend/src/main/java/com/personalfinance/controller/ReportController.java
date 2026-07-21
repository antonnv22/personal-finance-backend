package com.personalfinance.controller;

import com.personalfinance.config.SecurityUtils;
import com.personalfinance.dto.response.CategoryExpenseResponse;
import com.personalfinance.dto.response.MonthlyReportResponse;
import com.personalfinance.dto.response.SummaryReportResponse;
import com.personalfinance.service.ReportService;
import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtils securityUtils;

    @GetMapping("/monthly")
    public MonthlyReportResponse getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month) {
        return reportService.getMonthlyReport(securityUtils.getCurrentUserId(), year, month);
    }

    @GetMapping("/expenses-by-category")
    public List<CategoryExpenseResponse> getExpensesByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.getExpensesByCategory(securityUtils.getCurrentUserId(), from, to);
    }

    @GetMapping("/summary")
    public SummaryReportResponse getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.getSummary(securityUtils.getCurrentUserId(), from, to);
    }
}
