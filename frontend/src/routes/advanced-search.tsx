import { createFileRoute, Link } from "@tanstack/react-router";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useEffect, useState } from "react";
import { literatureApi } from "@/api/literature";
import { categoryApi, type Category } from "@/api/category";
import type { Literature } from "@/mock/literatures";
import { toast } from "sonner";

export const Route = createFileRoute("/advanced-search")({
  component: AdvancedSearchPage,
});

const emptyForm = {
  title: "",
  keyword: "",
  author: "",
  journal: "",
  doi: "",
  category: "",
  documentType: "",
  yearFrom: "",
  yearTo: "",
  sortBy: "relevance" as "relevance" | "year_desc" | "year_asc" | "citation_desc" | "citation_asc",
};

const documentTypes = ["期刊论文", "会议论文", "学位论文", "研究报告"];

function AdvancedSearchPage() {
  const [form, setForm] = useState({ ...emptyForm });
  const [list, setList] = useState<Literature[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [cats, setCats] = useState<Category[]>([]);
  const pageSize = 10;

  useEffect(() => {
    categoryApi.list().then(setCats);
  }, []);

  const runSearch = async (targetPage = 1) => {
    const start = form.yearFrom ? Number(form.yearFrom) : undefined;
    const end = form.yearTo ? Number(form.yearTo) : undefined;
    if (start !== undefined && end !== undefined && start > end) {
      toast.error("起始年份不能大于截止年份");
      return;
    }
    setLoading(true);
    try {
      const res = await literatureApi.advancedSearch({
        title: form.title || undefined,
        keyword: form.keyword || undefined,
        author: form.author || undefined,
        journal: form.journal || undefined,
        doi: form.doi || undefined,
        category: form.category || undefined,
        documentType: form.documentType || undefined,
        yearFrom: start,
        yearTo: end,
        sortBy: form.sortBy,
        page: targetPage,
        pageSize,
      });
      setList(res.list);
      setTotal(res.total);
      setPage(targetPage);
    } finally {
      setLoading(false);
    }
  };

  const reset = () => {
    setForm({ ...emptyForm });
    setList([]);
    setTotal(0);
    setPage(1);
  };

  const fields = [
    { key: "title" as const, label: "标题", placeholder: "文献标题" },
    { key: "keyword" as const, label: "关键词", placeholder: "关键词" },
    { key: "author" as const, label: "作者", placeholder: "作者姓名" },
    { key: "journal" as const, label: "期刊", placeholder: "期刊名称" },
    { key: "doi" as const, label: "DOI", placeholder: "DOI" },
  ];

  return (
    <AppShell>
      <Card>
        <CardHeader>
          <CardTitle>高级检索</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-3">
            {fields.map((f) => (
              <div key={f.key} className="space-y-1.5">
                <Label className="text-xs">{f.label}</Label>
                <Input
                  placeholder={f.placeholder}
                  value={form[f.key]}
                  onChange={(e) => setForm({ ...form, [f.key]: e.target.value })}
                />
              </div>
            ))}
            <div className="space-y-1.5">
              <Label className="text-xs">分类</Label>
              <Select value={form.category} onValueChange={(v) => setForm({ ...form, category: v })}>
                <SelectTrigger>
                  <SelectValue placeholder="全部" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部</SelectItem>
                  {cats.map((c) => (
                    <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label className="text-xs">文献类型</Label>
              <Select value={form.documentType} onValueChange={(v) => setForm({ ...form, documentType: v })}>
                <SelectTrigger>
                  <SelectValue placeholder="全部" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">全部</SelectItem>
                  {documentTypes.map((t) => (
                    <SelectItem key={t} value={t}>{t}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label className="text-xs">起始年份</Label>
              <Input
                placeholder="例如 2011"
                value={form.yearFrom}
                onChange={(e) => setForm({ ...form, yearFrom: e.target.value })}
              />
            </div>
            <div className="space-y-1.5">
              <Label className="text-xs">截止年份</Label>
              <Input
                placeholder="例如 2022"
                value={form.yearTo}
                onChange={(e) => setForm({ ...form, yearTo: e.target.value })}
              />
            </div>
            <div className="space-y-1.5">
              <Label className="text-xs">排序</Label>
              <Select
                value={form.sortBy}
                onValueChange={(v) => setForm({ ...form, sortBy: v as typeof form.sortBy })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="relevance">相关度</SelectItem>
                  <SelectItem value="year_desc">最新年份</SelectItem>
                  <SelectItem value="year_asc">最早年份</SelectItem>
                  <SelectItem value="citation_desc">引用次数（高→低）</SelectItem>
                  <SelectItem value="citation_asc">引用次数（低→高）</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <div className="mt-4 flex justify-end gap-2">
            <Button variant="outline" onClick={reset}>重置</Button>
            <Button onClick={() => runSearch(1)}>检索</Button>
          </div>
        </CardContent>
      </Card>

      <div className="mt-4 flex items-center justify-between text-sm text-muted-foreground">
        <span>共找到 {total} 篇文献</span>
      </div>

      <div className="mt-3 space-y-3">
        {loading && <p className="py-8 text-center text-sm text-muted-foreground">加载中...</p>}
        {!loading && list.map((l) => (
          <Card key={l.id}>
            <CardContent className="pt-6">
              <Link to="/literature/$id" params={{ id: l.id }}>
                <h3 className="text-lg font-medium hover:text-primary">{l.title}</h3>
              </Link>
              <div className="mt-1 text-xs text-muted-foreground">
                {l.authors.join(", ")} · {l.journal} · {l.year} · 引用 {l.citations}
                {l.documentType ? ` · ${l.documentType}` : ""}
              </div>
              <div className="mt-2 flex flex-wrap gap-1.5">
                {l.keywords.map((k) => <Badge key={k} variant="secondary">{k}</Badge>)}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {total > 0 && (
        <div className="mt-4 flex items-center justify-between text-sm text-muted-foreground">
          <span>共 {total} 篇</span>
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
      )}
    </AppShell>
  );
}
