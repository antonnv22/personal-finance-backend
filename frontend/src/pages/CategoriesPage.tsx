import { useState, type ReactNode } from "react";
import { Box, Card, CardContent, IconButton, List, ListItem, ListItemText, Stack, Typography } from "@mui/material";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import EditRoundedIcon from "@mui/icons-material/EditRounded";
import TrendingUpRoundedIcon from "@mui/icons-material/TrendingUpRounded";
import TrendingDownRoundedIcon from "@mui/icons-material/TrendingDownRounded";
import { useSnackbar } from "notistack";
import { PageHeader } from "@/components/common/PageHeader";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { EmptyState } from "@/components/common/EmptyState";
import { useCategories } from "@/hooks/useCategories";
import type { CategoryResponse, CategoryType } from "@/api/types";
import { CategoryFormDialog } from "./CategoryFormDialog";

function CategoryColumn({
  type,
  title,
  icon,
  color,
  onEdit,
  onCreate,
}: {
  type: CategoryType;
  title: string;
  icon: ReactNode;
  color: "success" | "error";
  onEdit: (category: CategoryResponse) => void;
  onCreate: (type: CategoryType) => void;
}) {
  const categoriesQuery = useCategories(type);

  return (
    <Card sx={{ height: "100%" }}>
      <CardContent>
        <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1.5 }}>
          <Stack direction="row" spacing={1.2} alignItems="center">
            <Box
              sx={{
                bgcolor: (t) => `${t.palette[color].main}1F`,
                color: `${color}.main`,
                borderRadius: "50%",
                width: 36,
                height: 36,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              {icon}
            </Box>
            <Typography variant="subtitle1">{title}</Typography>
          </Stack>
          <IconButton size="small" onClick={() => onCreate(type)} aria-label={`Добавить: ${title}`}>
            <AddRoundedIcon fontSize="small" />
          </IconButton>
        </Stack>

        {categoriesQuery.isLoading ? (
          <LoadingState minHeight={120} />
        ) : categoriesQuery.isError ? (
          <ErrorState error={categoriesQuery.error} onRetry={() => categoriesQuery.refetch()} />
        ) : categoriesQuery.data && categoriesQuery.data.length > 0 ? (
          <List dense disablePadding>
            {categoriesQuery.data.map((category) => (
              <ListItem
                key={category.id}
                disableGutters
                secondaryAction={
                  <IconButton size="small" onClick={() => onEdit(category)}>
                    <EditRoundedIcon fontSize="small" />
                  </IconButton>
                }
                sx={{ borderBottom: "1px solid", borderColor: "divider", py: 1 }}
              >
                <ListItemText primary={category.name} />
              </ListItem>
            ))}
          </List>
        ) : (
          <EmptyState title="Нет категорий" actionLabel="Добавить" onAction={() => onCreate(type)} />
        )}
      </CardContent>
    </Card>
  );
}

export function CategoriesPage() {
  const { enqueueSnackbar } = useSnackbar();
  const [formOpen, setFormOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<CategoryResponse | null>(null);
  const [createType, setCreateType] = useState<CategoryType>("EXPENSE");

  function handleCreate(type: CategoryType) {
    setEditingCategory(null);
    setCreateType(type);
    setFormOpen(true);
  }

  function handleEdit(category: CategoryResponse) {
    setEditingCategory(category);
    setCreateType(category.type);
    setFormOpen(true);
  }

  return (
    <Stack spacing={3}>
      <PageHeader title="Категории" subtitle="Группируйте доходы и расходы по категориям" />

      <Box sx={{ display: "grid", gap: 2.5, gridTemplateColumns: { xs: "1fr", md: "repeat(2, 1fr)" } }}>
        <CategoryColumn
          type="INCOME"
          title="Доходы"
          icon={<TrendingUpRoundedIcon fontSize="small" />}
          color="success"
          onEdit={handleEdit}
          onCreate={handleCreate}
        />
        <CategoryColumn
          type="EXPENSE"
          title="Расходы"
          icon={<TrendingDownRoundedIcon fontSize="small" />}
          color="error"
          onEdit={handleEdit}
          onCreate={handleCreate}
        />
      </Box>

      <CategoryFormDialog
        open={formOpen}
        category={editingCategory}
        createType={createType}
        onClose={() => setFormOpen(false)}
        onSaved={(message) => enqueueSnackbar(message, { variant: "success" })}
      />
    </Stack>
  );
}
