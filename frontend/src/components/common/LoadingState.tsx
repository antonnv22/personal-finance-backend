import { Box, CircularProgress } from "@mui/material";

export function LoadingState({ minHeight = 240 }: { minHeight?: number }) {
  return (
    <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight }}>
      <CircularProgress size={32} />
    </Box>
  );
}
