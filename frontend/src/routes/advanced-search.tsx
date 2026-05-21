import { createFileRoute, Link } from "@tanstack/react-router";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useState } from "react";
import { literatureApi } from "@/api/literature";
import type { Literature } from "@/mock/literatures";

export const Route = createFileRoute("/advanced-search")({
  component: AdvancedSearchPage,
});

function AdvancedSearchPage() {
  const [form, setForm] = useState({
    title: "", keyword: "", author: "", journal: "", doi: "",
    yearFrom: "", yearTo: "",
  });
  const [list, setList] = useState<Literature[]>([]);
  const [loading, setLoading] = useState(false);

  const runSearch = async () => {
    setLoading(true);
    const res = await literatureApi.advancedSearch({
      title: form.title || undefined,
      keyword: form.keyword || undefined,
      author: form.author || undefined,
      journal: form.journal || undefined,
      doi: form.doi || undefined,
      yearFrom: form.yearFrom ? Number(form.yearFrom) : undefined,
      yearTo: form.yearTo ? Number(form.yearTo) : undefined,
    });
    setList(res.list);
    setLoading(false);
  };

  const fields: { key: keyof typeof form; label: string; placeholder?: string }[] = [
    { key: "title", label: "标题" },
    { key: "keyword", label: "关键词" },
    { key: "author", label: "作者" },
    { key: "journal", label: "期刊" },
    { key: "doi", label: "DOI" },
  ];

  return (
    <AppShell>
      <Card>
        <CardHeader>
          <CardTitle>高级检索</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2">
            {fields.map((f) => (
              <div key={f.key} className="space-y-1.5">
                <Label className="text-xs">{f.label}</Label>
                <Input
                  value={form[f.key]}
                  onChange={(e) => setForm({ ...form, [f.key]: e.target.value })}
                />
              </div>
            ))}
            <div className="space-y-1.5">
              <Label className="text-xs">起始年份</Label>
              <Input value={form.yearFrom} onChange={(e) => setForm({ ...form, yearFrom: e.target.value })} />
            </div>
            <div className="space-y-1.5">
              <Label className="text-xs">截止年份</Label>
              <Input value={form.yearTo} onChange={(e) => setForm({ ...form, yearTo: e.target.value })} />
            </div>
          </div>
          <div className="mt-4 flex justify-end gap-2">
            <Button variant="outline" onClick={() => setForm({ title: "", keyword: "", author: "", journal: "", doi: "", yearFrom: "", yearTo: "" })}>重置</Button>
            <Button onClick={runSearch}>检索</Button>
          </div>
        </CardContent>
      </Card>

      <div className="mt-4 space-y-3">
        {loading && <p className="py-8 text-center text-sm text-muted-foreground">加载中...</p>}
        {!loading && list.map((l) => (
          <Card key={l.id}>
            <CardContent className="pt-6">
              <Link to="/literature/$id" params={{ id: l.id }}>
                <h3 className="text-lg font-medium hover:text-primary">{l.title}</h3>
              </Link>
              <div className="mt-1 text-xs text-muted-foreground">
                {l.authors.join(", ")} · {l.journal} · {l.year}
              </div>
              <div className="mt-2 flex flex-wrap gap-1.5">
                {l.keywords.map((k) => <Badge key={k} variant="secondary">{k}</Badge>)}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </AppShell>
  );
}
