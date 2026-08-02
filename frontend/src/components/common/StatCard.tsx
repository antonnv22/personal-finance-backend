import { Avatar, Box, Card, CardContent, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

interface StatCardProps {
  label: string;
  value: string;
  icon: ReactNode;
  color: "primary" | "success" | "error" | "secondary";
  helperText?: string;
}

export function StatCard({ label, value, icon, color, helperText }: StatCardProps) {
  return (
    <Card sx={{ height: "100%" }}>
      <CardContent>
        <Stack direction="row" spacing={2} alignItems="flex-start" justifyContent="space-between">
          <Box>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              {label}
            </Typography>
            <Typography variant="h5" sx={{ wordBreak: "break-word" }}>
              {value}
            </Typography>
            {helperText && (
              <Typography variant="caption" color="text.secondary">
                {helperText}
              </Typography>
            )}
          </Box>
          <Avatar
            sx={{
              bgcolor: (theme) => `${theme.palette[color].main}1F`,
              color: `${color}.main`,
              width: 44,
              height: 44,
            }}
          >
            {icon}
          </Avatar>
        </Stack>
      </CardContent>
    </Card>
  );
}
