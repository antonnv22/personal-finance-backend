import { useState } from "react";
import {
  Box,
  Card,
  CardContent,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Stack,
  Typography,
} from "@mui/material";
import MoreVertRoundedIcon from "@mui/icons-material/MoreVertRounded";
import AccountBalanceWalletRoundedIcon from "@mui/icons-material/AccountBalanceWalletRounded";
import { useSnackbar } from "notistack";
import { PageHeader } from "@/components/common/PageHeader";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { EmptyState } from "@/components/common/EmptyState";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { useAccounts, useDeleteAccount } from "@/hooks/useAccounts";
import type { AccountResponse } from "@/api/types";
import { ACCOUNT_TYPE_LABELS, formatMoney } from "@/utils/format";
import { extractErrorMessage } from "@/api/client";
import { AccountFormDialog } from "./AccountFormDialog";

export function AccountsPage() {
  const { enqueueSnackbar } = useSnackbar();
  const accountsQuery = useAccounts();
  const deleteAccount = useDeleteAccount();

  const [formOpen, setFormOpen] = useState(false);
  const [editingAccount, setEditingAccount] = useState<AccountResponse | null>(null);
  const [menuAnchor, setMenuAnchor] = useState<null | HTMLElement>(null);
  const [menuAccount, setMenuAccount] = useState<AccountResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<AccountResponse | null>(null);

  function openCreate() {
    setEditingAccount(null);
    setFormOpen(true);
  }

  function openEdit(account: AccountResponse) {
    setEditingAccount(account);
    setFormOpen(true);
    setMenuAnchor(null);
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await deleteAccount.mutateAsync(deleteTarget.id);
      enqueueSnackbar("Счёт удалён (или архивирован, если по нему есть транзакции)", {
        variant: "success",
      });
      setDeleteTarget(null);
    } catch (err) {
      enqueueSnackbar(extractErrorMessage(err, "Не удалось удалить счёт"), { variant: "error" });
    }
  }

  return (
    <Stack spacing={3}>
      <PageHeader
        title="Счета"
        subtitle="Управляйте вашими счетами и картами"
        actionLabel="Добавить счёт"
        onAction={openCreate}
      />

      {accountsQuery.isLoading ? (
        <LoadingState />
      ) : accountsQuery.isError ? (
        <ErrorState error={accountsQuery.error} onRetry={() => accountsQuery.refetch()} />
      ) : accountsQuery.data && accountsQuery.data.length > 0 ? (
        <Box
          sx={{
            display: "grid",
            gap: 2.5,
            gridTemplateColumns: { xs: "1fr", sm: "repeat(2, 1fr)", lg: "repeat(3, 1fr)" },
          }}
        >
          {accountsQuery.data.map((account) => (
            <Card key={account.id}>
              <CardContent>
                <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                  <Stack direction="row" spacing={1.5} alignItems="center">
                    <Box
                      sx={{
                        bgcolor: "primary.main",
                        color: "primary.contrastText",
                        borderRadius: "50%",
                        width: 40,
                        height: 40,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                    >
                      <AccountBalanceWalletRoundedIcon fontSize="small" />
                    </Box>
                    <Box>
                      <Typography variant="subtitle1" noWrap maxWidth={160}>
                        {account.name}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {ACCOUNT_TYPE_LABELS[account.type]}
                      </Typography>
                    </Box>
                  </Stack>
                  <IconButton
                    size="small"
                    onClick={(e) => {
                      setMenuAnchor(e.currentTarget);
                      setMenuAccount(account);
                    }}
                  >
                    <MoreVertRoundedIcon fontSize="small" />
                  </IconButton>
                </Stack>

                <Typography variant="h5" sx={{ mt: 2.5 }}>
                  {formatMoney(account.balance, account.currency)}
                </Typography>

                {account.archived && <Chip label="В архиве" size="small" sx={{ mt: 1.5 }} />}
              </CardContent>
            </Card>
          ))}
        </Box>
      ) : (
        <EmptyState
          icon={<AccountBalanceWalletRoundedIcon fontSize="large" color="disabled" />}
          title="Пока нет счетов"
          description="Добавьте счёт, чтобы начать учёт доходов и расходов"
          actionLabel="Добавить счёт"
          onAction={openCreate}
        />
      )}

      <Menu anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={() => setMenuAnchor(null)}>
        <MenuItem onClick={() => menuAccount && openEdit(menuAccount)}>Редактировать</MenuItem>
        <MenuItem
          onClick={() => {
            setDeleteTarget(menuAccount);
            setMenuAnchor(null);
          }}
          sx={{ color: "error.main" }}
        >
          Удалить
        </MenuItem>
      </Menu>

      <AccountFormDialog
        open={formOpen}
        account={editingAccount}
        onClose={() => setFormOpen(false)}
        onSaved={(message) => enqueueSnackbar(message, { variant: "success" })}
      />

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Удалить счёт?"
        description={`Счёт «${deleteTarget?.name}» будет удалён. Если по нему есть транзакции, он будет заархивирован вместо удаления.`}
        confirmLabel="Удалить"
        confirmColor="error"
        loading={deleteAccount.isPending}
        onConfirm={handleDelete}
        onClose={() => setDeleteTarget(null)}
      />
    </Stack>
  );
}
