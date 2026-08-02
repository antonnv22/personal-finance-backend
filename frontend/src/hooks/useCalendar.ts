import { useQuery } from "@tanstack/react-query";
import { calendarApi } from "@/api/calendar";

export function useCalendarMonth(year: number, month: number) {
  return useQuery({
    queryKey: ["calendar", year, month],
    queryFn: () => calendarApi.getMonth(year, month),
    placeholderData: (previous) => previous,
  });
}

export function usePlannedVsActual(year: number, month: number) {
  return useQuery({
    queryKey: ["calendar", year, month, "summary"],
    queryFn: () => calendarApi.getSummary(year, month),
  });
}
