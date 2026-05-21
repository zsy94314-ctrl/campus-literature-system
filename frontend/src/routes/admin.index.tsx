import { createFileRoute } from "@tanstack/react-router";
import { requireAdmin } from "@/lib/guards";
import { AdminShell } from "@/components/layout/AdminShell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useEffect, useState } from "react";
import { adminApi } from "@/api/admin";
import { Users, FileText, Search, Sparkles } from "lucide-react";

export const Route = createFileRoute("/admin/")({
  beforeLoad: ({ location }) => { requireAdmin(location.href); },
  component: AdminHomePage,
});

function AdminHomePage() {
  const [stats, setStats] = useState({ userCount: 0, literatureCount: 0, searchCount: 0, reviewCount: 0 });

  useEffect(() => {
    adminApi.getStatistics().then(setStats);
  }, []);

  const cards = [
    { label: "用户总数", value: stats.userCount, icon: Users, color: "text-blue-500" },
    { label: "文献总数", value: stats.literatureCount, icon: FileText, color: "text-green-500" },
    { label: "检索次数", value: stats.searchCount, icon: Search, color: "text-amber-500" },
    { label: "综述生成", value: stats.reviewCount, icon: Sparkles, color: "text-violet-500" },
  ];

  return (
    <AdminShell>
      <h1 className="mb-6 text-2xl font-semibold">管理首页</h1>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {cards.map((c) => {
          const Icon = c.icon;
          return (
            <Card key={c.label}>
              <CardContent className="flex items-center justify-between pt-6">
                <div>
                  <div className="text-sm text-muted-foreground">{c.label}</div>
                  <div className="mt-1 text-2xl font-semibold">{c.value.toLocaleString()}</div>
                </div>
                <Icon className={`h-8 w-8 ${c.color}`} />
              </CardContent>
            </Card>
          );
        })}
      </div>

      <Card className="mt-6">
        <CardHeader><CardTitle className="text-base">系统概览</CardTitle></CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          欢迎使用校园学术文献智能检索与综述生成系统的管理后台。您可以在左侧菜单中管理文献、用户、分类，并查看详细的数据统计。
        </CardContent>
      </Card>
    </AdminShell>
  );
}
