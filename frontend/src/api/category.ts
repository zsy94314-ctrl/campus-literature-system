import { request } from "./request";

export interface Category {
  id: string;
  name: string;
  count: number;
  parentId?: string;
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
  // DELETE /categories/:id
  remove: (id: string) => request<void>({ method: "DELETE", url: `/categories/${id}` }),
};
