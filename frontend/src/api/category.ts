import { mockRequest } from "./request";
import { mockCategories } from "@/mock/literatures";

export interface Category {
  id: string;
  name: string;
  count: number;
}

export const categoryApi = {
  // GET /categories
  list: () => mockRequest<Category[]>(mockCategories),
  // POST /categories
  create: (name: string) =>
    mockRequest<Category>({ id: Date.now().toString(), name, count: 0 }),
  // PUT /categories/:id
  update: (id: string, name: string) => mockRequest<Category>({ id, name, count: 0 }),
  // DELETE /categories/:id
  remove: (id: string) => mockRequest<{ success: boolean }>({ success: true }),
};
