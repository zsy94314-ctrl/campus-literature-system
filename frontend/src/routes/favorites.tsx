import { createFileRoute, Link } from "@tanstack/react-router";
import { requireAuth } from "@/lib/guards";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { favoriteApi } from "@/api/favorite";
import type { Literature } from "@/mock/literatures";
import { Star } from "lucide-react";

export const Route = createFileRoute("/favorites")({
  beforeLoad: ({ location }) => { requireAuth(location.href); },
  component: FavoritesPage,
});

function FavoritesPage() {
  const [list, setList] = useState<Literature[]>([]);

  const load = () => favoriteApi.list().then(setList);
  useEffect(() => { load(); }, []);

  const remove = async (id: string) => {
    await favoriteApi.remove(id);
    load();
  };

  return (
    <AppShell>
      <div className="mb-4 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">我的收藏</h1>
          <p className="text-sm text-muted-foreground">共 {list.length} 篇</p>
        </div>
      </div>
      {list.length === 0 ? (
        <Card><CardContent className="py-12 text-center text-muted-foreground">暂无收藏</CardContent></Card>
      ) : (
        <div className="space-y-3">
          {list.map((l) => (
            <Card key={l.id}>
              <CardContent className="flex items-start justify-between gap-4 pt-6">
                <div className="flex-1">
                  <Link to="/literature/$id" params={{ id: l.id }} className="text-base font-medium hover:text-primary">
                    {l.title}
                  </Link>
                  <div className="mt-1 text-xs text-muted-foreground">{l.authors.join(", ")} · {l.year}</div>
                </div>
                <Button variant="ghost" size="sm" onClick={() => remove(l.id)}>
                  <Star className="mr-1 h-4 w-4 fill-current" />
                  取消收藏
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </AppShell>
  );
}
