import { Link, useLocation, useNavigate } from "@tanstack/react-router";
import { authStore } from "@/lib/auth-store";
import { Button } from "@/components/ui/button";
import { LayoutDashboard, FileText, Users, Tags, BarChart3, LogOut, ArrowLeft, BrainCircuit } from "lucide-react";
import { ThemeToggle } from "@/components/ThemeToggle";
import { useEffect, useState } from "react";

const items = [
  { to: "/admin", label: "管理首页", icon: LayoutDashboard },
  { to: "/admin/literatures", label: "文献管理", icon: FileText },
  { to: "/admin/categories", label: "分类管理", icon: Tags },
  { to: "/admin/users", label: "用户管理", icon: Users },
  { to: "/admin/statistics", label: "数据统计", icon: BarChart3 },
  { to: "/admin/llm-configs", label: "LLM API 管理", icon: BrainCircuit },
];

export function AdminShell({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  const navigate = useNavigate();
  const [mounted, setMounted] = useState(false);
  const [username, setUsername] = useState<string>("");

  useEffect(() => {
    setMounted(true);
    const user = authStore.getUser();
    setUsername(user?.username || "");
  }, []);

  const handleLogout = () => {
    authStore.clear();
    navigate({ to: "/login" });
  };

  return (
    <div className="flex min-h-screen bg-background">
      <aside className="flex w-60 shrink-0 flex-col border-r bg-card">
        <div className="flex h-16 items-center gap-2 border-b px-6">
          <div className="flex h-8 w-8 items-center justify-center rounded-md bg-primary text-primary-foreground">
            <LayoutDashboard className="h-4 w-4" />
          </div>
          <span className="font-semibold">管理后台</span>
        </div>
        <nav className="flex flex-1 flex-col gap-1 p-3">
          {items.map((item) => {
            const Icon = item.icon;
            const active = location.pathname === item.to;
            return (
              <Link
                key={item.to}
                to={item.to}
                className={`flex items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors ${
                  active
                    ? "bg-primary text-primary-foreground"
                    : "text-muted-foreground hover:bg-secondary hover:text-foreground"
                }`}
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>
        <div className="border-t p-3">
          <Button asChild variant="outline" size="sm" className="w-full">
            <Link to="/home">
              <ArrowLeft className="mr-2 h-3 w-3" />
              返回前台
            </Link>
          </Button>
        </div>
      </aside>
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex h-16 items-center justify-between border-b bg-card px-6">
          <h1 className="text-sm font-medium text-muted-foreground">校园学术文献智能检索平台 · 管理控制台</h1>
          <div className="flex items-center gap-2">
            <ThemeToggle />
            <span className="text-sm text-foreground">{mounted ? username : "管理员"}</span>
            <Button variant="ghost" size="icon" onClick={handleLogout}>
              <LogOut className="h-4 w-4" />
            </Button>
          </div>
        </header>
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  );
}
