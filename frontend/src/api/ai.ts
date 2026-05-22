import { request } from "./request";
import type { Literature } from "@/mock/literatures";

export interface SemanticSearchParams {
  query: string;
  topK?: number;
}

export interface AiSearchResult {
  id: number;
  title: string;
  authors: string;
  abstractText: string;
  keywords: string;
  journal: string;
  publishYear: number;
  categoryId: number;
  categoryName: string;
  citationCount: number;
  documentType: string;
  similarity: number;
}

function adaptAiResult(raw: AiSearchResult): Literature & { similarity: number } {
  return {
    id: String(raw.id),
    title: raw.title || "",
    authors: raw.authors
      ? String(raw.authors)
          .split(/,|，/)
          .map((s) => s.trim())
          .filter(Boolean)
      : [],
    abstract: raw.abstractText || "",
    keywords: raw.keywords
      ? String(raw.keywords)
          .split(/,|，/)
          .map((s) => s.trim())
          .filter(Boolean)
      : [],
    journal: raw.journal || "",
    year: raw.publishYear || 0,
    doi: "",
    citations: raw.citationCount || 0,
    category: raw.categoryName || "",
    categoryId: raw.categoryId != null ? String(raw.categoryId) : undefined,
    documentType: raw.documentType || undefined,
    similarity: raw.similarity ?? 0,
  };
}

export const aiApi = {
  // POST /ai/semantic-search
  semanticSearch: async (params: SemanticSearchParams): Promise<(Literature & { similarity: number })[]> => {
    const raw = await request<AiSearchResult[]>({
      method: "POST",
      url: "/ai/semantic-search",
      data: {
        query: params.query,
        topK: params.topK ?? 20,
      },
    });
    return (raw || []).map(adaptAiResult);
  },

  // GET /ai/recommend/:id
  recommend: async (literatureId: string): Promise<(Literature & { similarity: number })[]> => {
    const raw = await request<AiSearchResult[]>({
      method: "GET",
      url: `/ai/recommend/${literatureId}`,
    });
    return (raw || []).map(adaptAiResult);
  },

  // POST /ai/rebuild-index
  rebuildIndex: async (): Promise<{ count: number }> => {
    const raw = await request<{ count: number }>({
      method: "POST",
      url: "/ai/rebuild-index",
    });
    return raw || { count: 0 };
  },
};
