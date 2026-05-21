import { mockRequest } from "./request";
import { mockLiteratures, type Literature } from "@/mock/literatures";

const favStore = new Set<string>(["1", "3"]);

export const favoriteApi = {
  // GET /favorites
  list: () => {
    const list = mockLiteratures.filter((l) => favStore.has(l.id));
    return mockRequest<Literature[]>(list);
  },
  // POST /favorites/:literatureId
  add: (literatureId: string) => {
    favStore.add(literatureId);
    return mockRequest<{ success: boolean }>({ success: true });
  },
  // DELETE /favorites/:literatureId
  remove: (literatureId: string) => {
    favStore.delete(literatureId);
    return mockRequest<{ success: boolean }>({ success: true });
  },
  // GET /favorites/:literatureId/check
  isFavorited: (literatureId: string) =>
    mockRequest<{ favorited: boolean }>({ favorited: favStore.has(literatureId) }),
};
