import { Alert, AlertTitle, Button, Stack } from "@mui/material";
import { extractErrorMessage } from "@/api/client";

export function ErrorState({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  return (
    <Alert severity="error" variant="outlined">
      <AlertTitle>Не удалось загрузить данные</AlertTitle>
      <Stack spacing={1.5} alignItems="flex-start">
        <span>{extractErrorMessage(error)}</span>
        {onRetry && (
          <Button size="small" color="error" variant="outlined" onClick={onRetry}>
            Повторить
          </Button>
        )}
      </Stack>
    </Alert>
  );
}
