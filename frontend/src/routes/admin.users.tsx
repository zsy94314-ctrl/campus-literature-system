import { createFileRoute } from "@tanstack/react-router";
import { requireAdmin } from "@/lib/guards";
import { AdminShell } from "@/components/layout/AdminShell";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useEffect, useState } from "react";
import { adminApi } from "@/api/admin";
import type { User } from "@/mock/users";
import { toast } from "sonner";

export const Route = createFileRoute("/admin/users")({
  beforeLoad: ({ location }) => { requireAdmin(location.href); },
  component: AdminUsersPage,
});

function AdminUsersPage() {
  const [list, setList] = useState<User[]>([]);
  const load = () => adminApi.listUsers().then(setList);
  useEffect(() => { load(); }, []);

  const toggle = async (u: User) => {
    const next = u.status === "active" ? "disabled" : "active";
    await adminApi.toggleUserStatus(u.id, next);
    toast.success(next === "active" ? "已启用" : "已禁用");
    setList((arr) => arr.map((x) => (x.id === u.id ? { ...x, status: next } : x)));
  };

  return (
    <AdminShell>
      <h1 className="mb-4 text-2xl font-semibold">用户管理</h1>
      <Card>
        <CardContent className="pt-6">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>用户名</TableHead><TableHead>邮箱</TableHead><TableHead>角色</TableHead>
                <TableHead>状态</TableHead><TableHead>注册时间</TableHead><TableHead>操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {list.map((u) => (
                <TableRow key={u.id}>
                  <TableCell>{u.username}</TableCell>
                  <TableCell>{u.email}</TableCell>
                  <TableCell><Badge variant={u.role === "admin" ? "default" : "secondary"}>{u.role}</Badge></TableCell>
                  <TableCell>
                    <Badge variant={u.status === "active" ? "secondary" : "destructive"}>
                      {u.status === "active" ? "正常" : "已禁用"}
                    </Badge>
                  </TableCell>
                  <TableCell>{u.createdAt}</TableCell>
                  <TableCell>
                    <Button variant="outline" size="sm" onClick={() => toggle(u)}>
                      {u.status === "active" ? "禁用" : "启用"}
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </AdminShell>
  );
}
