import { Box, Button, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

interface EmptyStateProps {
  icon?: ReactNode;
  title: string;
  description?: string;
  actionLabel?: string;
  onAction?: () => void;
}

export function EmptyState({ icon, title, description, actionLabel, onAction }: EmptyStateProps) {
  return (
    <Box
      sx={{
        textAlign: "center",
        py: 6,
        px: 2,
        color: "text.secondary",
      }}
    >
      <Stack spacing={1.5} alignItems="center">
        {icon}
        <Typography variant="subtitle1" color="text.primary">
          {title}
        </Typography>
        {description && (
          <Typography variant="body2" sx={{ maxWidth: 360 }}>
            {description}
          </Typography>
        )}
        {actionLabel && onAction && (
          <Button variant="contained" onClick={onAction} sx={{ mt: 1 }}>
            {actionLabel}
          </Button>
        )}
      </Stack>
    </Box>
  );
}
