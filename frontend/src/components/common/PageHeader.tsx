import { Button, Stack, Typography } from "@mui/material";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import type { ReactNode } from "react";

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  actionLabel?: string;
  onAction?: () => void;
  extra?: ReactNode;
}

export function PageHeader({ title, subtitle, actionLabel, onAction, extra }: PageHeaderProps) {
  return (
    <Stack
      direction={{ xs: "column", sm: "row" }}
      justifyContent="space-between"
      alignItems={{ xs: "flex-start", sm: "center" }}
      spacing={2}
    >
      <div>
        <Typography variant="h5">{title}</Typography>
        {subtitle && (
          <Typography variant="body2" color="text.secondary">
            {subtitle}
          </Typography>
        )}
      </div>
      <Stack direction="row" spacing={1.5} alignItems="center">
        {extra}
        {actionLabel && onAction && (
          <Button variant="contained" startIcon={<AddRoundedIcon />} onClick={onAction}>
            {actionLabel}
          </Button>
        )}
      </Stack>
    </Stack>
  );
}
