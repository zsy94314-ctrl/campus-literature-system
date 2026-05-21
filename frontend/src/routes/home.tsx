import { createFileRoute, Link } from "@tanstack/react-router";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useMemo, useState } from "react";
import { literatureApi } from "@/api/literature";
import { categoryApi, type Category } from "@/api/category";
import type { Literature } from "@/mock/literatures";
import { Search, TrendingUp, BookMarked, Sparkles } from "lucide-react";
import { useNavigate } from "@tanstack/react-router";

export const Route = createFileRoute("/home")({
  component: HomePage,
});

function HomePage() {
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState("");
  const [hot, setHot] = useState<Literature[]>([]);
  const [cats, setCats] = useState<Category[]>([]);
  const [allLits, setAllLits] = useState<Literature[]>([]);

  useEffect(() => {
    literatureApi.search({ sortBy: "citations", pageSize: 4 }).then((r) => setHot(r.list.slice(0, 4)));
    literatureApi.search({ page: 1, pageSize: 100 }).then((r) => setAllLits(r.list));
    categoryApi.list().then(setCats);
  }, []);

  const catCounts = useMemo(() => {
    const counts: Record<string, number> = {};
    allLits.forEach((l) => {
      if (l.categoryId) {
        counts[l.categoryId] = (counts[l.categoryId] || 0) + 1;
      }
    });
    return counts;
  }, [allLits]);

  return (
    <AppShell>
      <section className="rounded-2xl bg-gradient-to-br from-primary to-primary/70 p-10 text-primary-foreground shadow-sm">
        <h1 className="text-3xl font-semibold">校园学术文献智能检索与综述生成</h1>
        <p className="mt-2 max-w-xl text-sm text-primary-foreground/80">
          智能检索、文献分析、自动综述生成 —— 为高校师生打造的一站式学术助手。
        </p>
        <form
          className="mt-6 flex max-w-2xl gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            navigate({ to: "/search", search: { q: keyword } as never });
          }}
        >
          <Input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="输入关键词、作者或主题..."
            className="h-12 bg-background text-foreground"
          />
          <Button type="submit" size="lg" variant="secondary">
            <Search className="mr-2 h-4 w-4" />
            检索
          </Button>
        </form>
      </section>

      <div className="mt-6 grid gap-4 md:grid-cols-3">
        {[
          { icon: Search, title: "高级检索", desc: "多字段精准过滤", to: "/advanced-search" },
          { icon: Sparkles, title: "综述生成", desc: "AI 辅助一键成文", to: "/review-generate" },
          { icon: BookMarked, title: "我的收藏", desc: "随时回顾重要文献", to: "/favorites" },
        ].map((c) => {
          const Icon = c.icon;
          return (
            <Link to={c.to} key={c.to}>
              <Card className="transition-shadow hover:shadow-md">
                <CardHeader className="flex flex-row items-center gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-secondary text-primary">
                    <Icon className="h-5 w-5" />
                  </div>
                  <div>
                    <CardTitle className="text-base">{c.title}</CardTitle>
                    <CardDescription>{c.desc}</CardDescription>
                  </div>
                </CardHeader>
              </Card>
            </Link>
          );
        })}
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <TrendingUp className="h-4 w-4 text-primary" />
              热门文献
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {hot.map((l) => (
              <Link
                key={l.id}
                to="/literature/$id"
                params={{ id: l.id }}
                className="block rounded-lg border p-4 transition-colors hover:bg-secondary/60"
              >
                <div className="font-medium text-foreground">{l.title}</div>
                <div className="mt-1 text-xs text-muted-foreground">
                  {l.authors.join(", ")} · {l.journal} · {l.year} · 引用 {l.citations}
                </div>
              </Link>
            ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">学科分类</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-wrap gap-2">
            {cats.map((c) => (
              <Link
                key={c.id}
                to="/search"
                search={{ category: c.name } as never}
                className="rounded-full border px-3 py-1 text-xs text-muted-foreground transition-colors hover:bg-secondary hover:text-foreground"
              >
                {c.name} · {catCounts[c.id] || 0}
              </Link>
            ))}
          </CardContent>
        </Card>
      </div>
    </AppShell>
  );
}
