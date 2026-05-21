import { createFileRoute } from "@tanstack/react-router";
import { requireAdmin } from "@/lib/guards";
import { AdminShell } from "@/components/layout/AdminShell";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useEffect, useState } from "react";
import { categoryApi, type Category } from "@/api/category";
import { Trash2, Plus, Pencil, Check, X } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/admin/categories")({
  beforeLoad: ({ location }) => { requireAdmin(location.href); },
  component: AdminCategoriesPage,
});

function AdminCategoriesPage() {
  const [list, setList] = useState<Category[]>([]);
  const [newName, setNewName] = useState("");
  const [editing, setEditing] = useState<{ id: string; name: string } | null>(null);
  const load = () => categoryApi.list().then(setList);
  useEffect(() => { load(); }, []);

  const add = async () => {
    if (!newName.trim()) return;
    const c = await categoryApi.create(newName);
    setList((arr) => [...arr, c]);
    setNewName("");
    toast.success("已添加");
  };
  const save = async () => {
    if (!editing) return;
    await categoryApi.update(editing.id, editing.name);
    setList((arr) => arr.map((c) => (c.id === editing.id ? { ...c, name: editing.name } : c)));
    setEditing(null);
    toast.success("已更新");
  };
  const remove = async (id: string) => {
    await categoryApi.remove(id);
    setList((arr) => arr.filter((c) => c.id !== id));
    toast.success("已删除");
  };

  return (
    <AdminShell>
      <h1 className="mb-4 text-2xl font-semibold">分类管理</h1>
      <Card>
        <CardContent className="pt-6">
          <div className="mb-4 flex gap-2">
            <Input placeholder="新分类名称" value={newName} onChange={(e) => setNewName(e.target.value)} />
            <Button onClick={add}><Plus className="mr-2 h-4 w-4" />添加</Button>
          </div>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead><TableHead>名称</TableHead><TableHead>操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {list.map((c) => (
                <TableRow key={c.id}>
                  <TableCell>{c.id}</TableCell>
                  <TableCell>
                    {editing?.id === c.id ? (
                      <Input value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} className="h-8" />
                    ) : (c.name)}
                  </TableCell>
                  <TableCell className="space-x-1">
                    {editing?.id === c.id ? (
                      <>
                        <Button variant="ghost" size="icon" onClick={save}><Check className="h-4 w-4" /></Button>
                        <Button variant="ghost" size="icon" onClick={() => setEditing(null)}><X className="h-4 w-4" /></Button>
                      </>
                    ) : (
                      <Button variant="ghost" size="icon" onClick={() => setEditing({ id: c.id, name: c.name })}><Pencil className="h-4 w-4" /></Button>
                    )}
                    <Button variant="ghost" size="icon" onClick={() => remove(c.id)}><Trash2 className="h-4 w-4" /></Button>
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
