// Types mirror the backend DTOs (com.personalfinance.dto.*) exactly.

export type AccountType = "CASH" | "DEBIT_CARD";
export type CategoryType = "INCOME" | "EXPENSE";
export type TransactionType = "INCOME" | "EXPENSE";
export type Currency = "RUB" | "USD" | "EUR";

export const ACCOUNT_TYPES: AccountType[] = ["CASH", "DEBIT_CARD"];
export const CATEGORY_TYPES: CategoryType[] = ["INCOME", "EXPENSE"];
export const CURRENCIES: Currency[] = ["RUB", "USD", "EUR"];

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AccountResponse {
  id: string;
  name: string;
  type: AccountType;
  currency: Currency;
  balance: number;
  archived: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAccountRequest {
  name: string;
  type: AccountType;
  currency: Currency | null;
}

export interface UpdateAccountRequest {
  name: string;
  type: AccountType;
  currency: Currency | null;
}

export interface CategoryResponse {
  id: string;
  name: string;
  type: CategoryType;
  createdAt: string;
}

export interface CreateCategoryRequest {
  name: string;
  type: CategoryType;
}

export interface UpdateCategoryRequest {
  name: string;
}

export interface TransactionResponse {
  id: string;
  amount: number;
  /** Транзакция не имеет своей валюты — это всегда валюта счёта. */
  currency: Currency;
  type: TransactionType;
  accountId: string;
  accountName: string;
  categoryId: string;
  categoryName: string;
  description: string | null;
  transactionDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTransactionRequest {
  amount: string;
  type: TransactionType;
  accountId: string;
  categoryId: string;
  description: string | null;
  transactionDate: string;
}

export interface TransactionFilters {
  accountId?: string;
  categoryId?: string;
  type?: TransactionType;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface MonthlyReportResponse {
  year: number;
  month: number;
  income: number;
  expense: number;
  balance: number;
}

export interface SummaryReportResponse {
  from: string;
  to: string;
  income: number;
  expense: number;
  balance: number;
}

export interface CategoryExpenseResponse {
  categoryName: string;
  amount: number;
}

// --- Регулярные расходы и календарь ---

export type RecurrenceType = "MONTHLY";
export type OccurrenceStatus = "PLANNED" | "COMPLETED" | "SKIPPED";

/**
 * Статус для отображения. OVERDUE в базе не хранится — бэкенд отдаёт
 * признак `overdue` рядом с обычным статусом, здесь они схлопываются
 * в одно значение для раскраски.
 */
export type DisplayStatus = OccurrenceStatus | "OVERDUE";

export const RECURRENCE_TYPES: RecurrenceType[] = ["MONTHLY"];

export interface RecurringExpenseResponse {
  id: string;
  name: string;
  description: string | null;
  plannedAmount: number;
  currency: Currency;
  accountId: string;
  accountName: string;
  categoryId: string;
  categoryName: string;
  recurrenceType: RecurrenceType;
  dayOfMonth: number;
  startDate: string;
  endDate: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRecurringExpenseRequest {
  name: string;
  description: string | null;
  plannedAmount: string;
  currency: Currency;
  accountId: string;
  categoryId: string;
  recurrenceType: RecurrenceType;
  dayOfMonth: number;
  startDate: string;
  endDate: string | null;
}

export interface UpdateRecurringExpenseRequest extends CreateRecurringExpenseRequest {
  active: boolean;
}

export interface CalendarItemResponse {
  occurrenceId: string;
  recurringExpenseId: string;
  date: string;
  name: string;
  plannedAmount: number;
  actualAmount: number | null;
  deviation: number | null;
  currency: Currency;
  actualDate: string | null;
  status: OccurrenceStatus;
  overdue: boolean;
  transactionId: string | null;
  accountName: string;
  categoryName: string;
}

export interface PlannedVsActualResponse {
  plannedTotal: number;
  actualTotal: number;
  deviation: number;
  /**
   * null, если за период смешаны несколько валют: суммы не конвертируются,
   * поэтому подписывать такой итог валютой нельзя.
   */
  currency: Currency | null;
}

export interface CalendarMonthResponse {
  year: number;
  month: number;
  items: CalendarItemResponse[];
  summary: PlannedVsActualResponse;
}

export interface CompleteOccurrenceRequest {
  actualDate: string;
  actualAmount: string;
  accountId: string;
  comment: string | null;
}

export interface UpcomingPaymentResponse {
  occurrenceId: string;
  name: string;
  plannedDate: string;
  daysUntil: number;
  plannedAmount: number;
  currency: Currency;
  overdue: boolean;
}
