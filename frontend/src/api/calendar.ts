import { apiClient } from "./client";
import type { CalendarMonthResponse, PlannedVsActualResponse } from "./types";

export const calendarApi = {
  getMonth: (year: number, month: number) =>
    apiClient.get<CalendarMonthResponse>(`/calendar/${year}/${month}`).then((res) => res.data),

  getSummary: (year: number, month: number) =>
    apiClient
      .get<PlannedVsActualResponse>(`/calendar/${year}/${month}/summary`)
      .then((res) => res.data),
};
