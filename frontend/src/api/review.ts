import { request } from "./request";
import type { Review } from "@/mock/reviews";

export interface GenerateReviewParams {
  topic: string;
  literatureIds: string[];
}

function adaptReview(raw: any): Review {
  return {
    id: String(raw.id),
    topic: raw.topic || "",
    content: raw.content || "",
    references: Array.isArray(raw.references)
      ? raw.references.map((r: any) => String(r.literatureId || r))
      : [],
    createdAt: raw.createTime ? raw.createTime.slice(0, 10) : "",
  };
}

export const reviewApi = {
  // POST /reviews/generate
  generate: async (params: GenerateReviewParams) => {
    const raw = await request<any>({
      method: "POST",
      url: "/reviews/generate",
      data: {
        topic: params.topic,
        literatureIds: params.literatureIds.map(Number),
      },
    });
    return adaptReview(raw);
  },
  // GET /reviews/history
  list: async (): Promise<Review[]> => {
    const raw = await request<any[]>({ method: "GET", url: "/reviews/history" });
    return raw.map(adaptReview);
  },
  // GET /reviews/:id
  getById: async (id: string): Promise<Review> => {
    const raw = await request<any>({ method: "GET", url: `/reviews/${id}` });
    return adaptReview(raw);
  },
  // DELETE /reviews/:id
  remove: (id: string) => request<void>({ method: "DELETE", url: `/reviews/${id}` }),
};
