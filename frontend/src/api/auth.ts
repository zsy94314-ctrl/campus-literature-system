import { request } from "./request";
import { authStore } from "@/lib/auth-store";
import type { User } from "@/mock/users";

export interface LoginParams {
  username: string;
  password: string;
}
export interface RegisterParams {
  username: string;
  email: string;
  password: string;
}
export interface LoginResult {
  token: string;
  user: User;
}

function adaptUser(raw: any): User {
  return {
    id: String(raw.id),
    username: raw.username,
    email: raw.email || "",
    role: raw.role === "ADMIN" ? "admin" : "user",
    status: raw.status === 1 ? "active" : "disabled",
    createdAt: raw.createTime ? raw.createTime.slice(0, 10) : "",
  };
}

export const authApi = {
  // POST /auth/login
  login: async (params: LoginParams) => {
    const raw = await request<any>({ method: "POST", url: "/auth/login", data: params });
    const result: LoginResult = {
      token: raw.token,
      user: adaptUser(raw.user),
    };
    authStore.setSession(result.token, result.user);
    return result;
  },
  // POST /auth/register
  register: (params: RegisterParams) =>
    request<void>({ method: "POST", url: "/auth/register", data: params }),
  // logout is client-side only
  logout: () => {
    authStore.clear();
    return Promise.resolve({ success: true });
  },
  // getCurrentUser reads from localStorage (backend has no /auth/me)
  getCurrentUser: (): Promise<User | null> => {
    const user = authStore.getUser();
    return Promise.resolve(user);
  },
};
