import { createFileRoute } from "@tanstack/react-router";
import { requireAuth } from "@/lib/guards";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Badge } from "@/components/ui/badge";
import { useState } from "react";
import { literatureApi } from "@/api/literature";
import { reviewApi, formatReviewTitle } from "@/api/review";
import { favoriteApi } from "@/api/favorite";
import type { Literature } from "@/mock/literatures";
import type { Review } from "@/mock/reviews";
import { Sparkles, Search, Heart } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/review-generate")({
  beforeLoad: ({ location }) => { requireAuth(location.href); },
  component: ReviewGeneratePage,
});

function ReviewGeneratePage() {
  const [topic, setTopic] = useState("");
  const [pool, setPool] = useState<Literature[]>([]);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [allLiterature, setAllLiterature] = useState<Record<string, Literature>>({});
  const [result, setResult] = useState<Review | null>(null);
  const [loading, setLoading] = useState(false);
  const [searching, setSearching] = useState(false);

  const selectedPapers = selectedIds.map((id) => allLiterature[id]).filter(Boolean);

  const mergeIntoAll = (items: Literature[]) => {
    setAllLiterature((prev) => {
      const next = { ...prev };
      items.forEach((l) => { next[l.id] = l; });
      return next;
    });
  };

  const searchRelated = async () => {
    if (!topic.trim()) {
      toast.error("请先输入综述主题");
      return;
    }
    setSearching(true);
    try {
      const res = await literatureApi.search({ keyword: topic, page: 1, pageSize: 20 });
      setPool(res.list);
      mergeIntoAll(res.list);
    } finally {
      setSearching(false);
    }
  };

  const loadFavorites = async () => {
    setSearching(true);
    try {
      const res = await favoriteApi.list();
      setPool(res);
      mergeIntoAll(res);
    } finally {
      setSearching(false);
    }
  };

  const toggle = (id: string) =>
    setSelectedIds((s) => (s.includes(id) ? s.filter((x) => x !== id) : [...s, id]));

  const generate = async () => {
    if (!topic.trim()) return toast.error("请先输入综述主题");
    if (selectedIds.length === 0) return toast.error("请选择至少一篇参考文献");
    setLoading(true);
    try {
      const r = await reviewApi.generate({ topic, literatureIds: selectedIds });
      setResult(r);
      toast.success("综述生成成功");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppShell>
      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2 space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Sparkles className="h-4 w-4 text-primary" />
                生成综述
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-1.5">
                <Label>综述主题</Label>
                <Input
                  value={topic}
                  onChange={(e) => setTopic(e.target.value)}
                  placeholder="例如：深度学习在医学影像中的应用"
                />
              </div>
              <div className="flex gap-2">
                <Button variant="outline" onClick={searchRelated} disabled={searching}>
                  <Search className="mr-2 h-4 w-4" />
                  {searching ? "检索中..." : "检索相关文献"}
                </Button>
                <Button variant="outline" onClick={loadFavorites} disabled={searching}>
                  <Heart className="mr-2 h-4 w-4" />
                  加载我的收藏
                </Button>
              </div>
            </CardContent>
          </Card>

          {pool.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">文献列表</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  {pool.map((l) => (
                    <label
                      key={l.id}
                      className="flex cursor-pointer items-start gap-3 rounded-md border p-3 hover:bg-secondary/40"
                    >
                      <Checkbox
                        checked={selectedIds.includes(l.id)}
                        onCheckedChange={() => toggle(l.id)}
                        className="mt-0.5"
                      />
                      <div className="min-w-0 flex-1">
                        <div className="truncate text-sm font-medium">{l.title}</div>
                        <div className="text-xs text-muted-foreground">
                          {l.authors.join(", ")} · {l.year}
                          {l.documentType ? ` · ${l.documentType}` : ""}
                        </div>
                        <div className="mt-1 flex flex-wrap gap-1">
                          {l.keywords.map((k) => (
                            <Badge key={k} variant="outline" className="text-[10px]">
                              {k}
                            </Badge>
                          ))}
                        </div>
                      </div>
                    </label>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}

          {selectedPapers.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">
                  已选择文献（{selectedPapers.length} 篇）
                </CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {selectedPapers.map((l) => (
                    <li
                      key={l.id}
                      className="flex items-start justify-between rounded-md border p-2 text-sm"
                    >
                      <div className="min-w-0">
                        <div className="truncate font-medium">{l.title}</div>
                        <div className="text-xs text-muted-foreground">
                          {l.authors.join(", ")} · {l.year}
                          {l.documentType ? ` · ${l.documentType}` : ""}
                        </div>
                      </div>
                      <Button variant="ghost" size="sm" onClick={() => toggle(l.id)}>
                        移除
                      </Button>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}

          <Button className="w-full" onClick={generate} disabled={loading}>
            {loading ? "正在生成..." : "一键生成综述"}
          </Button>
        </div>

        <div>
          <Card>
            <CardHeader>
              <CardTitle>生成结果</CardTitle>
            </CardHeader>
            <CardContent>
              {!result ? (
                <p className="py-12 text-center text-sm text-muted-foreground">
                  填写主题并选择文献后点击生成
                </p>
              ) : (
                <div>
                  <h3 className="text-lg font-semibold">《{formatReviewTitle(result.topic)}》</h3>
                  <p className="mt-1 text-xs text-muted-foreground">
                    生成时间：{result.createdAt}
                  </p>
                  <div className="mt-4 whitespace-pre-wrap rounded-md bg-secondary/40 p-4 text-sm leading-relaxed">
                    {result.content}
                  </div>
                  <div className="mt-4">
                    <div className="text-xs font-medium text-muted-foreground">参考来源</div>
                    <div className="mt-2 flex flex-wrap gap-1.5">
                      {result.references.map((id) => {
                        const lit = allLiterature[id];
                        return (
                          <Badge key={id} variant="secondary">
                            {lit?.title ?? id}
                          </Badge>
                        );
                      })}
                    </div>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </AppShell>
  );
}
