import { createFileRoute, Link } from "@tanstack/react-router";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useEffect, useState } from "react";
import { literatureApi } from "@/api/literature";
import { favoriteApi } from "@/api/favorite";
import type { Literature } from "@/mock/literatures";
import { Star, ExternalLink } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/literature/$id")({
  component: LiteratureDetailPage,
});

function LiteratureDetailPage() {
  const { id } = Route.useParams();
  const [lit, setLit] = useState<Literature | null>(null);
  const [similar, setSimilar] = useState<Literature[]>([]);
  const [favorited, setFavorited] = useState(false);

  useEffect(() => {
    literatureApi.getById(id).then(setLit);
    literatureApi.getSimilar(id).then(setSimilar);
    favoriteApi.isFavorited(id).then((r) => setFavorited(r.favorited));
  }, [id]);

  const toggleFav = async () => {
    if (favorited) {
      await favoriteApi.remove(id);
      setFavorited(false);
      toast.success("已取消收藏");
    } else {
      await favoriteApi.add(id);
      setFavorited(true);
      toast.success("已加入收藏");
    }
  };

  if (!lit) return <AppShell><p className="text-muted-foreground">加载中...</p></AppShell>;

  return (
    <AppShell>
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1">
              <div className="flex flex-wrap items-center gap-2">
                <Badge variant="secondary">{lit.category}</Badge>
                {lit.documentType && <Badge variant="outline">{lit.documentType}</Badge>}
              </div>
              <h1 className="mt-2 text-2xl font-semibold leading-snug">{lit.title}</h1>
              <p className="mt-2 text-sm text-muted-foreground">
                {lit.authors.join(", ")} · {lit.journal} · {lit.year}
              </p>
            </div>
            <Button variant={favorited ? "default" : "outline"} onClick={toggleFav}>
              <Star className={`mr-2 h-4 w-4 ${favorited ? "fill-current" : ""}`} />
              {favorited ? "已收藏" : "收藏"}
            </Button>
          </div>

          <div className="mt-6 grid grid-cols-3 gap-4 border-y py-4 text-center text-sm">
            <div>
              <div className="text-xs text-muted-foreground">DOI</div>
              <div className="mt-1 font-medium">{lit.doi || "—"}</div>
            </div>
            <div>
              <div className="text-xs text-muted-foreground">引用次数</div>
              <div className="mt-1 font-medium">{lit.citations}</div>
            </div>
            <div>
              <div className="text-xs text-muted-foreground">发表年份</div>
              <div className="mt-1 font-medium">{lit.year}</div>
            </div>
          </div>

          {lit.sourceUrl && (
            <div className="mt-6">
              <h2 className="text-base font-semibold">来源链接</h2>
              <a
                href={lit.sourceUrl}
                target="_blank"
                rel="noreferrer"
                className="mt-2 inline-flex items-center gap-1 text-sm text-primary hover:underline"
              >
                {lit.sourceUrl}
                <ExternalLink className="h-3 w-3" />
              </a>
            </div>
          )}

          <div className="mt-6">
            <h2 className="text-base font-semibold">摘要</h2>
            <p className="mt-2 leading-relaxed text-muted-foreground">{lit.abstract}</p>
          </div>

          {lit.content && (
            <div className="mt-6 rounded-md bg-secondary/40 p-4">
              <h2 className="text-base font-semibold">正文节选</h2>
              <p className="mt-2 leading-relaxed text-muted-foreground">{lit.content}</p>
            </div>
          )}

          <div className="mt-6">
            <h2 className="text-base font-semibold">关键词</h2>
            <div className="mt-2 flex flex-wrap gap-1.5">
              {lit.keywords.map((k) => (
                <Badge key={k} variant="outline">{k}</Badge>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>

      <Card className="mt-6">
        <CardHeader>
          <CardTitle className="text-base">相似文献推荐</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {similar.map((s) => (
            <Link
              key={s.id}
              to="/literature/$id"
              params={{ id: s.id }}
              className="flex items-start justify-between rounded-lg border p-4 hover:bg-secondary/60"
            >
              <div>
                <div className="font-medium">{s.title}</div>
                <div className="mt-1 text-xs text-muted-foreground">{s.authors.join(", ")} · {s.year}</div>
              </div>
              <ExternalLink className="h-4 w-4 text-muted-foreground" />
            </Link>
          ))}
        </CardContent>
      </Card>
    </AppShell>
  );
}
