import { Link, useNavigate, useLocation } from "@tanstack/react-router";
import { authStore } from "@/lib/auth-store";
import { Button } from "@/components/ui/button";
import { BookOpen, Search, Star, FileText, ClipboardList, User, LogOut } from "lucide-react";
import { ThemeToggle } from "@/components/ThemeToggle";
import { useEffect, useState } from "react";

const userNav = [
  { to: "/home", label: "首页", icon: BookOpen },
  { to: "/search", label: "文献检索", icon: Search },
  { to: "/advanced-search", label: "高级检索", icon: Search },
  { to: "/favorites", label: "我的收藏", icon: Star },

  { to: "/review-generate", label: "综述生成", icon: FileText },
  { to: "/review-history", label: "综述记录", icon: ClipboardList },
  { to: "/profile", label: "个人中心", icon: User },
];

export function AppHeader() {
  const navigate = useNavigate();
  const [mounted, setMounted] = useState(false);
  const [user, setUser] = useState<ReturnType<typeof authStore.getUser>>(null);

  useEffect(() => {
    setMounted(true);
    setUser(authStore.getUser());
  }, []);

  const handleLogout = () => {
    authStore.clear();
    setUser(null);
    navigate({ to: "/login" });
  };

  // 服务端渲染和客户端首次渲染保持一致，避免 Hydration mismatch
  if (!mounted) {
    return (
      <header className="sticky top-0 z-40 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80">
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-6">
          <Link to="/home" className="flex items-center gap-2">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <BookOpen className="h-5 w-5" />
            </div>
            <div className="flex flex-col leading-tight">
              <span className="text-sm font-semibold text-foreground">学术文献智能平台</span>
              <span className="text-xs text-muted-foreground">Scholar AI</span>
            </div>
          </Link>
          <div className="flex items-center gap-2">
            <Button asChild size="sm">
              <Link to="/login">登录</Link>
            </Button>
          </div>
        </div>
      </header>
    );
  }

  const isAdmin = user?.role === "admin";

  return (
    <header className="sticky top-0 z-40 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-6">
        <Link to="/home" className="flex items-center gap-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <BookOpen className="h-5 w-5" />
          </div>
          <div className="flex flex-col leading-tight">
            <span className="text-sm font-semibold text-foreground">学术文献智能平台</span>
            <span className="text-xs text-muted-foreground">Scholar AI</span>
          </div>
        </Link>

        <div className="flex items-center gap-2">
          <ThemeToggle />
          {isAdmin && (
            <Button asChild variant="outline" size="sm">
              <Link to="/admin">管理后台</Link>
            </Button>
          )}
          {user ? (
            <>
              <Button asChild variant="ghost" size="sm">
                <Link to="/profile">{user.username}</Link>
              </Button>
              <Button variant="ghost" size="icon" onClick={handleLogout} title="退出">
                <LogOut className="h-4 w-4" />
              </Button>
            </>
          ) : (
            <Button asChild size="sm">
              <Link to="/login">登录</Link>
            </Button>
          )}
        </div>
      </div>
    </header>
  );
}

export function AppSidebar() {
  const location = useLocation();
  return (
    <aside className="hidden w-56 shrink-0 border-r bg-card/30 lg:block">
      <nav className="sticky top-16 flex flex-col gap-1 p-4">
        {userNav.map((item) => {
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
    </aside>
  );
}

export function AppShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-background">
      <AppHeader />
      <div className="mx-auto flex max-w-7xl">
        <AppSidebar />
        <main className="min-w-0 flex-1 p-6">{children}</main>
      </div>
    </div>
  );
}
