import { request } from "./request";

export interface SearchHistoryItem {
  id: string;
  keyword: string;
  time: string;
}

function adaptHistory(raw: any): SearchHistoryItem {
  return {
    id: String(raw.id),
    keyword: raw.keyword || "",
    time: raw.createTime || "",
  };
}

export const searchHistoryApi = {
  // GET /search-history
  list: async (): Promise<SearchHistoryItem[]> => {
    const raw = await request<any[]>({ method: "GET", url: "/search-history" });
    return raw.map(adaptHistory);
  },
  // Backend automatically records search history on /literatures/search,
  // so add/remove/clear are no-ops to keep page components unchanged.
  add: (_keyword: string) => Promise.resolve({ success: true }),
  remove: (_id: string) => Promise.resolve({ success: true }),
  clear: () => Promise.resolve({ success: true }),
};
