// Lightweight client-side auth store using localStorage. Replace with real
// token/session logic when the Spring Boot backend is integrated.
import type { User } from "@/mock/users";

const TOKEN_KEY = "lit_token";
const USER_KEY = "lit_user";

export const authStore = {
  setSession(token: string, user: User) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  },
  getUser(): User | null {
    if (typeof window === "undefined") return null;
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  },
  getToken(): string | null {
    if (typeof window === "undefined") return null;
    return localStorage.getItem(TOKEN_KEY);
  },
  clear() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
  isAdmin(): boolean {
    return this.getUser()?.role === "admin";
  },
};
