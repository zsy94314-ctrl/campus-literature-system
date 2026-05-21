import { mockRequest } from "./request";
import { mockReviews, type Review } from "@/mock/reviews";
import { mockLiteratures } from "@/mock/literatures";

export interface GenerateReviewParams {
  topic: string;
  literatureIds: string[];
}

export const reviewApi = {
  // POST /reviews/generate
  generate: (params: GenerateReviewParams) => {
    const refs = mockLiteratures.filter((l) => params.literatureIds.includes(l.id));
    const content =
      `本综述围绕「${params.topic}」展开。\n\n` +
      refs
        .map(
          (r, i) =>
            `${i + 1}. ${r.title}（${r.authors.join("、")}，${r.year}）指出：${r.abstract.slice(0, 60)}……`,
        )
        .join("\n\n") +
      `\n\n综上所述，${params.topic}领域近年来研究活跃，未来仍有广阔探索空间。`;
    return mockRequest<Review>(
      {
        id: "r" + Date.now(),
        topic: params.topic,
        content,
        references: params.literatureIds,
        createdAt: new Date().toISOString().slice(0, 10),
      },
      900,
    );
  },
  // GET /reviews
  list: () => mockRequest<Review[]>(mockReviews),
  // GET /reviews/:id
  getById: (id: string) =>
    mockRequest<Review>(mockReviews.find((r) => r.id === id) ?? mockReviews[0]),
  // DELETE /reviews/:id
  remove: (id: string) => mockRequest<{ success: boolean }>({ success: true }),
};
