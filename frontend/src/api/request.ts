// Centralized API request helper. Mock mode simulates the backend's unified
// response envelope { code, message, data } and unwraps `data` for callers,
// so swapping to real axios requests later won't change any call site.
import axios from "axios";

export const API_BASE_URL = "http://localhost:8080/api";

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

/**
 * Simulate a backend call. Internally constructs the unified response envelope
 * { code: 200, message: "success", data } — the same shape the Spring Boot
 * backend will return — and resolves with the unwrapped `data` payload.
 */
export function mockRequest<T>(data: T, delay = 300): Promise<T> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const envelope: ApiResponse<T> = {
        code: 200,
        message: "success",
        data,
      };
      resolve(envelope.data);
    }, delay);
  });
}

// Axios instance for real backend communication (used once mock is replaced).
export const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

http.interceptors.request.use((cfg) => {
  const token = localStorage.getItem("lit_token");
  if (token) {
    cfg.headers.Authorization = `Bearer ${token}`;
  }
  return cfg;
});

// Real request helper: unwraps the backend { code, message, data } envelope.
export async function request<T>(config: Parameters<typeof http.request>[0]): Promise<T> {
  const { data } = await http.request<ApiResponse<T>>(config);
  if (data.code !== 200) {
    throw new Error(data.message || "请求失败");
  }
  return data.data;
}
