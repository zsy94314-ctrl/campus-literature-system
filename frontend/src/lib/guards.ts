// Route guards for protected pages. Used in `beforeLoad` to redirect
// unauthenticated users to /login, preserving the original URL.
import { redirect } from "@tanstack/react-router";
import { authStore } from "./auth-store";

export function requireAuth(currentHref: string) {
  const user = authStore.getUser();
  if (!user) {
    throw redirect({ to: "/login", search: { redirect: currentHref } as never });
  }
  return user;
}

export function requireAdmin(currentHref: string) {
  const user = requireAuth(currentHref);
  if (user.role !== "admin") {
    throw redirect({ to: "/home" });
  }
  return user;
}
