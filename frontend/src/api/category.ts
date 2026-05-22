import { request } from "./request";

export interface Category {
  id: string;
  name: string;
  count: number;
  parentId?: string;
}

export interface CategoryStatistics {
  categoryId: string;
  categoryName: string;
  parentId: string;
  count: number;
}

function adaptCategory(raw: any): Category {
  return {
    id: String(raw.id),
    name: raw.name || "",
    count: 0,
    parentId: raw.parentId != null ? String(raw.parentId) : undefined,
  };
}

export const categoryApi = {
  // GET /categories
  list: async (): Promise<Category[]> => {
    const raw = await request<any[]>({ method: "GET", url: "/categories" });
    return raw.map(adaptCategory);
  },
  // POST /categories
  create: (name: string, parentId: string | number = 0) =>
    request<Category>({ method: "POST", url: "/categories", data: { name, parentId: Number(parentId), sortOrder: 0 } }),
  // PUT /categories/:id
  update: (id: string, name: string, parentId: string | number = 0) =>
    request<Category>({ method: "PUT", url: `/categories/${id}`, data: { name, parentId: Number(parentId), sortOrder: 0 } }),
  // GET /categories/statistics
  statistics: async (): Promise<CategoryStatistics[]> => {
    const raw = await request<any[]>({ method: "GET", url: "/categories/statistics" });
    return raw.map((item) => ({
      categoryId: String(item.categoryId),
      categoryName: item.categoryName || "",
      parentId: item.parentId != null ? String(item.parentId) : "0",
      count: Number(item.count) || 0,
    }));
  },
  // DELETE /categories/:id
  remove: (id: string) => request<void>({ method: "DELETE", url: `/categories/${id}` }),
};
