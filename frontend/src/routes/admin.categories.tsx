import { createFileRoute } from "@tanstack/react-router";
import { requireAdmin } from "@/lib/guards";
import { AdminShell } from "@/components/layout/AdminShell";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useEffect, useMemo, useState } from "react";
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
  const [newParent, setNewParent] = useState("0");
  const [editing, setEditing] = useState<{ id: string; name: string; parentId: string } | null>(null);
  const load = () => categoryApi.list().then(setList);
  useEffect(() => { load(); }, []);

  const topCategories = useMemo(() => list.filter((c) => c.parentId === "0" || c.parentId === undefined), [list]);

  const add = async () => {
    if (!newName.trim()) return;
    const c = await categoryApi.create(newName, newParent);
    setList((arr) => [...arr, c]);
    setNewName("");
    setNewParent("0");
    toast.success("已添加");
  };

  const save = async () => {
    if (!editing) return;
    await categoryApi.update(editing.id, editing.name, editing.parentId);
    setList((arr) => arr.map((c) => (c.id === editing.id ? { ...c, name: editing.name, parentId: editing.parentId } : c)));
    setEditing(null);
    toast.success("已更新");
  };

  const remove = async (id: string) => {
    const hasChildren = list.some((c) => c.parentId === id);
    if (hasChildren) {
      toast.error("请先删除该分类下的二级分类");
      return;
    }
    await categoryApi.remove(id);
    setList((arr) => arr.filter((c) => c.id !== id));
    toast.success("已删除");
  };

  const displayName = (c: Category) => {
    if (c.parentId === "0" || c.parentId === undefined) return c.name;
    const parent = list.find((p) => p.id === c.parentId);
    return parent ? `${parent.name} > ${c.name}` : c.name;
  };

  const sortedList = useMemo(() => {
    const result: Category[] = [];
    topCategories.forEach((top) => {
      result.push(top);
      list.filter((c) => c.parentId === top.id).forEach((child) => result.push(child));
    });
    return result;
  }, [list, topCategories]);

  return (
    <AdminShell>
      <h1 className="mb-4 text-2xl font-semibold">分类管理</h1>
      <Card>
        <CardContent className="pt-6">
          <div className="mb-4 flex gap-2">
            <Input placeholder="新分类名称" value={newName} onChange={(e) => setNewName(e.target.value)} className="max-w-xs" />
            <Select value={newParent} onValueChange={setNewParent}>
              <SelectTrigger className="w-[200px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="0">作为一级分类</SelectItem>
                {topCategories.map((c) => (
                  <SelectItem key={c.id} value={c.id}>{c.name} 下的二级分类</SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button onClick={add}><Plus className="mr-2 h-4 w-4" />添加</Button>
          </div>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead><TableHead>层级</TableHead><TableHead>名称</TableHead><TableHead>操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {sortedList.map((c) => {
                const isTop = c.parentId === "0" || c.parentId === undefined;
                return (
                  <TableRow key={c.id} className={isTop ? "bg-secondary/30" : ""}>
                    <TableCell>{c.id}</TableCell>
                    <TableCell>
                      <span className={`inline-block rounded px-2 py-0.5 text-xs ${isTop ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground"}`}>
                        {isTop ? "一级" : "二级"}
                      </span>
                    </TableCell>
                    <TableCell>
                      {editing?.id === c.id ? (
                        <div className="flex flex-col gap-2">
                          <Input value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} className="h-8" />
                          <Select value={editing.parentId} onValueChange={(v) => setEditing({ ...editing, parentId: v })}>
                            <SelectTrigger className="h-8 w-[200px]">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="0">一级分类</SelectItem>
                              {topCategories.map((p) => (
                                <SelectItem key={p.id} value={p.id}>{p.name}</SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </div>
                      ) : (
                        <span className={isTop ? "font-medium" : "pl-4 text-muted-foreground"}>
                          {displayName(c)}
                        </span>
                      )}
                    </TableCell>
                    <TableCell className="space-x-1">
                      {editing?.id === c.id ? (
                        <>
                          <Button variant="ghost" size="icon" onClick={save}><Check className="h-4 w-4" /></Button>
                          <Button variant="ghost" size="icon" onClick={() => setEditing(null)}><X className="h-4 w-4" /></Button>
                        </>
                      ) : (
                        <Button variant="ghost" size="icon" onClick={() => setEditing({ id: c.id, name: c.name, parentId: c.parentId || "0" })}><Pencil className="h-4 w-4" /></Button>
                      )}
                      <Button variant="ghost" size="icon" onClick={() => remove(c.id)}><Trash2 className="h-4 w-4" /></Button>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </AdminShell>
  );
}
