import { createFileRoute, Link } from "@tanstack/react-router";
import { AppShell } from "@/components/layout/AppShell";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { useEffect, useState } from "react";
import { literatureApi, type SearchParams } from "@/api/literature";
import { categoryApi, type Category } from "@/api/category";

import type { Literature } from "@/mock/literatures";
import { Search } from "lucide-react";

export const Route = createFileRoute("/search")({
  validateSearch: (s: Record<string, unknown>) => ({
    q: (s.q as string) ?? "",
    category: (s.category as string) ?? "",
  }),
  component: SearchPage,
});

function SearchPage() {
  const { q, category: initCat } = Route.useSearch();
  const [keyword, setKeyword] = useState(q);
  const [author, setAuthor] = useState("");
  const [category, setCategory] = useState(initCat);
  const [year, setYear] = useState<string>("");
  const [sortBy, setSortBy] = useState<SearchParams["sortBy"]>("relevance");
  const [list, setList] = useState<Literature[]>([]);
  const [cats, setCats] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const pageSize = 10;

  const runSearch = async (targetPage = page) => {
    setLoading(true);
    const res = await literatureApi.search({
      keyword,
      author,
      category: category && category !== "all" ? category : undefined,
      year: year ? Number(year) : undefined,
      sortBy,
      page: targetPage,
      pageSize,
    });
    setList(res.list);
    setTotal(res.total);
    setPage(targetPage);
    setLoading(false);
  };

  useEffect(() => {
    categoryApi.list().then(setCats);
    runSearch(1);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <AppShell>
      <Card>
        <CardContent className="pt-6">
          <div className="flex gap-2">
            <Input
              placeholder="搜索文献标题、关键词、摘要..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && runSearch()}
            />
            <Button onClick={() => runSearch(1)}>
              <Search className="mr-2 h-4 w-4" />
              检索
            </Button>
          </div>
          <div className="mt-4 grid gap-4 md:grid-cols-4">
            <div className="space-y-1.5">
              <Label className="text-xs">作者</Label>
              <Input value={author} onChange={(e) => setAuthor(e.target.value)} placeholder="作者姓名" />
            </div>
            <div className="space-y-1.5">
              <Label className="text-xs">分类</Label>
              <Select value={category} onValueChange={(v) => { setCategory(v); runSearch(1); }}>
                <SelectTrigger><SelectValue placeholder="全部" /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部</SelectItem>
                  {cats.map((c) => (
                    <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label className="text-xs">年份</Label>
              <Input value={year} onChange={(e) => setYear(e.target.value)} placeholder="例如 2023" />
            </div>
            <div className="space-y-1.5">
              <Label className="text-xs">排序</Label>
              <Select value={sortBy} onValueChange={(v) => { setSortBy(v as SearchParams["sortBy"]); runSearch(1); }}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="relevance">相关度</SelectItem>
                  <SelectItem value="year">最新年份</SelectItem>
                  <SelectItem value="citations">引用次数</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="mt-4 flex items-center justify-between text-sm text-muted-foreground">
        <span>共找到 {total} 篇文献</span>
        <Link to="/advanced-search" className="text-primary hover:underline">前往高级检索 →</Link>
      </div>

      <div className="mt-3 space-y-3">
        {loading && <p className="py-8 text-center text-sm text-muted-foreground">加载中...</p>}
        {!loading && list.map((l) => (
          <Card key={l.id} className="transition-shadow hover:shadow-md">
            <CardContent className="pt-6">
              <Link to="/literature/$id" params={{ id: l.id }} className="block">
                <h3 className="text-lg font-medium text-foreground hover:text-primary">{l.title}</h3>
              </Link>
              <div className="mt-1 text-xs text-muted-foreground">
                {l.authors.join(", ")} · {l.journal} · {l.year} · 引用 {l.citations}
              </div>
              <p className="mt-2 line-clamp-2 text-sm text-muted-foreground">{l.abstract}</p>
              <div className="mt-3 flex flex-wrap gap-1.5">
                {l.keywords.map((k) => (
                  <Badge key={k} variant="secondary">{k}</Badge>
                ))}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="mt-4 flex items-center justify-between text-sm text-muted-foreground">
        <span>共找到 {total} 篇文献</span>
        <div className="flex items-center gap-4">
          <span>第 {page} / {Math.max(1, Math.ceil(total / pageSize))} 页</span>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={page <= 1}
              onClick={() => runSearch(page - 1)}
            >
              上一页
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={page >= Math.ceil(total / pageSize)}
              onClick={() => runSearch(page + 1)}
            >
              下一页
            </Button>
          </div>
        </div>
      </div>
    </AppShell>
  );
}
