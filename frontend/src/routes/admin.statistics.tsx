import { createFileRoute } from "@tanstack/react-router";
import { requireAdmin } from "@/lib/guards";
import { AdminShell } from "@/components/layout/AdminShell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useEffect, useState } from "react";
import { adminApi } from "@/api/admin";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

export const Route = createFileRoute("/admin/statistics")({
  beforeLoad: ({ location }) => { requireAdmin(location.href); },
  component: AdminStatisticsPage,
});

function AdminStatisticsPage() {
  const [data, setData] = useState<{
    userCount: number; literatureCount: number; reviewCount: number; categoryCount: number;
    recentTrend: { date: string; value: number }[];
  } | null>(null);

  useEffect(() => { adminApi.getStatistics().then(setData); }, []);
  if (!data) return <AdminShell><p>加载中...</p></AdminShell>;

  const stats = [
    { label: "用户总数", value: data.userCount },
    { label: "文献总数", value: data.literatureCount },
    { label: "分类数量", value: data.categoryCount },
    { label: "综述生成", value: data.reviewCount },
  ];

  return (
    <AdminShell>
      <h1 className="mb-6 text-2xl font-semibold">数据统计</h1>
      <div className="grid gap-4 md:grid-cols-4">
        {stats.map((s) => (
          <Card key={s.label}>
            <CardContent className="pt-6">
              <div className="text-sm text-muted-foreground">{s.label}</div>
              <div className="mt-2 text-3xl font-semibold text-primary">{s.value.toLocaleString()}</div>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card className="mt-6">
        <CardHeader><CardTitle className="text-base">近 7 日检索趋势</CardTitle></CardHeader>
        <CardContent>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data.recentTrend}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis dataKey="date" stroke="hsl(var(--muted-foreground))" fontSize={12} />
                <YAxis stroke="hsl(var(--muted-foreground))" fontSize={12} />
                <Tooltip />
                <Bar dataKey="value" fill="oklch(0.55 0.15 245)" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>
    </AdminShell>
  );
}
