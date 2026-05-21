import { mockRequest } from "./request";
import { mockSearchHistory } from "@/mock/reviews";

export interface SearchHistoryItem {
  id: string;
  keyword: string;
  time: string;
}

export const searchHistoryApi = {
  // GET /search-history
  list: () => mockRequest<SearchHistoryItem[]>(mockSearchHistory),
  // POST /search-history
  add: (keyword: string) =>
    mockRequest<SearchHistoryItem>({
      id: Date.now().toString(),
      keyword,
      time: new Date().toLocaleString(),
    }),
  // DELETE /search-history/:id
  remove: (id: string) => mockRequest<{ success: boolean }>({ success: true }),
  // DELETE /search-history
  clear: () => mockRequest<{ success: boolean }>({ success: true }),
};
