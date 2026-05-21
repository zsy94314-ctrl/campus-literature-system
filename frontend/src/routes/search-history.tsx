import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { requireAuth } from "@/lib/guards";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { searchHistoryApi, type SearchHistoryItem } from "@/api/searchHistory";
import { History, X } from "lucide-react";

export const Route = createFileRoute("/search-history")({
  beforeLoad: ({ location }) => { requireAuth(location.href); },
  component: SearchHistoryPage,
});

function SearchHistoryPage() {
  const navigate = useNavigate();
  const [list, setList] = useState<SearchHistoryItem[]>([]);
  const load = () => searchHistoryApi.list().then(setList);
  useEffect(() => { load(); }, []);

  return (
    <AppShell>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-semibold">检索历史</h1>
        <Button variant="outline" onClick={async () => { await searchHistoryApi.clear(); setList([]); }}>清空全部</Button>
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
                    className="flex items-center gap-3 text-left hover:text-primary"
                    onClick={() => navigate({ to: "/search", search: { q: h.keyword } as never })}
                  >
                    <History className="h-4 w-4 text-muted-foreground" />
                    <span>{h.keyword}</span>
                    <span className="text-xs text-muted-foreground">{h.time}</span>
                  </button>
                  <Button variant="ghost" size="icon" onClick={async () => { await searchHistoryApi.remove(h.id); load(); }}>
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
