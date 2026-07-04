/**
 * Single source of truth for the JWT. Kept in localStorage so the session
 * survives reloads. Swap the implementation here if you move to cookies.
 */
const TOKEN_KEY = "spk_token";

export const tokenStorage = {
  get(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  },
  set(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  },
  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
  },
};
