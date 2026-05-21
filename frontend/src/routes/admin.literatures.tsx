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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useEffect, useState } from "react";
import { literatureApi } from "@/api/literature";
import { categoryApi, type Category } from "@/api/category";
import type { Literature } from "@/mock/literatures";
import { Pencil, Trash2, Plus } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/admin/literatures")({
  beforeLoad: ({ location }) => { requireAdmin(location.href); },
  component: AdminLiteraturesPage,
});

const emptyForm = {
  title: "", authors: "", abstract: "", keywords: "",
  journal: "", year: 2024, doi: "", citations: 0, categoryId: "",
  documentType: "", sourceUrl: "", content: "",
};

const documentTypes = ["期刊论文", "会议论文", "学位论文", "研究报告"];

function AdminLiteraturesPage() {
  const [list, setList] = useState<Literature[]>([]);
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [open, setOpen] = useState(false);
  const [mode, setMode] = useState<"create" | "edit">("create");
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [categories, setCategories] = useState<Category[]>([]);
  const pageSize = 10;

  const load = (k = "", p = 1) =>
    literatureApi.search({ keyword: k, page: p, pageSize }).then((r) => {
      setList(r.list);
      setTotal(r.total);
      setPage(p);
    });

  useEffect(() => {
    load();
    categoryApi.list().then(setCategories);
  }, []);

  const handleOpenChange = (v: boolean) => {
    setOpen(v);
    if (!v) {
      setMode("create");
      setEditingId(null);
      setForm(emptyForm);
    }
  };

  const openCreate = () => {
    setMode("create");
    setEditingId(null);
    setForm(emptyForm);
    setOpen(true);
  };

  const openEdit = async (l: Literature) => {
    try {
      const detail = await literatureApi.getById(l.id);
      const cid = String(detail.categoryId ?? "");
      setMode("edit");
      setEditingId(detail.id);
      setForm({
        title: detail.title,
        authors: detail.authors.join(","),
        abstract: detail.abstract,
        keywords: detail.keywords.join(","),
        journal: detail.journal,
        year: detail.year,
        doi: detail.doi,
        citations: detail.citations,
        categoryId: cid,
        documentType: detail.documentType || "",
        sourceUrl: detail.sourceUrl || "",
        content: detail.content || "",
      });
      setOpen(true);
    } catch (err: any) {
      toast.error("获取文献详情失败");
    }
  };

  const save = async () => {
    const payload = {
      ...form,
      authors: form.authors.split(",").map((s) => s.trim()).filter(Boolean),
      keywords: form.keywords.split(",").map((s) => s.trim()).filter(Boolean),
      year: Number(form.year),
      citations: Number(form.citations),
      categoryId: form.categoryId || undefined,
    };
    try {
      if (mode === "edit") {
        if (!editingId) {
          toast.error("未选择要编辑的文献");
          return;
        }
        await literatureApi.update(editingId, payload);
        toast.success("修改文献成功");
      } else {
        await literatureApi.create(payload);
        toast.success("新增文献成功");
      }
      setOpen(false);
      setMode("create");
      setEditingId(null);
      setForm(emptyForm);
      await load(keyword, page);
    } catch (err: any) {
      console.error("save literature error:", err);
      toast.error(err?.message || "操作失败，请稍后重试");
    }
  };

  const remove = async (id: string) => {
    try {
      await literatureApi.remove(id);
      toast.success("删除文献成功");
      load(keyword, page);
    } catch (err: any) {
      toast.error(err?.message || "操作失败，请稍后重试");
    }
  };

  return (
    <AdminShell>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-semibold">文献管理</h1>
        <Dialog open={open} onOpenChange={handleOpenChange}>
          <DialogTrigger asChild>
            <Button onClick={openCreate}><Plus className="mr-2 h-4 w-4" />新增文献</Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl" aria-describedby={undefined}>
            <DialogHeader>
              <DialogTitle>{mode === "edit" ? "编辑文献" : "新增文献"}</DialogTitle>
            </DialogHeader>
            <div className="grid grid-cols-2 gap-3">
              <div className="col-span-2 space-y-1.5">
                <Label>标题</Label>
                <Input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
              </div>
              <div className="space-y-1.5">
                <Label>作者（逗号分隔）</Label>
                <Input value={form.authors} onChange={(e) => setForm({ ...form, authors: e.target.value })} />
              </div>
              <div className="space-y-1.5">
                <Label>关键词（逗号分隔）</Label>
                <Input value={form.keywords} onChange={(e) => setForm({ ...form, keywords: e.target.value })} />
              </div>
              <div className="space-y-1.5">
                <Label>期刊</Label>
                <Input value={form.journal} onChange={(e) => setForm({ ...form, journal: e.target.value })} />
              </div>
              <div className="space-y-1.5">
                <Label>分类</Label>
                <Select value={form.categoryId} onValueChange={(v) => setForm({ ...form, categoryId: v })}>
                  <SelectTrigger>
                    <SelectValue placeholder="选择分类" />
                  </SelectTrigger>
                  <SelectContent>
                    {categories
                      .filter((c) => c.parentId && c.parentId !== "0")
                      .map((c) => (
                        <SelectItem key={String(c.id)} value={String(c.id)}>{c.name}</SelectItem>
                      ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label>年份</Label>
                <Input type="number" value={form.year} onChange={(e) => setForm({ ...form, year: Number(e.target.value) })} />
              </div>
              <div className="space-y-1.5">
                <Label>引用次数</Label>
                <Input type="number" value={form.citations} onChange={(e) => setForm({ ...form, citations: Number(e.target.value) })} />
              </div>
              <div className="space-y-1.5">
                <Label>文献类型</Label>
                <Select value={form.documentType} onValueChange={(v) => setForm({ ...form, documentType: v })}>
                  <SelectTrigger>
                    <SelectValue placeholder="选择文献类型" />
                  </SelectTrigger>
                  <SelectContent>
                    {documentTypes.map((t) => (
                      <SelectItem key={t} value={t}>{t}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="col-span-2 space-y-1.5">
                <Label>DOI</Label>
                <Input value={form.doi} onChange={(e) => setForm({ ...form, doi: e.target.value })} />
              </div>
              <div className="col-span-2 space-y-1.5">
                <Label>来源链接</Label>
                <Input value={form.sourceUrl} onChange={(e) => setForm({ ...form, sourceUrl: e.target.value })} placeholder="https://..." />
              </div>
              <div className="col-span-2 space-y-1.5">
                <Label>摘要</Label>
                <Textarea rows={4} value={form.abstract} onChange={(e) => setForm({ ...form, abstract: e.target.value })} />
              </div>
              <div className="col-span-2 space-y-1.5">
                <Label>正文节选</Label>
                <Textarea rows={4} value={form.content} onChange={(e) => setForm({ ...form, content: e.target.value })} placeholder="输入正文节选或文献内容说明..." />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => handleOpenChange(false)}>取消</Button>
              <Button onClick={save}>保存</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <Card>
        <CardContent className="pt-6">
          <div className="mb-4 flex gap-2">
            <Input placeholder="搜索标题或关键词..." value={keyword} onChange={(e) => setKeyword(e.target.value)} />
            <Button onClick={() => load(keyword, 1)}>查询</Button>
          </div>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>标题</TableHead><TableHead>作者</TableHead><TableHead>期刊</TableHead>
                <TableHead>分类</TableHead><TableHead>类型</TableHead><TableHead>年份</TableHead><TableHead>引用</TableHead><TableHead>操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {list.map((l) => (
                <TableRow key={l.id}>
                  <TableCell className="max-w-md truncate">{l.title}</TableCell>
                  <TableCell>{l.authors.join(", ")}</TableCell>
                  <TableCell>{l.journal}</TableCell>
                  <TableCell>{l.category}</TableCell>
                  <TableCell>{l.documentType || "—"}</TableCell>
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

          <div className="mt-4 flex items-center justify-between border-t pt-4 text-sm text-muted-foreground">
            <span>共 {total} 条</span>
            <div className="flex items-center gap-4">
              <span>第 {page} / {Math.max(1, Math.ceil(total / pageSize))} 页</span>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page <= 1}
                  onClick={() => load(keyword, page - 1)}
                >
                  上一页
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page >= Math.ceil(total / pageSize)}
                  onClick={() => load(keyword, page + 1)}
                >
                  下一页
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </AdminShell>
  );
}
