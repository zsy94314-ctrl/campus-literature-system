import { createFileRoute } from "@tanstack/react-router";
import { requireAdmin } from "@/lib/guards";
import { AdminShell } from "@/components/layout/AdminShell";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { useEffect, useState } from "react";
import { literatureApi } from "@/api/literature";
import type { Literature } from "@/mock/literatures";
import { Pencil, Trash2, Plus } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/admin/literatures")({
  beforeLoad: ({ location }) => { requireAdmin(location.href); },
  component: AdminLiteraturesPage,
});

const emptyForm = {
  title: "", authors: "", abstract: "", keywords: "",
  journal: "", year: 2024, doi: "", citations: 0, category: "",
};

function AdminLiteraturesPage() {
  const [list, setList] = useState<Literature[]>([]);
  const [keyword, setKeyword] = useState("");
  const [open, setOpen] = useState(false);
  const [editId, setEditId] = useState<string | null>(null);
  const [form, setForm] = useState(emptyForm);

  const load = (k = "") => literatureApi.search({ keyword: k }).then((r) => setList(r.list));
  useEffect(() => { load(); }, []);

  const openCreate = () => { setEditId(null); setForm(emptyForm); setOpen(true); };
  const openEdit = (l: Literature) => {
    setEditId(l.id);
    setForm({
      title: l.title, authors: l.authors.join(","), abstract: l.abstract,
      keywords: l.keywords.join(","), journal: l.journal, year: l.year,
      doi: l.doi, citations: l.citations, category: l.category,
    });
    setOpen(true);
  };

  const save = async () => {
    const payload = {
      ...form,
      authors: form.authors.split(",").map((s) => s.trim()).filter(Boolean),
      keywords: form.keywords.split(",").map((s) => s.trim()).filter(Boolean),
      year: Number(form.year),
      citations: Number(form.citations),
    };
    if (editId) await literatureApi.update(editId, payload);
    else await literatureApi.create(payload);
    toast.success("保存成功");
    setOpen(false);
    load(keyword);
  };

  const remove = async (id: string) => {
    await literatureApi.remove(id);
    toast.success("已删除");
    load(keyword);
  };

  return (
    <AdminShell>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-semibold">文献管理</h1>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button onClick={openCreate}><Plus className="mr-2 h-4 w-4" />新增文献</Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl">
            <DialogHeader><DialogTitle>{editId ? "编辑文献" : "新增文献"}</DialogTitle></DialogHeader>
            <div className="grid grid-cols-2 gap-3">
              <div className="col-span-2 space-y-1.5"><Label>标题</Label><Input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></div>
              <div className="space-y-1.5"><Label>作者（逗号分隔）</Label><Input value={form.authors} onChange={(e) => setForm({ ...form, authors: e.target.value })} /></div>
              <div className="space-y-1.5"><Label>关键词（逗号分隔）</Label><Input value={form.keywords} onChange={(e) => setForm({ ...form, keywords: e.target.value })} /></div>
              <div className="space-y-1.5"><Label>期刊</Label><Input value={form.journal} onChange={(e) => setForm({ ...form, journal: e.target.value })} /></div>
              <div className="space-y-1.5"><Label>分类</Label><Input value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} /></div>
              <div className="space-y-1.5"><Label>年份</Label><Input type="number" value={form.year} onChange={(e) => setForm({ ...form, year: Number(e.target.value) })} /></div>
              <div className="space-y-1.5"><Label>引用次数</Label><Input type="number" value={form.citations} onChange={(e) => setForm({ ...form, citations: Number(e.target.value) })} /></div>
              <div className="col-span-2 space-y-1.5"><Label>DOI</Label><Input value={form.doi} onChange={(e) => setForm({ ...form, doi: e.target.value })} /></div>
              <div className="col-span-2 space-y-1.5"><Label>摘要</Label><Textarea rows={4} value={form.abstract} onChange={(e) => setForm({ ...form, abstract: e.target.value })} /></div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setOpen(false)}>取消</Button>
              <Button onClick={save}>保存</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <Card>
        <CardContent className="pt-6">
          <div className="mb-4 flex gap-2">
            <Input placeholder="搜索标题或关键词..." value={keyword} onChange={(e) => setKeyword(e.target.value)} />
            <Button onClick={() => load(keyword)}>查询</Button>
          </div>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>标题</TableHead><TableHead>作者</TableHead><TableHead>期刊</TableHead>
                <TableHead>年份</TableHead><TableHead>引用</TableHead><TableHead>操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {list.map((l) => (
                <TableRow key={l.id}>
                  <TableCell className="max-w-md truncate">{l.title}</TableCell>
                  <TableCell>{l.authors.join(", ")}</TableCell>
                  <TableCell>{l.journal}</TableCell>
                  <TableCell>{l.year}</TableCell>
                  <TableCell>{l.citations}</TableCell>
                  <TableCell className="space-x-1">
                    <Button variant="ghost" size="icon" onClick={() => openEdit(l)}><Pencil className="h-4 w-4" /></Button>
                    <Button variant="ghost" size="icon" onClick={() => remove(l.id)}><Trash2 className="h-4 w-4" /></Button>
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
