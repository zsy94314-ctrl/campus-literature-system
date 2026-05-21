import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { requireAuth } from "@/lib/guards";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { useEffect, useState } from "react";
import { authApi } from "@/api/auth";
import { authStore } from "@/lib/auth-store";
import type { User } from "@/mock/users";
import { toast } from "sonner";

export const Route = createFileRoute("/profile")({
  beforeLoad: ({ location }) => { requireAuth(location.href); },
  component: ProfilePage,
});

function ProfilePage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const u = authStore.getUser();
    if (u) setUser(u);
    else authApi.getCurrentUser().then(setUser);
  }, []);

  if (!user) return <AppShell><p>加载中...</p></AppShell>;

  return (
    <AppShell>
      <div className="grid gap-6 lg:grid-cols-3">
        <Card>
          <CardContent className="flex flex-col items-center gap-3 pt-6">
            <div className="flex h-20 w-20 items-center justify-center rounded-full bg-primary text-2xl font-semibold text-primary-foreground">
              {user.username[0].toUpperCase()}
            </div>
            <div className="text-lg font-medium">{user.username}</div>
            <Badge variant="secondary">{user.role === "admin" ? "管理员" : "普通用户"}</Badge>
            <Button
              variant="outline"
              className="w-full"
              onClick={() => {
                authStore.clear();
                toast.success("已退出登录");
                navigate({ to: "/login" });
              }}
            >
              退出登录
            </Button>
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>账户信息</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label className="text-xs">用户名</Label>
                <Input value={user.username} readOnly />
              </div>
              <div className="space-y-1.5">
                <Label className="text-xs">邮箱</Label>
                <Input value={user.email} readOnly />
              </div>
              <div className="space-y-1.5">
                <Label className="text-xs">角色</Label>
                <Input value={user.role} readOnly />
              </div>
              <div className="space-y-1.5">
                <Label className="text-xs">注册时间</Label>
                <Input value={user.createdAt} readOnly />
              </div>
            </div>
            <Button>保存修改</Button>
          </CardContent>
        </Card>
      </div>
    </AppShell>
  );
}
