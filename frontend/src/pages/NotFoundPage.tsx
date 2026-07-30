import { Box, Button, Stack, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

export function NotFoundPage() {
  const navigate = useNavigate();
  return (
    <Box sx={{ display: "flex", minHeight: "100vh", alignItems: "center", justifyContent: "center" }}>
      <Stack spacing={2} alignItems="center">
        <Typography variant="h2" fontWeight={700}>
          404
        </Typography>
        <Typography color="text.secondary">Страница не найдена</Typography>
        <Button variant="contained" onClick={() => navigate("/")}>
          На главную
        </Button>
      </Stack>
    </Box>
  );
}
