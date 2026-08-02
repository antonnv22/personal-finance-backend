import DashboardRoundedIcon from "@mui/icons-material/DashboardRounded";
import AccountBalanceWalletRoundedIcon from "@mui/icons-material/AccountBalanceWalletRounded";
import CategoryRoundedIcon from "@mui/icons-material/CategoryRounded";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import CalendarMonthRoundedIcon from "@mui/icons-material/CalendarMonthRounded";
import BarChartRoundedIcon from "@mui/icons-material/BarChartRounded";
import SettingsRoundedIcon from "@mui/icons-material/SettingsRounded";
import type { SvgIconComponent } from "@mui/icons-material";

export interface NavItem {
  label: string;
  path: string;
  icon: SvgIconComponent;
}

export const NAV_ITEMS: NavItem[] = [
  { label: "Дашборд", path: "/", icon: DashboardRoundedIcon },
  { label: "Счета", path: "/accounts", icon: AccountBalanceWalletRoundedIcon },
  { label: "Категории", path: "/categories", icon: CategoryRoundedIcon },
  { label: "Транзакции", path: "/transactions", icon: ReceiptLongRoundedIcon },
  { label: "Календарь", path: "/calendar", icon: CalendarMonthRoundedIcon },
  { label: "Отчёты", path: "/reports", icon: BarChartRoundedIcon },
  { label: "Настройки", path: "/settings", icon: SettingsRoundedIcon },
];
