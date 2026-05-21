import { request } from "./request";

export interface Category {
  id: string;
  name: string;
  count: number;
}

function adaptCategory(raw: any): Category {
  return {
    id: String(raw.id),
    name: raw.name || "",
    count: 0,
  };
}

export const categoryApi = {
  // GET /categories
  list: async (): Promise<Category[]> => {
    const raw = await request<any[]>({ method: "GET", url: "/categories" });
    return raw.map(adaptCategory);
  },
  // POST /categories
  create: (name: string) =>
    request<Category>({ method: "POST", url: "/categories", data: { name, parentId: 0, sortOrder: 0 } }),
  // PUT /categories/:id
  update: (id: string, name: string) =>
    request<Category>({ method: "PUT", url: `/categories/${id}`, data: { name, parentId: 0, sortOrder: 0 } }),
  // DELETE /categories/:id
  remove: (id: string) => request<void>({ method: "DELETE", url: `/categories/${id}` }),
};
