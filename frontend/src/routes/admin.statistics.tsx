import { createFileRoute } from "@tanstack/react-router";
import { requireAdmin } from "@/lib/guards";
import { AdminShell } from "@/components/layout/AdminShell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useEffect, useState } from "react";
import { adminApi, type StatisticsData } from "@/api/admin";
import {
  Users,
  FileText,
  Tags,
  Heart,
  Sparkles,
  Search,
  Settings,
  BrainCircuit,
  BarChart3,
  PieChart as PieChartIcon,
  Calendar,
  Activity,
  AlertCircle,
} from "lucide-react";
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts";

export const Route = createFileRoute("/admin/statistics")({
  beforeLoad: ({ location }) => {
    requireAdmin(location.href);
  },
  component: AdminStatisticsPage,
});

const CHART_COLORS = [
  "#3b82f6",
  "#10b981",
  "#8b5cf6",
  "#f59e0b",
  "#ef4444",
  "#06b6d4",
  "#84cc16",
  "#f97316",
  "#ec4899",
  "#6366f1",
];

function EmptyChart({ message = "暂无统计数据" }: { message?: string }) {
  return (
    <div className="flex h-64 flex-col items-center justify-center text-muted-foreground">
      <AlertCircle className="mb-2 h-8 w-8 opacity-50" />
      <p className="text-sm">{message}</p>
    </div>
  );
}

