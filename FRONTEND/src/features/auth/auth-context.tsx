import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { tokenStorage } from "@/lib/token-storage";
import { authApi } from "./auth-api";
import type { LoginRequest, User } from "./types";

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
}

// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

/**
 * Holds the session. On mount, if a token exists, it restores the user via
 * `/auth/profile`; an invalid token is cleared (and the api client 401 handler
 * redirects to login).
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = tokenStorage.get();
    if (!token) {
      setIsLoading(false);
      return;
    }
    authApi
      .getProfile()
      .then(setUser)
      .catch(() => tokenStorage.clear())
      .finally(() => setIsLoading(false));
  }, []);

  const login = useCallback(async (payload: LoginRequest) => {
    const { user: loggedIn, token } = await authApi.login(payload);
    tokenStorage.set(token);
    setUser(loggedIn);
  }, []);

  const logout = useCallback(async () => {
    await authApi.logout();
    tokenStorage.clear();
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ user, isAuthenticated: user !== null, isLoading, login, logout }),
    [user, isLoading, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
