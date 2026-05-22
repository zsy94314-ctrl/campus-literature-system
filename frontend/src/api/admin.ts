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

export interface CategoryStat {
  name: string;
  count: number;
}

export interface YearDistribution {
  year: number;
  count: number;
}

export interface DocumentTypeDistribution {
  type: string;
  count: number;
}

export interface ReviewModeDistribution {
  mode: string;
  label: string;
  count: number;
}

export interface SearchTypeDistribution {
  type: string;
  label: string;
  count: number;
}

export interface StatisticsData {
  userCount: number;
  literatureCount: number;
  reviewCount: number;
  categoryCount: number;
  favoriteCount: number;
  searchHistoryCount: number;
  llmConfigCount: number;
  activeLlmName: string | null;
  activeLlmProvider: string | null;
  activeLlmModel: string | null;
  activeLlmEnabled: boolean;
  categoryTop: CategoryStat[];
  yearDistribution: YearDistribution[];
  documentTypeDistribution: DocumentTypeDistribution[];
  reviewModeDistribution: ReviewModeDistribution[];
  searchTypeDistribution: SearchTypeDistribution[];
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
  getStatistics: async (): Promise<StatisticsData> => {
    const raw = await request<any>({ method: "GET", url: "/admin/statistics" });
    return {
      userCount: raw.userCount || 0,
      literatureCount: raw.literatureCount || 0,
      reviewCount: raw.reviewCount || 0,
      categoryCount: raw.categoryCount || 0,
      favoriteCount: raw.favoriteCount || 0,
      searchHistoryCount: raw.searchHistoryCount || 0,
      llmConfigCount: raw.llmConfigCount || 0,
      activeLlmName: raw.activeLlmName || null,
      activeLlmProvider: raw.activeLlmProvider || null,
      activeLlmModel: raw.activeLlmModel || null,
      activeLlmEnabled: !!raw.activeLlmEnabled,
      categoryTop: raw.categoryTop || [],
      yearDistribution: raw.yearDistribution || [],
      documentTypeDistribution: raw.documentTypeDistribution || [],
      reviewModeDistribution: raw.reviewModeDistribution || [],
      searchTypeDistribution: raw.searchTypeDistribution || [],
    };
  },
};
