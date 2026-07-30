import type {
  AccountType,
  CategoryType,
  Currency,
  DisplayStatus,
  OccurrenceStatus,
  TransactionType,
} from "@/api/types";

const currencyLocale: Record<Currency, string> = {
  RUB: "ru-RU",
  USD: "en-US",
  EUR: "de-DE",
};

export function formatMoney(amount: number, currency: Currency = "RUB"): string {
  return new Intl.NumberFormat(currencyLocale[currency] ?? "ru-RU", {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
}

export function formatDate(isoDate: string): string {
  if (!isoDate) return "";
  const [year, month, day] = isoDate.split("T")[0].split("-");
  return `${day}.${month}.${year}`;
}

export function formatDateTime(isoDateTime: string): string {
  if (!isoDateTime) return "";
  const date = new Date(isoDateTime);
  return date.toLocaleString("ru-RU", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

export const ACCOUNT_TYPE_LABELS: Record<AccountType, string> = {
  CASH: "Наличные",
  DEBIT_CARD: "Дебетовая карта",
};

export const CATEGORY_TYPE_LABELS: Record<CategoryType, string> = {
  INCOME: "Доход",
  EXPENSE: "Расход",
};

export const TRANSACTION_TYPE_LABELS: Record<TransactionType, string> = {
  INCOME: "Доход",
  EXPENSE: "Расход",
};

export const CURRENCY_LABELS: Record<Currency, string> = {
  RUB: "₽ Рубль",
  USD: "$ Доллар",
  EUR: "€ Евро",
};

export const MONTH_LABELS = [
  "Январь",
  "Февраль",
  "Март",
  "Апрель",
  "Май",
  "Июнь",
  "Июль",
  "Август",
  "Сентябрь",
  "Октябрь",
  "Ноябрь",
  "Декабрь",
];

export function initialsFromEmail(email: string): string {
  return email.slice(0, 2).toUpperCase();
}

export const WEEKDAY_LABELS = ["Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"];

export const DISPLAY_STATUS_LABELS: Record<DisplayStatus, string> = {
  PLANNED: "Запланирован",
  COMPLETED: "Оплачен",
  SKIPPED: "Пропущен",
  OVERDUE: "Просрочен",
};

/**
 * Схлопывает хранимый статус и признак просрочки в одно значение для UI.
 * OVERDUE в базе не существует — это просроченный PLANNED.
 */
export function toDisplayStatus(item: {
  status: OccurrenceStatus;
  overdue: boolean;
}): DisplayStatus {
  return item.status === "PLANNED" && item.overdue ? "OVERDUE" : item.status;
}

/** Цвет статуса из палитры темы: серый / зелёный / жёлтый / красный. */
export function displayStatusColor(status: DisplayStatus): {
  main: string;
  contrast: string;
} {
  switch (status) {
    case "COMPLETED":
      return { main: "#1DB870", contrast: "#ffffff" };
    case "SKIPPED":
      return { main: "#F5A623", contrast: "#3b2600" };
    case "OVERDUE":
      return { main: "#E5484D", contrast: "#ffffff" };
    default:
      return { main: "#8A94A6", contrast: "#ffffff" };
  }
}

/** Человекочитаемый срок: «через 3 дня», «сегодня», «просрочен на 2 дня». */
export function formatDaysUntil(days: number): string {
  if (days === 0) return "сегодня";
  if (days === 1) return "завтра";
  if (days < 0) {
    const overdueBy = Math.abs(days);
    return `просрочен на ${overdueBy} ${pluralizeDays(overdueBy)}`;
  }
  return `через ${days} ${pluralizeDays(days)}`;
}

function pluralizeDays(days: number): string {
  const mod10 = days % 10;
  const mod100 = days % 100;
  if (mod10 === 1 && mod100 !== 11) return "день";
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return "дня";
  return "дней";
}

/** Отклонение со знаком: +2.99 / −5.00. */
export function formatDeviation(deviation: number, currency: Currency = "RUB"): string {
  const sign = deviation > 0 ? "+" : deviation < 0 ? "−" : "";
  return `${sign}${formatMoney(Math.abs(deviation), currency)}`;
}

/**
 * Итог, валюта которого может быть неизвестна (за период смешаны несколько
 * валют). В таком случае показываем голое число — подписывать его чужим
 * символом было бы враньём.
 */
export function formatTotal(amount: number, currency: Currency | null): string {
  if (!currency) {
    return new Intl.NumberFormat("ru-RU", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  }
  return formatMoney(amount, currency);
}

/** Отклонение со знаком для итога с возможно неизвестной валютой. */
export function formatTotalDeviation(deviation: number, currency: Currency | null): string {
  const sign = deviation > 0 ? "+" : deviation < 0 ? "−" : "";
  return `${sign}${formatTotal(Math.abs(deviation), currency)}`;
}
