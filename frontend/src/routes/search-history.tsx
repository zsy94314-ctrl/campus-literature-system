import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { requireAuth } from "@/lib/guards";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { searchHistoryApi, type SearchHistoryItem } from "@/api/searchHistory";
import { History, X } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/search-history")({
  beforeLoad: ({ location }) => { requireAuth(location.href); },
  component: SearchHistoryPage,
});

function SearchHistoryPage() {
  const navigate = useNavigate();
  const [list, setList] = useState<SearchHistoryItem[]>([]);
  const load = () => searchHistoryApi.list().then(setList);
  useEffect(() => { load(); }, []);

  const handleClear = async () => {
    try {
      await searchHistoryApi.clear();
      setList([]);
      toast.success("已清空全部历史");
    } catch (err: any) {
      toast.error(err?.message || "清空失败");
    }
  };

  const handleRemove = async (id: string) => {
    try {
      await searchHistoryApi.remove(id);
      load();
      toast.success("已删除");
    } catch (err: any) {
      toast.error(err?.message || "删除失败");
    }
  };

  return (
    <AppShell>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-semibold">检索历史</h1>
        <Button variant="outline" onClick={handleClear}>清空全部</Button>
      </div>
      <Card>
        <CardContent className="pt-6">
          {list.length === 0 ? (
            <p className="py-8 text-center text-muted-foreground">暂无历史记录</p>
          ) : (
            <ul className="divide-y">
              {list.map((h) => (
                <li key={h.id} className="flex items-center justify-between py-3">
                  <button
                    className="flex flex-1 items-center gap-3 text-left hover:text-primary"
                    onClick={() => navigate({ to: "/search", search: { q: h.keyword } as never })}
                  >
                    <History className="h-4 w-4 text-muted-foreground shrink-0" />
                    <span className="flex-1 truncate">
                      {h.keyword || <span className="text-muted-foreground">[无关键词]</span>}
                    </span>
                    <span className="shrink-0 rounded bg-secondary px-1.5 py-0.5 text-xs text-muted-foreground">
                      {h.searchType}
                    </span>
                    <span className="shrink-0 text-xs text-muted-foreground">
                      {h.resultCount} 条结果
                    </span>
                    <span className="shrink-0 text-xs text-muted-foreground">{h.time}</span>
                  </button>
                  <Button variant="ghost" size="icon" onClick={() => handleRemove(h.id)}>
                    <X className="h-4 w-4" />
                  </Button>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </AppShell>
  );
}
