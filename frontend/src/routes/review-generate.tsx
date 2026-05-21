import { createFileRoute } from "@tanstack/react-router";
import { requireAuth } from "@/lib/guards";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Badge } from "@/components/ui/badge";
import { useEffect, useState } from "react";
import { literatureApi } from "@/api/literature";
import { reviewApi } from "@/api/review";
import type { Literature } from "@/mock/literatures";
import type { Review } from "@/mock/reviews";
import { Sparkles } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/review-generate")({
  beforeLoad: ({ location }) => { requireAuth(location.href); },
  component: ReviewGeneratePage,
});

function ReviewGeneratePage() {
  const [topic, setTopic] = useState("");
  const [pool, setPool] = useState<Literature[]>([]);
  const [selected, setSelected] = useState<string[]>([]);
  const [result, setResult] = useState<Review | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    literatureApi.search({ pageSize: 20 }).then((r) => setPool(r.list));
  }, []);

  const toggle = (id: string) =>
    setSelected((s) => (s.includes(id) ? s.filter((x) => x !== id) : [...s, id]));

  const generate = async () => {
    if (!topic.trim()) return toast.error("请输入综述主题");
    if (selected.length === 0) return toast.error("请至少选择一篇文献");
    setLoading(true);
    try {
      const r = await reviewApi.generate({ topic, literatureIds: selected });
      setResult(r);
      toast.success("综述生成成功");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppShell>
      <div className="grid gap-6 lg:grid-cols-2">
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
              <Input value={topic} onChange={(e) => setTopic(e.target.value)} placeholder="例如：深度学习在医学影像中的应用" />
            </div>
            <div>
              <Label className="text-xs">选择参考文献 ({selected.length} 已选)</Label>
              <div className="mt-2 max-h-96 space-y-2 overflow-y-auto rounded-md border p-3">
                {pool.map((l) => (
                  <label key={l.id} className="flex cursor-pointer items-start gap-3 rounded-md p-2 hover:bg-secondary/60">
                    <Checkbox checked={selected.includes(l.id)} onCheckedChange={() => toggle(l.id)} />
                    <div className="flex-1">
                      <div className="text-sm font-medium">{l.title}</div>
                      <div className="text-xs text-muted-foreground">{l.authors.join(", ")} · {l.year}</div>
                    </div>
                  </label>
                ))}
              </div>
            </div>
            <Button className="w-full" onClick={generate} disabled={loading}>
              {loading ? "正在生成..." : "一键生成综述"}
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>生成结果</CardTitle>
          </CardHeader>
          <CardContent>
            {!result ? (
              <p className="py-12 text-center text-sm text-muted-foreground">填写主题并选择文献后点击生成</p>
            ) : (
              <div>
                <h3 className="text-lg font-semibold">{result.topic}</h3>
                <p className="mt-1 text-xs text-muted-foreground">生成时间：{result.createdAt}</p>
                <div className="mt-4 whitespace-pre-wrap rounded-md bg-secondary/40 p-4 text-sm leading-relaxed">
                  {result.content}
                </div>
                <div className="mt-4">
                  <div className="text-xs font-medium text-muted-foreground">参考来源</div>
                  <div className="mt-2 flex flex-wrap gap-1.5">
                    {result.references.map((id) => {
                      const lit = pool.find((l) => l.id === id);
                      return <Badge key={id} variant="secondary">{lit?.title ?? id}</Badge>;
                    })}
                  </div>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </AppShell>
  );
}
