import { request } from "./request";

export interface SearchHistoryItem {
  id: string;
  keyword: string;
  searchType: string;
  resultCount: number;
  time: string;
}

function adaptHistory(raw: any): SearchHistoryItem {
  return {
    id: String(raw.id),
    keyword: raw.keyword || "",
    searchType: raw.searchType || "",
    resultCount: raw.resultCount ?? 0,
    time: raw.createTime || "",
  };
}

export const searchHistoryApi = {
  // GET /search-history
  list: async (): Promise<SearchHistoryItem[]> => {
    const raw = await request<any[]>({ method: "GET", url: "/search-history" });
    return raw.map(adaptHistory);
  },
  // DELETE /search-history/:id
  remove: (id: string) => request<void>({ method: "DELETE", url: `/search-history/${id}` }),
  // DELETE /search-history
  clear: () => request<void>({ method: "DELETE", url: "/search-history" }),
};
