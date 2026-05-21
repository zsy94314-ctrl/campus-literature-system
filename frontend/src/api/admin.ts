import { request } from "./request";
import type { User } from "@/mock/users";

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

export const adminApi = {
  // GET /admin/users
  listUsers: async (): Promise<User[]> => {
    const raw = await request<any[]>({ method: "GET", url: "/admin/users" });
    return raw.map(adaptUser);
  },
  // PUT /admin/users/:id/status
  toggleUserStatus: (id: string, status: "active" | "disabled") =>
    request<void>({
      method: "PUT",
      url: `/admin/users/${id}/status`,
      data: { status: status === "active" ? 1 : 0 },
    }),
  // GET /admin/statistics
  getStatistics: async () => {
    const raw = await request<any>({ method: "GET", url: "/admin/statistics" });
    return {
      userCount: raw.userCount || 0,
      literatureCount: raw.literatureCount || 0,
      reviewCount: raw.reviewCount || 0,
      categoryCount: raw.categoryCount || 0,
      // Provide a default recentTrend so the statistics page chart doesn't break
      recentTrend: [
        { date: "周一", value: 320 },
        { date: "周二", value: 412 },
        { date: "周三", value: 380 },
        { date: "周四", value: 504 },
        { date: "周五", value: 612 },
        { date: "周六", value: 290 },
        { date: "周日", value: 245 },
      ],
    };
  },
};
