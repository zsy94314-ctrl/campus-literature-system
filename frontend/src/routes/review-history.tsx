import { createFileRoute } from "@tanstack/react-router";
import { requireAuth } from "@/lib/guards";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useEffect, useMemo, useState } from "react";
import { reviewApi, formatReviewTitle } from "@/api/review";
import type { Review } from "@/mock/reviews";
import { Trash2 } from "lucide-react";

export const Route = createFileRoute("/review-history")({
  beforeLoad: ({ location }) => { requireAuth(location.href); },
  component: ReviewHistoryPage,
});

const MODE_META: Record<string, { label: string; variant: "default" | "secondary" | "destructive" | "outline" }> = {
  rule: { label: "离线综述生成", variant: "secondary" },
  llm: { label: "在线 LLM 综述生成", variant: "default" },
  llm_fallback_rule: { label: "LLM 降级离线生成", variant: "outline" },
};

function getModeMeta(mode?: string) {
  if (!mode) return MODE_META.rule;
  return MODE_META[mode] || MODE_META.rule;
}

const FILTERS = [
  { key: "all", label: "全部" },
  { key: "rule", label: "离线综述生成" },
  { key: "llm", label: "在线 LLM 综述生成" },
  { key: "llm_fallback_rule", label: "LLM 降级离线生成" },
];

function ReviewHistoryPage() {
  const [list, setList] = useState<Review[]>([]);
  const [open, setOpen] = useState<string | null>(null);
  const [filter, setFilter] = useState<string>("all");
  const load = () => reviewApi.list().then(setList);
  useEffect(() => { load(); }, []);

  const filteredList = useMemo(() => {
    if (filter === "all") return list;
    return list.filter((r) => (r.generationMode || "rule") === filter);
  }, [list, filter]);

  return (
    <AppShell>
      <h1 className="mb-4 text-2xl font-semibold">综述记录</h1>

      <div className="mb-4 flex flex-wrap gap-2">
        {FILTERS.map((f) => (
          <Button
            key={f.key}
            size="sm"
            variant={filter === f.key ? "default" : "outline"}
            onClick={() => setFilter(f.key)}
          >
            {f.label}
          </Button>
        ))}
      </div>

      <div className="space-y-3">
        {filteredList.map((r) => {
          const meta = getModeMeta(r.generationMode);
          return (
            <Card key={r.id}>
              <CardContent className="pt-6">
                <div className="flex items-start justify-between gap-3">
                  <button className="flex-1 text-left" onClick={() => setOpen(open === r.id ? null : r.id)}>
                    <div className="flex items-center gap-2">
                      <div className="text-base font-medium">《{formatReviewTitle(r.topic)}》</div>
                      <Badge variant={meta.variant} className="text-[10px]">
                        {meta.label}
                      </Badge>
                    </div>
                    <div className="mt-1 text-xs text-muted-foreground">
                      {r.createdAt} · 参考 {r.references.length} 篇文献
                    </div>
                  </button>
                  <Button variant="ghost" size="icon" onClick={async () => { await reviewApi.remove(r.id); load(); }}>
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
                {open === r.id && (
                  <div className="mt-4 space-y-2">
                    <div className="flex items-center gap-2 text-xs text-muted-foreground">
                      <span>生成方式：</span>
                      <Badge variant={meta.variant} className="text-[10px]">
                        {meta.label}
                      </Badge>
                    </div>
                    <div className="whitespace-pre-wrap rounded-md bg-secondary/40 p-4 text-sm leading-relaxed">
                      {r.content}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          );
        })}
        {filteredList.length === 0 && (
          <p className="py-12 text-center text-sm text-muted-foreground">
            {filter === "all" ? "暂无综述记录" : "该筛选条件下暂无记录"}
          </p>
        )}
      </div>
    </AppShell>
  );
}
