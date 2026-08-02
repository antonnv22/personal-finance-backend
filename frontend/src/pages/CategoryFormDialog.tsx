import { useEffect, useState, type FormEvent } from "react";
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField } from "@mui/material";
import type { CategoryResponse, CategoryType } from "@/api/types";
import { CATEGORY_TYPE_LABELS } from "@/utils/format";
import { useCreateCategory, useUpdateCategory } from "@/hooks/useCategories";
import { extractErrorMessage } from "@/api/client";

interface CategoryFormDialogProps {
  open: boolean;
  onClose: () => void;
  category: CategoryResponse | null;
  /** Fixed category type when creating (dialog is opened from an INCOME or EXPENSE column). */
  createType: CategoryType;
  onSaved: (message: string) => void;
}

export function CategoryFormDialog({
  open,
  onClose,
  category,
  createType,
  onSaved,
}: CategoryFormDialogProps) {
  const isEdit = Boolean(category);
  const createCategory = useCreateCategory();
  const updateCategory = useUpdateCategory();

  const [name, setName] = useState("");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (open) {
      setName(category?.name ?? "");
      setError(null);
    }
  }, [open, category]);

  const submitting = createCategory.isPending || updateCategory.isPending;
  const type = category?.type ?? createType;

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      if (isEdit && category) {
        await updateCategory.mutateAsync({ id: category.id, payload: { name } });
        onSaved("Категория обновлена");
      } else {
        await createCategory.mutateAsync({ name, type: createType });
        onSaved("Категория создана");
      }
      onClose();
    } catch (err) {
      setError(extractErrorMessage(err, "Не удалось сохранить категорию"));
    }
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>
        {isEdit ? "Переименовать категорию" : `Новая категория (${CATEGORY_TYPE_LABELS[type]})`}
      </DialogTitle>
      <Stack component="form" onSubmit={handleSubmit}>
        <DialogContent>
          <Stack spacing={2.5} sx={{ mt: 0.5 }}>
            {error && <Alert severity="error">{error}</Alert>}
            <TextField
              label="Название"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              autoFocus
              fullWidth
            />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2.5 }}>
          <Button onClick={onClose} color="inherit" disabled={submitting}>
            Отмена
          </Button>
          <Button type="submit" variant="contained" disabled={submitting}>
            {isEdit ? "Сохранить" : "Создать"}
          </Button>
        </DialogActions>
      </Stack>
    </Dialog>
  );
}
