import { mockRequest } from "./request";
import { mockLiteratures, type Literature } from "@/mock/literatures";

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

function filterAndSort(params: SearchParams | AdvancedSearchParams): Literature[] {
  let list = [...mockLiteratures];
  const adv = params as AdvancedSearchParams;
  if (params.keyword) {
    const k = params.keyword.toLowerCase();
    list = list.filter(
      (l) =>
        l.title.toLowerCase().includes(k) ||
        l.abstract.toLowerCase().includes(k) ||
        l.keywords.some((kw) => kw.toLowerCase().includes(k)),
    );
  }
  if (params.author) list = list.filter((l) => l.authors.some((a) => a.includes(params.author!)));
  if (params.category) list = list.filter((l) => l.category === params.category);
  if (params.year) list = list.filter((l) => l.year === params.year);
  if (adv.title) list = list.filter((l) => l.title.includes(adv.title!));
  if (adv.journal) list = list.filter((l) => l.journal.includes(adv.journal!));
  if (adv.yearFrom) list = list.filter((l) => l.year >= adv.yearFrom!);
  if (adv.yearTo) list = list.filter((l) => l.year <= adv.yearTo!);
  if (adv.doi) list = list.filter((l) => l.doi.includes(adv.doi!));
  if (params.sortBy === "year") list.sort((a, b) => b.year - a.year);
  else if (params.sortBy === "citations") list.sort((a, b) => b.citations - a.citations);
  return list;
}

export const literatureApi = {
  // GET /literatures
  search: (params: SearchParams) => {
    const list = filterAndSort(params);
    return mockRequest<PageResult<Literature>>({
      list,
      total: list.length,
      page: params.page ?? 1,
      pageSize: params.pageSize ?? 10,
    });
  },
  // GET /literatures/advanced
  advancedSearch: (params: AdvancedSearchParams) => {
    const list = filterAndSort(params);
    return mockRequest<PageResult<Literature>>({
      list,
      total: list.length,
      page: params.page ?? 1,
      pageSize: params.pageSize ?? 10,
    });
  },
  // GET /literatures/:id
  getById: (id: string) => {
    const item = mockLiteratures.find((l) => l.id === id) ?? mockLiteratures[0];
    return mockRequest<Literature>(item);
  },
  // GET /literatures/:id/similar
  getSimilar: (id: string) =>
    mockRequest<Literature[]>(mockLiteratures.filter((l) => l.id !== id).slice(0, 3)),
  // POST /literatures (admin)
  create: (data: Omit<Literature, "id">) =>
    mockRequest<Literature>({ ...data, id: Date.now().toString() }),
  // PUT /literatures/:id (admin)
  update: (id: string, data: Partial<Literature>) =>
    mockRequest<Literature>({ ...mockLiteratures[0], ...data, id }),
  // DELETE /literatures/:id (admin)
  remove: (id: string) => mockRequest<{ success: boolean }>({ success: true }),
};
