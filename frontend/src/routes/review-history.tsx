import { createFileRoute } from "@tanstack/react-router";
import { requireAuth } from "@/lib/guards";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { reviewApi, formatReviewTitle } from "@/api/review";
import type { Review } from "@/mock/reviews";
import { Trash2 } from "lucide-react";

export const Route = createFileRoute("/review-history")({
  beforeLoad: ({ location }) => { requireAuth(location.href); },
  component: ReviewHistoryPage,
});

function ReviewHistoryPage() {
  const [list, setList] = useState<Review[]>([]);
  const [open, setOpen] = useState<string | null>(null);
  const load = () => reviewApi.list().then(setList);
  useEffect(() => { load(); }, []);

  return (
    <AppShell>
      <h1 className="mb-4 text-2xl font-semibold">综述记录</h1>
      <div className="space-y-3">
        {list.map((r) => (
          <Card key={r.id}>
            <CardContent className="pt-6">
              <div className="flex items-start justify-between gap-3">
                <button className="flex-1 text-left" onClick={() => setOpen(open === r.id ? null : r.id)}>
                  <div className="text-base font-medium">《{formatReviewTitle(r.topic)}》</div>
                  <div className="mt-1 text-xs text-muted-foreground">{r.createdAt} · 参考 {r.references.length} 篇文献</div>
                </button>
                <Button variant="ghost" size="icon" onClick={async () => { await reviewApi.remove(r.id); load(); }}>
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
              {open === r.id && (
                <div className="mt-4 whitespace-pre-wrap rounded-md bg-secondary/40 p-4 text-sm leading-relaxed">
                  {r.content}
                </div>
              )}
            </CardContent>
          </Card>
        ))}
      </div>
    </AppShell>
  );
}
