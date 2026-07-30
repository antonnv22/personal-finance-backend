package com.personalfinance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.domain.CategoryType;
import com.personalfinance.domain.Currency;
import com.personalfinance.dto.request.CompleteOccurrenceRequest;
import com.personalfinance.dto.request.CreateRecurringExpenseRequest;
import com.personalfinance.domain.RecurrenceType;
import com.personalfinance.dto.response.AuthResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class RecurringExpenseIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;

    /** Первый платёж — в ближайшем месяце, где нужный день ещё не прошёл. */
    private static YearMonth firstUpcomingMonth(int dayOfMonth) {
        LocalDate today = LocalDate.now();
        YearMonth current = YearMonth.from(today);
        return today.getDayOfMonth() <= dayOfMonth ? current : current.plusMonths(1);
    }

    private String auth(String email) throws Exception {
        AuthResponse response = testDataFactory.registerUser(email);
        return response.token();
    }

    @Test
    void shouldGeneratePlannedPaymentsOnRuleCreation() throws Exception {
        String token = auth("recurring-generate@example.com");
        UUID accountId = testDataFactory.createAccount(token, "Card EUR", Currency.EUR);
        UUID categoryId = testDataFactory.createCategory(token, "Subscriptions", CategoryType.EXPENSE);

        testDataFactory.createRecurringExpense(
                token, "Netflix", new BigDecimal("15.00"), Currency.EUR, accountId, categoryId, 5);

        YearMonth month = firstUpcomingMonth(5);
        mockMvc.perform(get("/api/calendar/" + month.getYear() + "/" + month.getMonthValue())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].name").value("Netflix"))
                .andExpect(jsonPath("$.items[0].status").value("PLANNED"))
                .andExpect(jsonPath("$.items[0].plannedAmount", comparesEqualTo(15.0)))
                .andExpect(jsonPath("$.items[0].actualAmount").doesNotExist())
                .andExpect(jsonPath("$.items[0].date").value(month.atDay(5).toString()));
    }

    /** Открытие календаря повторно не должно плодить дубли. */
    @Test
    void shouldNotDuplicateOccurrencesOnRepeatedCalendarReads() throws Exception {
        String token = auth("recurring-idempotent@example.com");
        UUID accountId = testDataFactory.createAccount(token, "Card", Currency.RUB);
        UUID categoryId = testDataFactory.createCategory(token, "Rent", CategoryType.EXPENSE);

        testDataFactory.createRecurringExpense(
                token, "Rent", new BigDecimal("1000.00"), Currency.RUB, accountId, categoryId, 10);

        YearMonth month = firstUpcomingMonth(10);
        String url = "/api/calendar/" + month.getYear() + "/" + month.getMonthValue();

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)));
        }
    }

    /** 31-е число в коротком месяце схлопывается к последнему дню. */
    @Test
    void shouldClampDayOfMonthToShorterMonths() throws Exception {
        String token = auth("recurring-clamp@example.com");
        UUID accountId = testDataFactory.createAccount(token, "Card", Currency.RUB);
        UUID categoryId = testDataFactory.createCategory(token, "Loan", CategoryType.EXPENSE);

        testDataFactory.createRecurringExpense(
                token, "Loan", new BigDecimal("500.00"), Currency.RUB, accountId, categoryId, 31);

        // Ближайший будущий февраль: в нём 28 или 29 дней, но не 31.
        YearMonth current = YearMonth.from(LocalDate.now());
        YearMonth target = current.getMonthValue() < 2
                ? YearMonth.of(current.getYear(), 2)
                : YearMonth.of(current.getYear() + 1, 2);

        mockMvc.perform(get("/api/calendar/" + target.getYear() + "/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].date").value(target.atEndOfMonth().toString()));
    }

    @Test
    void shouldRejectRuleWithIncomeCategory() throws Exception {
        String token = auth("recurring-income-category@example.com");
        UUID accountId = testDataFactory.createAccount(token, "Card", Currency.RUB);
        UUID incomeCategory = testDataFactory.createCategory(token, "Salary", CategoryType.INCOME);

        CreateRecurringExpenseRequest request = new CreateRecurringExpenseRequest(
                "Wrong", null, new BigDecimal("10.00"), Currency.RUB, accountId, incomeCategory,
                RecurrenceType.MONTHLY, 5, LocalDate.now(), null);

        mockMvc.perform(post("/api/recurring-expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRuleWhenCurrencyDiffersFromAccount() throws Exception {
        String token = auth("recurring-currency@example.com");
        UUID rubAccount = testDataFactory.createAccount(token, "Card RUB", Currency.RUB);
        UUID categoryId = testDataFactory.createCategory(token, "Subscriptions", CategoryType.EXPENSE);

        CreateRecurringExpenseRequest request = new CreateRecurringExpenseRequest(
                "Netflix", null, new BigDecimal("15.00"), Currency.EUR, rubAccount, categoryId,
                RecurrenceType.MONTHLY, 5, LocalDate.now(), null);

        mockMvc.perform(post("/api/recurring-expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /** Сквозной сценарий из задания: план 15 EUR, факт 17.99 EUR, отклонение +2.99. */
    @Test
    void shouldCompletePaymentAndCreateLinkedTransaction() throws Exception {
        String token = auth("recurring-complete@example.com");
        UUID accountId = testDataFactory.createAccount(token, "Card EUR", Currency.EUR);
        UUID categoryId = testDataFactory.createCategory(token, "Subscriptions", CategoryType.EXPENSE);

        testDataFactory.createRecurringExpense(
                token, "Netflix", new BigDecimal("15.00"), Currency.EUR, accountId, categoryId, 5);

        YearMonth month = firstUpcomingMonth(5);
        LocalDate plannedDate = month.atDay(5);
        UUID occurrenceId =
                testDataFactory.findOccurrenceId(token, month.getYear(), month.getMonthValue(), plannedDate);

        LocalDate actualDate = plannedDate.plusDays(2);
        CompleteOccurrenceRequest request = new CompleteOccurrenceRequest(
                actualDate, new BigDecimal("17.99"), accountId, "Netflix subscription");

        mockMvc.perform(post("/api/recurring-expenses/occurrences/" + occurrenceId + "/complete")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.actualAmount", comparesEqualTo(17.99)))
                .andExpect(jsonPath("$.deviation", comparesEqualTo(2.99)))
                .andExpect(jsonPath("$.actualDate").value(actualDate.toString()))
                .andExpect(jsonPath("$.transactionId").isNotEmpty());

        // Транзакция действительно создана и видна в общем списке.
        mockMvc.perform(get("/api/transactions")
                        .param("from", actualDate.toString())
                        .param("to", actualDate.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].amount", comparesEqualTo(17.99)))
                .andExpect(jsonPath("$.content[0].type").value("EXPENSE"))
                .andExpect(jsonPath("$.content[0].description").value("Netflix subscription"));

        // Сводка месяца отражает план и факт.
        mockMvc.perform(get("/api/calendar/" + month.getYear() + "/" + month.getMonthValue())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.plannedTotal", comparesEqualTo(15.0)))
                .andExpect(jsonPath("$.summary.actualTotal", comparesEqualTo(17.99)))
                .andExpect(jsonPath("$.summary.deviation", comparesEqualTo(2.99)));
    }

    @Test
    void shouldRejectCompletingTwice() throws Exception {
        String token = auth("recurring-double-complete@example.com");
        UUID accountId = testDataFactory.createAccount(token, "Card", Currency.RUB);
        UUID categoryId = testDataFactory.createCategory(token, "Subscriptions", CategoryType.EXPENSE);

        testDataFactory.createRecurringExpense(
                token, "Gym", new BigDecimal("20.00"), Currency.RUB, accountId, categoryId, 7);

        YearMonth month = firstUpcomingMonth(7);
        UUID occurrenceId =
                testDataFactory.findOccurrenceId(token, month.getYear(), month.getMonthValue(), month.atDay(7));

        CompleteOccurrenceRequest request = new CompleteOccurrenceRequest(
                month.atDay(7), new BigDecimal("20.00"), accountId, null);
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/recurring-expenses/occurrences/" + occurrenceId + "/complete")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/recurring-expenses/occurrences/" + occurrenceId + "/complete")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldSkipPlannedPayment() throws Exception {
        String token = auth("recurring-skip@example.com");
        UUID accountId = testDataFactory.createAccount(token, "Card", Currency.RUB);
        UUID categoryId = testDataFactory.createCategory(token, "Subscriptions", CategoryType.EXPENSE);

        testDataFactory.createRecurringExpense(
                token, "Spotify", new BigDecimal("10.00"), Currency.RUB, accountId, categoryId, 12);

        YearMonth month = firstUpcomingMonth(12);
        UUID occurrenceId =
                testDataFactory.findOccurrenceId(token, month.getYear(), month.getMonthValue(), month.atDay(12));

        mockMvc.perform(post("/api/recurring-expenses/occurrences/" + occurrenceId + "/skip")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SKIPPED"));

        // Пропущенный платёж не попадает в плановую сумму месяца.
        // Ноль приходит целым числом, поэтому сравниваем как строку — иначе
        // матчер упирается в Integer против Double.
        mockMvc.perform(get("/api/calendar/" + month.getYear() + "/" + month.getMonthValue())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.plannedTotal").value(0));
    }

    /** Платёж чужого пользователя недоступен ни на чтение, ни на изменение. */
    @Test
    void shouldNotExposeOccurrenceToAnotherUser() throws Exception {
        String ownerToken = auth("recurring-owner@example.com");
        UUID accountId = testDataFactory.createAccount(ownerToken, "Card", Currency.RUB);
        UUID categoryId = testDataFactory.createCategory(ownerToken, "Subscriptions", CategoryType.EXPENSE);
        testDataFactory.createRecurringExpense(
                ownerToken, "Private", new BigDecimal("42.00"), Currency.RUB, accountId, categoryId, 9);

        YearMonth month = firstUpcomingMonth(9);
        UUID occurrenceId =
                testDataFactory.findOccurrenceId(ownerToken, month.getYear(), month.getMonthValue(), month.atDay(9));

        String intruderToken = auth("recurring-intruder@example.com");

        mockMvc.perform(get("/api/recurring-expenses/occurrences/" + occurrenceId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/recurring-expenses/occurrences/" + occurrenceId + "/skip")
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        // Чужой календарь пуст.
        mockMvc.perform(get("/api/calendar/" + month.getYear() + "/" + month.getMonthValue())
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    /** Правило с историей архивируется, а не исчезает вместе с расходами. */
    @Test
    void shouldArchiveRuleThatHasCompletedPayments() throws Exception {
        String token = auth("recurring-archive@example.com");
        UUID accountId = testDataFactory.createAccount(token, "Card", Currency.RUB);
        UUID categoryId = testDataFactory.createCategory(token, "Subscriptions", CategoryType.EXPENSE);
        UUID ruleId = testDataFactory.createRecurringExpense(
                token, "Archived", new BigDecimal("30.00"), Currency.RUB, accountId, categoryId, 15);

        YearMonth month = firstUpcomingMonth(15);
        UUID occurrenceId =
                testDataFactory.findOccurrenceId(token, month.getYear(), month.getMonthValue(), month.atDay(15));

        mockMvc.perform(post("/api/recurring-expenses/occurrences/" + occurrenceId + "/complete")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CompleteOccurrenceRequest(
                                month.atDay(15), new BigDecimal("30.00"), accountId, null))))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/recurring-expenses/" + ruleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/recurring-expenses/" + ruleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldReturnUpcomingPaymentsForDashboard() throws Exception {
        String token = auth("recurring-upcoming@example.com");
        UUID accountId = testDataFactory.createAccount(token, "Card", Currency.RUB);
        UUID categoryId = testDataFactory.createCategory(token, "Subscriptions", CategoryType.EXPENSE);
        testDataFactory.createRecurringExpense(
                token, "Upcoming", new BigDecimal("11.00"), Currency.RUB, accountId, categoryId, 20);

        mockMvc.perform(get("/api/recurring-expenses/upcoming")
                        .param("limit", "3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Upcoming"))
                .andExpect(jsonPath("$[0].plannedAmount", comparesEqualTo(11.0)))
                .andExpect(jsonPath("$[0].overdue").value(false));
    }
}
