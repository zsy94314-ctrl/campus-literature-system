import { createFileRoute } from "@tanstack/react-router";
import { requireAdmin } from "@/lib/guards";
import { AdminShell } from "@/components/layout/AdminShell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { useEffect, useState } from "react";
import { llmConfigApi, type LlmConfig, type LlmConfigForm } from "@/api/llm-config";
import { BrainCircuit, Plus, Pencil, Trash2, CheckCircle2, Zap, TestTube } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/admin/llm-configs")({
  beforeLoad: ({ location }) => { requireAdmin(location.href); },
  component: LlmConfigPage,
});

const emptyForm: LlmConfigForm = {
  name: "",
  provider: "openai-compatible",
  baseUrl: "",
  model: "",
  apiKey: "",
  enabled: 1,
  active: 0,
  timeoutSeconds: 30,
  remark: "",
};

function LlmConfigPage() {
  const [configs, setConfigs] = useState<LlmConfig[]>([]);
  const [loading, setLoading] = useState(false);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<LlmConfigForm>(emptyForm);

  const load = async () => {
    setLoading(true);
    try {
      const list = await llmConfigApi.list();
      setConfigs(list);
    } catch (err: any) {
      toast.error(err?.message || "加载失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const openCreate = () => {
    setEditingId(null);
    setForm(emptyForm);
    setDialogOpen(true);
  };

  const openEdit = (cfg: LlmConfig) => {
    setEditingId(cfg.id);
    setForm({
      name: cfg.name,
      provider: cfg.provider,
      baseUrl: cfg.baseUrl,
      model: cfg.model,
      apiKey: "",
      enabled: cfg.enabled,
      active: cfg.active,
      timeoutSeconds: cfg.timeoutSeconds,
      remark: cfg.remark,
    });
    setDialogOpen(true);
  };

  const submit = async () => {
    if (!form.name.trim()) return toast.error("配置名称不能为空");
    if (!form.baseUrl.trim()) return toast.error("Base URL 不能为空");
    if (!form.model.trim()) return toast.error("模型名称不能为空");
    try {
      if (editingId != null) {
        await llmConfigApi.update(editingId, form);
        toast.success("更新成功");
      } else {
        await llmConfigApi.create(form);
        toast.success("创建成功");
      }
      setDialogOpen(false);
      load();
    } catch (err: any) {
      toast.error(err?.message || "保存失败");
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("确定删除该配置吗？")) return;
    try {
      await llmConfigApi.remove(id);
      toast.success("删除成功");
      load();
    } catch (err: any) {
      toast.error(err?.message || "删除失败");
    }
  };

  const handleActivate = async (id: number) => {
    try {
      await llmConfigApi.activate(id);
      toast.success("已设为当前使用配置");
      load();
    } catch (err: any) {
      toast.error(err?.message || "设置失败");
    }
  };

  const handleTest = async (id: number) => {
    try {
      const msg = await llmConfigApi.test(id);
      toast.success(`LLM API 连接成功：${msg}`);
    } catch (err: any) {
      toast.error(err?.message || "连接测试失败");
    }
  };

  const handleTestReview = async (id: number) => {
    try {
      const res = await llmConfigApi.testReview(id);
      if (res.success) {
        toast.success(`长文本综述测试成功，耗时 ${res.elapsedMs}ms，内容长度 ${res.contentLength}，包含 ${res.sectionCount} 个章节`);
      } else {
        toast.error(`长文本综述测试失败：${res.error}（耗时 ${res.elapsedMs}ms）`);
      }
    } catch (err: any) {
      toast.error(err?.message || "长文本综述测试失败");
    }
  };

  return (
    <AdminShell>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-semibold">LLM API 管理</h1>
        <Button onClick={openCreate}>
          <Plus className="mr-2 h-4 w-4" />
          新增配置
        </Button>
      </div>

      {configs.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-16">
            <BrainCircuit className="h-10 w-10 text-muted-foreground" />
            <p className="mt-4 text-sm text-muted-foreground">暂无 LLM API 配置，请新增配置</p>
            <Button className="mt-4" onClick={openCreate}>
              <Plus className="mr-2 h-4 w-4" />
              新增配置
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {configs.map((cfg) => (
            <Card key={cfg.id} className={cfg.active === 1 ? "border-primary" : undefined}>
              <CardContent className="pt-6">
                <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                  <div className="min-w-0 flex-1 space-y-2">
                    <div className="flex items-center gap-2">
                      <span className="text-base font-semibold">{cfg.name}</span>
                      {cfg.active === 1 && (
                        <Badge variant="default" className="text-[10px]">
                          <CheckCircle2 className="mr-1 h-3 w-3" />
                          当前使用
                        </Badge>
                      )}
                      {cfg.enabled === 0 && (
                        <Badge variant="secondary" className="text-[10px]">已禁用</Badge>
                      )}
                    </div>
                    <div className="grid gap-1 text-sm text-muted-foreground">
                      <div>Provider: {cfg.provider}</div>
                      <div>Base URL: {cfg.baseUrl}</div>
                      <div>Model: {cfg.model}</div>
                      <div>API Key: {cfg.apiKeyMasked || "未设置"}</div>
                      <div>超时: {cfg.timeoutSeconds}s</div>
                      {cfg.remark && <div>备注: {cfg.remark}</div>}
                    </div>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {cfg.active !== 1 && cfg.enabled !== 0 && (
                      <Button size="sm" variant="outline" onClick={() => handleActivate(cfg.id)}>
                        <Zap className="mr-1 h-3 w-3" />
                        设为当前
                      </Button>
                    )}
                    <Button size="sm" variant="outline" onClick={() => handleTest(cfg.id)}>
                      <TestTube className="mr-1 h-3 w-3" />
                      测试连接
                    </Button>
                    <Button size="sm" variant="outline" onClick={() => handleTestReview(cfg.id)}>
                      <TestTube className="mr-1 h-3 w-3" />
                      长文本测试
                    </Button>
                    <Button size="sm" variant="outline" onClick={() => openEdit(cfg)}>
                      <Pencil className="mr-1 h-3 w-3" />
                      编辑
                    </Button>
                    <Button size="sm" variant="destructive" onClick={() => handleDelete(cfg.id)}>
                      <Trash2 className="mr-1 h-3 w-3" />
                      删除
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>{editingId != null ? "编辑配置" : "新增配置"}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="space-y-1.5">
              <Label>配置名称</Label>
              <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="例如：DeepSeek" />
            </div>
            <div className="space-y-1.5">
              <Label>Provider</Label>
              <Select value={form.provider} onValueChange={(v) => setForm({ ...form, provider: v })}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="openai-compatible">openai-compatible</SelectItem>
                  <SelectItem value="openai">openai</SelectItem>
                  <SelectItem value="deepseek">deepseek</SelectItem>
                  <SelectItem value="qwen">qwen</SelectItem>
                  <SelectItem value="custom">custom</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label>Base URL</Label>
              <Input value={form.baseUrl} onChange={(e) => setForm({ ...form, baseUrl: e.target.value })} placeholder="https://api.example.com/v1" />
            </div>
            <div className="space-y-1.5">
              <Label>模型名称</Label>
              <Input value={form.model} onChange={(e) => setForm({ ...form, model: e.target.value })} placeholder="例如：gpt-4、deepseek-chat" />
            </div>
            <div className="space-y-1.5">
              <Label>API Key{editingId != null ? "（留空表示不修改）" : ""}</Label>
              <Input type="password" value={form.apiKey} onChange={(e) => setForm({ ...form, apiKey: e.target.value })} placeholder="sk-..." />
            </div>
            <div className="space-y-1.5">
              <Label>超时时间（秒）</Label>
              <Input type="number" value={form.timeoutSeconds} onChange={(e) => setForm({ ...form, timeoutSeconds: Number(e.target.value) })} />
            </div>
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2">
                <Switch checked={form.enabled === 1} onCheckedChange={(v) => setForm({ ...form, enabled: v ? 1 : 0 })} />
                <Label className="cursor-pointer">启用</Label>
              </div>
              <div className="flex items-center gap-2">
                <Switch checked={form.active === 1} onCheckedChange={(v) => setForm({ ...form, active: v ? 1 : 0 })} />
                <Label className="cursor-pointer">设为当前使用</Label>
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>备注</Label>
              <Input value={form.remark} onChange={(e) => setForm({ ...form, remark: e.target.value })} />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>取消</Button>
            <Button onClick={submit}>保存</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </AdminShell>
  );
}
