import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { authApi } from "@/api/auth";
import { getStoredToken, registerUnauthorizedHandler, setStoredToken } from "@/api/client";
import { decodeJwt, isTokenExpired } from "./jwt";

export interface AuthUser {
  userId: string;
  email: string;
}

export interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);

function userFromToken(token: string): AuthUser | null {
  const payload = decodeJwt(token);
  if (!payload || isTokenExpired(payload)) {
    return null;
  }
  return { userId: payload.userId, email: payload.sub };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const logout = useCallback(() => {
    setStoredToken(null);
    setUser(null);
  }, []);

  useEffect(() => {
    const token = getStoredToken();
    if (token) {
      const decoded = userFromToken(token);
      if (decoded) {
        setUser(decoded);
      } else {
        setStoredToken(null);
      }
    }
    setIsLoading(false);
  }, []);

  useEffect(() => {
    registerUnauthorizedHandler(() => logout());
  }, [logout]);

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login({ email, password });
    setStoredToken(response.token);
    setUser({ userId: response.userId, email: response.email });
  }, []);

  const register = useCallback(async (email: string, password: string) => {
    const response = await authApi.register({ email, password });
    setStoredToken(response.token);
    setUser({ userId: response.userId, email: response.email });
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      isLoading,
      login,
      register,
      logout,
    }),
    [user, isLoading, login, register, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
