// Centralized API request helper for the Spring Boot backend.
// baseURL: http://localhost:8080/api
// Unwraps the unified response envelope { code, message, data } and returns `data`.
import axios from "axios";
import { toast } from "sonner";

export const API_BASE_URL = "http://localhost:8080/api";

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

// Axios instance for real backend communication.
export const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
});

// Request interceptor: automatically attach Bearer token.
http.interceptors.request.use((cfg) => {
  const token = localStorage.getItem("lit_token");
  if (token) {
    cfg.headers.Authorization = `Bearer ${token}`;
  }
  return cfg;
});

// Response interceptor: handle 401/403 globally.
http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error)) {
      const status = error.response?.status;
      const resData = error.response?.data as ApiResponse<unknown> | undefined;

      if (status === 401) {
        localStorage.removeItem("lit_token");
        localStorage.removeItem("lit_user");
        toast.error("登录已失效，请重新登录");
        window.location.href = "/login";
        return Promise.reject(new Error("未登录或登录失效"));
      }

      if (status === 403) {
        toast.error(resData?.message || "无权限访问");
        return Promise.reject(new Error("无权限"));
      }
    }
    return Promise.reject(error);
  }
);

// Real request helper: unwraps the backend { code, message, data } envelope.
export async function request<T>(config: Parameters<typeof http.request>[0]): Promise<T> {
  const { data } = await http.request<ApiResponse<T>>(config);
  if (data.code !== 200) {
    throw new Error(data.message || "请求失败");
  }
  return data.data;
}
