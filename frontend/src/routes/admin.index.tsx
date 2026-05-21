import { createFileRoute, Link } from "@tanstack/react-router";
import { requireAdmin } from "@/lib/guards";
import { AdminShell } from "@/components/layout/AdminShell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { adminApi } from "@/api/admin";
import { literatureApi } from "@/api/literature";
import type { Literature } from "@/mock/literatures";
import { Users, FileText, Sparkles, Tags, ArrowRight } from "lucide-react";

export const Route = createFileRoute("/admin/")({
  beforeLoad: ({ location }) => { requireAdmin(location.href); },
  component: AdminHomePage,
});

function AdminHomePage() {
  const [stats, setStats] = useState({
    userCount: 0,
    literatureCount: 0,
    reviewCount: 0,
    categoryCount: 0,
  });
  const [recent, setRecent] = useState<Literature[]>([]);

  useEffect(() => {
    adminApi.getStatistics().then(setStats);
    literatureApi.search({ page: 1, pageSize: 5 }).then((r) => setRecent(r.list));
  }, []);

  const statCards = [
    { label: "用户总数", value: stats.userCount, icon: Users, color: "text-blue-500" },
    { label: "文献总数", value: stats.literatureCount, icon: FileText, color: "text-green-500" },
    { label: "综述生成", value: stats.reviewCount, icon: Sparkles, color: "text-violet-500" },
    { label: "分类数量", value: stats.categoryCount, icon: Tags, color: "text-amber-500" },
  ];

  const quickLinks = [
    { to: "/admin/literatures", label: "文献管理", desc: "新增、编辑、删除文献" },
    { to: "/admin/users", label: "用户管理", desc: "查看与管理注册用户" },
    { to: "/admin/categories", label: "分类管理", desc: "维护学科分类体系" },
    { to: "/admin/statistics", label: "数据统计", desc: "查看平台运营数据" },
  ];

  return (
    <AdminShell>
      <h1 className="mb-6 text-2xl font-semibold">管理首页</h1>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {statCards.map((c) => {
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

      <div className="mt-6 grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-base">最近文献</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {recent.length === 0 ? (
              <p className="text-sm text-muted-foreground">暂无文献</p>
            ) : (
              recent.map((l) => (
                <div key={l.id} className="flex items-center justify-between rounded-lg border p-3">
                  <div className="min-w-0">
                    <div className="truncate font-medium text-foreground">{l.title}</div>
                    <div className="mt-0.5 text-xs text-muted-foreground">
                      {l.authors.join(", ")} · {l.year}
                    </div>
                  </div>
                  <Button asChild variant="ghost" size="sm">
                    <Link to="/literature/$id" params={{ id: l.id }}>
                      查看
                    </Link>
                  </Button>
                </div>
              ))
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">快捷操作</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {quickLinks.map((q) => (
              <Link
                key={q.to}
                to={q.to}
                className="flex items-center justify-between rounded-md border p-3 text-sm transition-colors hover:bg-secondary"
              >
                <div>
                  <div className="font-medium text-foreground">{q.label}</div>
                  <div className="text-xs text-muted-foreground">{q.desc}</div>
                </div>
                <ArrowRight className="h-4 w-4 text-muted-foreground" />
              </Link>
            ))}
          </CardContent>
        </Card>
      </div>
    </AdminShell>
  );
}
