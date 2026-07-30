import { useState } from "react";
import {
  Box,
  Card,
  Chip,
  IconButton,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import DeleteRoundedIcon from "@mui/icons-material/DeleteRounded";
import FilterAltOffRoundedIcon from "@mui/icons-material/FilterAltOffRounded";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import { useSnackbar } from "notistack";
import { PageHeader } from "@/components/common/PageHeader";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { EmptyState } from "@/components/common/EmptyState";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { useAccounts } from "@/hooks/useAccounts";
import { useCategories } from "@/hooks/useCategories";
import { useDeleteTransaction, useTransactions } from "@/hooks/useTransactions";
import type { TransactionResponse, TransactionType } from "@/api/types";
import { formatDate, formatMoney } from "@/utils/format";
import { extractErrorMessage } from "@/api/client";
import { TransactionFormDialog } from "./TransactionFormDialog";

const PAGE_SIZE_OPTIONS = [10, 20, 50];

export function TransactionsPage() {
  const { enqueueSnackbar } = useSnackbar();

  const [accountId, setAccountId] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [type, setType] = useState<TransactionType | "">("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);

  const [formOpen, setFormOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<TransactionResponse | null>(null);

  const accountsQuery = useAccounts();
  const categoriesQuery = useCategories();
  const transactionsQuery = useTransactions({
    accountId: accountId || undefined,
    categoryId: categoryId || undefined,
    type: type || undefined,
    from: from || undefined,
    to: to || undefined,
    page,
    size,
  });
  const deleteTransaction = useDeleteTransaction();

  const hasFilters = Boolean(accountId || categoryId || type || from || to);

  function resetFilters() {
    setAccountId("");
    setCategoryId("");
    setType("");
    setFrom("");
    setTo("");
    setPage(0);
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await deleteTransaction.mutateAsync(deleteTarget.id);
      enqueueSnackbar("Транзакция удалена", { variant: "success" });
      setDeleteTarget(null);
    } catch (err) {
      enqueueSnackbar(extractErrorMessage(err, "Не удалось удалить транзакцию"), { variant: "error" });
    }
  }

  const page_ = transactionsQuery.data;

  return (
    <Stack spacing={3}>
      <PageHeader
        title="Транзакции"
        subtitle="Все операции по вашим счетам"
        actionLabel="Добавить транзакцию"
        onAction={() => setFormOpen(true)}
      />

      <Card sx={{ p: 2.5 }}>
        <Box
          sx={{
            display: "grid",
            gap: 2,
            gridTemplateColumns: {
              xs: "1fr",
              sm: "repeat(2, 1fr)",
              md: "repeat(3, 1fr)",
              lg: "repeat(6, 1fr)",
            },
            alignItems: "center",
          }}
        >
          <TextField
            select
            label="Счёт"
            size="small"
            value={accountId}
            onChange={(e) => {
              setAccountId(e.target.value);
              setPage(0);
            }}
          >
            <MenuItem value="">Все счета</MenuItem>
            {(accountsQuery.data ?? []).map((account) => (
              <MenuItem key={account.id} value={account.id}>
                {account.name}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            select
            label="Категория"
            size="small"
            value={categoryId}
            onChange={(e) => {
              setCategoryId(e.target.value);
              setPage(0);
            }}
          >
            <MenuItem value="">Все категории</MenuItem>
            {(categoriesQuery.data ?? []).map((category) => (
              <MenuItem key={category.id} value={category.id}>
                {category.name}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            select
            label="Тип"
            size="small"
            value={type}
            onChange={(e) => {
              setType(e.target.value as TransactionType | "");
              setPage(0);
            }}
          >
            <MenuItem value="">Все типы</MenuItem>
            <MenuItem value="INCOME">Доход</MenuItem>
            <MenuItem value="EXPENSE">Расход</MenuItem>
          </TextField>

          <TextField
            label="С даты"
            type="date"
            size="small"
            value={from}
            onChange={(e) => {
              setFrom(e.target.value);
              setPage(0);
            }}
            slotProps={{ inputLabel: { shrink: true } }}
          />

          <TextField
            label="По дату"
            type="date"
            size="small"
            value={to}
            onChange={(e) => {
              setTo(e.target.value);
              setPage(0);
            }}
            slotProps={{ inputLabel: { shrink: true } }}
          />

          <Tooltip title="Сбросить фильтры">
            <span>
              <IconButton onClick={resetFilters} disabled={!hasFilters}>
                <FilterAltOffRoundedIcon />
              </IconButton>
            </span>
          </Tooltip>
        </Box>
      </Card>

      {transactionsQuery.isLoading ? (
        <LoadingState />
      ) : transactionsQuery.isError ? (
        <ErrorState error={transactionsQuery.error} onRetry={() => transactionsQuery.refetch()} />
      ) : page_ && page_.content.length > 0 ? (
        <Card>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Дата</TableCell>
                  <TableCell>Категория</TableCell>
                  <TableCell>Счёт</TableCell>
                  <TableCell>Описание</TableCell>
                  <TableCell align="right">Сумма</TableCell>
                  <TableCell align="right" />
                </TableRow>
              </TableHead>
              <TableBody>
                {page_.content.map((tx) => (
                  <TableRow key={tx.id} hover>
                    <TableCell>{formatDate(tx.transactionDate)}</TableCell>
                    <TableCell>
                      <Chip
                        label={tx.categoryName}
                        size="small"
                        color={tx.type === "INCOME" ? "success" : "error"}
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>{tx.accountName}</TableCell>
                    <TableCell sx={{ maxWidth: 260 }}>
                      <Typography variant="body2" noWrap>
                        {tx.description || "—"}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography
                        variant="body2"
                        fontWeight={600}
                        color={tx.type === "INCOME" ? "success.main" : "error.main"}
                      >
                        {tx.type === "INCOME" ? "+" : "−"}
                        {formatMoney(tx.amount, tx.currency)}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <IconButton size="small" onClick={() => setDeleteTarget(tx)}>
                        <DeleteRoundedIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            component="div"
            count={page_.totalElements}
            page={page_.number}
            onPageChange={(_, newPage) => setPage(newPage)}
            rowsPerPage={page_.size}
            onRowsPerPageChange={(e) => {
              setSize(Number(e.target.value));
              setPage(0);
            }}
            rowsPerPageOptions={PAGE_SIZE_OPTIONS}
            labelRowsPerPage="Строк на странице"
            labelDisplayedRows={({ from: f, to: t, count }) => `${f}–${t} из ${count}`}
          />
        </Card>
      ) : (
        <EmptyState
          icon={<ReceiptLongRoundedIcon fontSize="large" color="disabled" />}
          title={hasFilters ? "Ничего не найдено" : "Пока нет транзакций"}
          description={hasFilters ? "Попробуйте изменить фильтры" : "Добавьте первую транзакцию"}
          actionLabel={hasFilters ? undefined : "Добавить транзакцию"}
          onAction={hasFilters ? undefined : () => setFormOpen(true)}
        />
      )}

      <TransactionFormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        onSaved={(message) => enqueueSnackbar(message, { variant: "success" })}
      />

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Удалить транзакцию?"
        description="Это действие нельзя отменить."
        confirmLabel="Удалить"
        confirmColor="error"
        loading={deleteTransaction.isPending}
        onConfirm={handleDelete}
        onClose={() => setDeleteTarget(null)}
      />
    </Stack>
  );
}
