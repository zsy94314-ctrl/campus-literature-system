import { request } from "./request";
import type { Review } from "@/mock/reviews";

export interface GenerateReviewParams {
  topic: string;
  literatureIds: string[];
  mode?: "rule" | "llm";
}

export function formatReviewTitle(topic: string): string {
  const t = topic.trim();
  if (!t) return "研究综述";
  if (t.includes("综述")) return t;
  return `${t}领域研究综述`;
}

function adaptReview(raw: any): Review {
  return {
    id: String(raw.id),
    topic: raw.topic || "",
    content: raw.content || "",
    generationMode: raw.generationMode || "rule",
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
      timeout: 240000,
      data: {
        topic: params.topic,
        literatureIds: params.literatureIds.map(Number),
        mode: params.mode || "rule",
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
