import { mockRequest } from "./request";
import { mockCurrentUser, type User } from "@/mock/users";

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

export const authApi = {
  // POST /auth/login
  login: (params: LoginParams) => {
    const isAdmin = params.username === "admin";
    const user: User = isAdmin
      ? { ...mockCurrentUser, id: "4", username: "admin", email: "admin@univ.edu", role: "admin" }
      : { ...mockCurrentUser, username: params.username };
    return mockRequest<LoginResult>({ token: "mock-jwt-token", user });
  },
  // POST /auth/register
  register: (params: RegisterParams) =>
    mockRequest<{ success: boolean }>({ success: true }),
  // POST /auth/logout
  logout: () => mockRequest<{ success: boolean }>({ success: true }),
  // GET /auth/me
  getCurrentUser: () => mockRequest<User>(mockCurrentUser),
};
