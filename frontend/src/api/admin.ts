import { mockRequest } from "./request";
import { mockUsers, type User } from "@/mock/users";
import { mockStatistics } from "@/mock/reviews";

export const adminApi = {
  // GET /admin/users
  listUsers: () => mockRequest<User[]>(mockUsers),
  // PATCH /admin/users/:id/status
  toggleUserStatus: (id: string, status: "active" | "disabled") =>
    mockRequest<{ success: boolean }>({ success: true }),
  // GET /admin/statistics
  getStatistics: () =>
    mockRequest<typeof mockStatistics & { recentTrend: { date: string; value: number }[] }>({
      ...mockStatistics,
      recentTrend: [
        { date: "周一", value: 320 },
        { date: "周二", value: 412 },
        { date: "周三", value: 380 },
        { date: "周四", value: 504 },
        { date: "周五", value: 612 },
        { date: "周六", value: 290 },
        { date: "周日", value: 245 },
      ],
    }),
};
