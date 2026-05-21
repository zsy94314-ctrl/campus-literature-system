import { request } from "./request";
import type { Literature } from "@/mock/literatures";

export interface SearchParams {
  keyword?: string;
  author?: string;
  category?: string;
  year?: number;
  sortBy?: "relevance" | "year" | "citations";
  page?: number;
  pageSize?: number;
}
export interface AdvancedSearchParams extends SearchParams {
  title?: string;
  journal?: string;
  yearFrom?: number;
  yearTo?: number;
  doi?: string;
}
export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

function adaptLiterature(raw: any): Literature {
  return {
    id: String(raw.id),
    title: raw.title || "",
    authors: raw.authors
      ? String(raw.authors)
          .split(/,|，/)
          .map((s) => s.trim())
          .filter(Boolean)
      : [],
    abstract: raw.abstractText || raw.abstract || "",
    keywords: raw.keywords
      ? String(raw.keywords)
          .split(/,|，/)
          .map((s) => s.trim())
          .filter(Boolean)
      : [],
    journal: raw.journal || "",
    year: raw.publishYear || raw.year || 0,
    doi: raw.doi || "",
    citations: raw.citationCount || raw.citations || 0,
    category: raw.categoryName || raw.category || "",
  };
}

function buildLiteraturePayload(data: Partial<Literature> & { categoryId?: number; category?: string }): any {
  const payload: any = {
    title: data.title,
    authors: Array.isArray(data.authors) ? data.authors.join(",") : data.authors,
    abstractText: data.abstract,
    keywords: Array.isArray(data.keywords) ? data.keywords.join(",") : data.keywords,
    journal: data.journal,
    publishYear: data.year,
    doi: data.doi,
    citationCount: data.citations,
  };
  if (data.categoryId !== undefined) {
    payload.categoryId = data.categoryId;
  }
  return payload;
}

export const literatureApi = {
  // GET /literatures/search
  search: async (params: SearchParams): Promise<PageResult<Literature>> => {
    const page = params.page ?? 1;
    const size = params.pageSize ?? 10;
    const raw = await request<any>({
      method: "GET",
      url: "/literatures/search",
      params: {
        keyword: params.keyword,
        author: params.author,
        categoryId: params.category && !isNaN(Number(params.category)) ? Number(params.category) : undefined,
        year: params.year,
        sortBy: params.sortBy,
        page,
        size,
      },
    });
    return {
      list: (raw.records || []).map(adaptLiterature),
      total: raw.total || 0,
      page,
      pageSize: size,
    };
  },
  // GET /literatures/search (advanced search falls back to basic search)
  advancedSearch: async (params: AdvancedSearchParams): Promise<PageResult<Literature>> => {
    const page = params.page ?? 1;
    const size = params.pageSize ?? 10;
    const raw = await request<any>({
      method: "GET",
      url: "/literatures/search",
      params: {
        keyword: params.keyword || params.title,
        author: params.author,
        year: params.yearFrom || params.yearTo || params.year,
        sortBy: params.sortBy,
        page,
        size,
      },
    });
    return {
      list: (raw.records || []).map(adaptLiterature),
      total: raw.total || 0,
      page,
      pageSize: size,
    };
  },
  // GET /literatures/:id
  getById: async (id: string): Promise<Literature> => {
    const raw = await request<any>({ method: "GET", url: `/literatures/${id}` });
    return adaptLiterature(raw);
  },
  // GET /ai/recommend/:id
  getSimilar: async (id: string): Promise<Literature[]> => {
    const recs = await request<any[]>({ method: "GET", url: `/ai/recommend/${id}` });
    const lits = await Promise.all(
      recs.map((r) => literatureApi.getById(String(r.literatureId)).catch(() => null))
    );
    return lits.filter(Boolean) as Literature[];
  },
  // POST /literatures (admin)
  create: async (data: Omit<Literature, "id"> & { category?: string; categoryId?: number }): Promise<Literature> => {
    const raw = await request<any>({ method: "POST", url: "/literatures", data: buildLiteraturePayload(data) });
    return adaptLiterature(raw);
  },
  // PUT /literatures/:id (admin)
  update: async (id: string, data: Partial<Literature> & { category?: string; categoryId?: number }): Promise<Literature> => {
    const raw = await request<any>({ method: "PUT", url: `/literatures/${id}`, data: buildLiteraturePayload(data) });
    return adaptLiterature(raw);
  },
  // DELETE /literatures/:id (admin)
  remove: (id: string) => request<void>({ method: "DELETE", url: `/literatures/${id}` }),
};
