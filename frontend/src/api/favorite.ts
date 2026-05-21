import { request } from "./request";
import type { Literature } from "@/mock/literatures";
import { literatureApi } from "./literature";

export const favoriteApi = {
  // GET /favorites
  list: async (): Promise<Literature[]> => {
    const favs = await request<any[]>({ method: "GET", url: "/favorites" });
    const lits = await Promise.all(
      favs.map((fav) => literatureApi.getById(String(fav.literatureId)).catch(() => null))
    );
    return lits.filter(Boolean) as Literature[];
  },
  // POST /favorites/:literatureId
  add: (literatureId: string) => request<void>({ method: "POST", url: `/favorites/${literatureId}` }),
  // DELETE /favorites/:literatureId
  remove: (literatureId: string) => request<void>({ method: "DELETE", url: `/favorites/${literatureId}` }),
  // Check favorited by querying the list (backend has no single check endpoint)
  isFavorited: async (literatureId: string) => {
    const favs = await request<any[]>({ method: "GET", url: "/favorites" });
    return { favorited: favs.some((f: any) => String(f.literatureId) === literatureId) };
  },
};