function AdminStatisticsPage() {
  const [data, setData] = useState<StatisticsData | null>(null);

  useEffect(() => {
    adminApi.getStatistics().then(setData);
  }, []);

  if (!data) {
    return (
      <AdminShell>
        <p>加载中...</p>
      </AdminShell>
    );
  }

  const coreStats = [
    {
      label: "用户总数",
      value: data.userCount,
      icon: Users,
      color: "text-blue-500",
    },
    {
      label: "文献总数",
      value: data.literatureCount,
      icon: FileText,
      color: "text-green-500",
    },
    {
      label: "分类数量",
      value: data.categoryCount,
      icon: Tags,
      color: "text-amber-500",
    },
    {
      label: "收藏总数",
      value: data.favoriteCount,
      icon: Heart,
      color: "text-red-500",
    },
    {
      label: "综述生成",
      value: data.reviewCount,
      icon: Sparkles,
      color: "text-violet-500",
    },
    {
      label: "检索记录",
      value: data.searchHistoryCount,
      icon: Search,
      color: "text-cyan-500",
    },
    {
      label: "LLM 配置",
      value: data.llmConfigCount,
      icon: Settings,
      color: "text-slate-500",
    },
    {
      label: "当前 LLM",
      value: data.activeLlmName ? "已启用" : "未配置",
      icon: BrainCircuit,
      color: data.activeLlmName ? "text-emerald-500" : "text-gray-400",
      isText: true,
    },
  ];

  return (
    <AdminShell>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold">数据统计</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          基于系统真实数据的文献资源、用户行为与综述生成统计
        </p>
      </div>

      {/* 核心指标卡片 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {coreStats.map((s) => {
          const Icon = s.icon;
          return (
            <Card key={s.label}>
              <CardContent className="flex items-center justify-between pt-6">
                <div>
                  <div className="text-sm text-muted-foreground">
                    {s.label}
                  </div>
                  <div className="mt-1 text-2xl font-semibold">
                    {s.isText
                      ? s.value
                      : (s.value as number).toLocaleString()}
                  </div>
                </div>
                <Icon className={`h-8 w-8 ${s.color}`} />
              </CardContent>
            </Card>
          );
        })}
      </div>

      {/* 图表区 */}
      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        {/* 分类 Top 10 */}
        <Card>
          <CardHeader className="flex flex-row items-center gap-2">
            <BarChart3 className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-base">Top 10 分类文献数量</CardTitle>
          </CardHeader>
          <CardContent>
            {data.categoryTop.length === 0 ? (
              <EmptyChart />
            ) : (
              <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={data.categoryTop}
                    layout="vertical"
                    margin={{ left: 20, right: 20, top: 10, bottom: 10 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                    <XAxis type="number" stroke="hsl(var(--muted-foreground))" fontSize={12} />
                    <YAxis
                      dataKey="name"
                      type="category"
                      width={100}
                      stroke="hsl(var(--muted-foreground))"
                      fontSize={12}
                      tick={{ fill: "hsl(var(--foreground))" }}
                    />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "hsl(var(--card))",
                        border: "1px solid hsl(var(--border))",
                        borderRadius: "6px",
                      }}
                    />
                    <Bar dataKey="count" fill="#3b82f6" radius={[0, 6, 6, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 文献年份分布 */}
        <Card>
          <CardHeader className="flex flex-row items-center gap-2">
            <Calendar className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-base">文献年份分布</CardTitle>
          </CardHeader>
          <CardContent>
            {data.yearDistribution.length === 0 ? (
              <EmptyChart />
            ) : (
              <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={[...data.yearDistribution].reverse()}
                    margin={{ left: 10, right: 20, top: 10, bottom: 10 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                    <XAxis
                      dataKey="year"
                      stroke="hsl(var(--muted-foreground))"
                      fontSize={12}
                    />
                    <YAxis stroke="hsl(var(--muted-foreground))" fontSize={12} />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "hsl(var(--card))",
                        border: "1px solid hsl(var(--border))",
                        borderRadius: "6px",
                      }}
                    />
                    <Bar dataKey="count" fill="#10b981" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 文献类型分布 */}
        <Card>
          <CardHeader className="flex flex-row items-center gap-2">
            <PieChartIcon className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-base">文献类型分布</CardTitle>
          </CardHeader>
          <CardContent>
            {data.documentTypeDistribution.length === 0 ? (
              <EmptyChart />
            ) : (
              <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={data.documentTypeDistribution}
                      dataKey="count"
                      nameKey="type"
                      cx="50%"
                      cy="50%"
                      outerRadius={90}
                      label={({ type, percent }) =>
                        `${type}: ${(percent * 100).toFixed(0)}%`
                      }
                    >
                      {data.documentTypeDistribution.map((_, index) => (
                        <Cell
                          key={`cell-${index}`}
                          fill={CHART_COLORS[index % CHART_COLORS.length]}
                        />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "hsl(var(--card))",
                        border: "1px solid hsl(var(--border))",
                        borderRadius: "6px",
                      }}
                    />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 综述生成方式分布 */}
        <Card>
          <CardHeader className="flex flex-row items-center gap-2">
            <Activity className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-base">综述生成方式分布</CardTitle>
          </CardHeader>
          <CardContent>
            {data.reviewModeDistribution.length === 0 ? (
              <EmptyChart />
            ) : (
              <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={data.reviewModeDistribution}
                      dataKey="count"
                      nameKey="label"
                      cx="50%"
                      cy="50%"
                      innerRadius={50}
                      outerRadius={90}
                      paddingAngle={4}
                      label={({ label, percent }) =>
                        `${label}: ${(percent * 100).toFixed(0)}%`
                      }
                    >
                      {data.reviewModeDistribution.map((_, index) => (
                        <Cell
                          key={`cell-mode-${index}`}
                          fill={CHART_COLORS[index % CHART_COLORS.length]}
                        />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "hsl(var(--card))",
                        border: "1px solid hsl(var(--border))",
                        borderRadius: "6px",
                      }}
                    />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* 检索类型分布 + LLM 状态 */}
      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader className="flex flex-row items-center gap-2">
            <Search className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-base">检索类型分布</CardTitle>
          </CardHeader>
          <CardContent>
            {data.searchTypeDistribution.length === 0 ? (
              <EmptyChart />
            ) : (
              <div className="space-y-3">
                {data.searchTypeDistribution.map((item) => {
                  const total = data.searchTypeDistribution.reduce(
                    (sum, i) => sum + i.count,
                    0
                  );
                  const percent = total > 0 ? (item.count / total) * 100 : 0;
                  return (
                    <div key={item.type}>
                      <div className="flex items-center justify-between text-sm">
                        <span className="font-medium">{item.label}</span>
                        <span className="text-muted-foreground">
                          {item.count.toLocaleString()} 次 ({percent.toFixed(1)}%)
                        </span>
                      </div>
                      <div className="mt-1 h-2 w-full overflow-hidden rounded-full bg-secondary">
                        <div
                          className="h-full rounded-full bg-blue-500 transition-all"
                          style={{ width: `${percent}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center gap-2">
            <BrainCircuit className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-base">当前 LLM API 状态</CardTitle>
          </CardHeader>
          <CardContent>
            {data.activeLlmName ? (
              <div className="space-y-3 text-sm">
                <div className="flex items-center justify-between rounded-lg border p-3">
                  <span className="text-muted-foreground">配置名称</span>
                  <span className="font-medium">{data.activeLlmName}</span>
                </div>
                <div className="flex items-center justify-between rounded-lg border p-3">
                  <span className="text-muted-foreground">Provider</span>
                  <span className="font-medium">{data.activeLlmProvider || "-"}</span>
                </div>
                <div className="flex items-center justify-between rounded-lg border p-3">
                  <span className="text-muted-foreground">Model</span>
                  <span className="font-medium">{data.activeLlmModel || "-"}</span>
                </div>
                <div className="flex items-center justify-between rounded-lg border p-3">
                  <span className="text-muted-foreground">启用状态</span>
                  <span
                    className={`font-medium ${
                      data.activeLlmEnabled
                        ? "text-green-600"
                        : "text-amber-600"
                    }`}
                  >
                    {data.activeLlmEnabled ? "已启用" : "已禁用"}
                  </span>
                </div>
              </div>
            ) : (
              <div className="flex h-40 flex-col items-center justify-center text-muted-foreground">
                <AlertCircle className="mb-2 h-8 w-8 opacity-50" />
                <p className="text-sm">未配置当前启用的 LLM API</p>
                <p className="mt-1 text-xs">
                  请前往 LLM API 管理页面添加并激活配置
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </AdminShell>
  );
}
