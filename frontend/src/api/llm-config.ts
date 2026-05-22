import { request } from "./request";

export interface LlmConfig {
  id: number;
  name: string;
  provider: string;
  baseUrl: string;
  model: string;
  apiKeyMasked: string;
  enabled: number;
  active: number;
  timeoutSeconds: number;
  remark: string;
  createTime: string;
  updateTime: string;
}

export interface LlmConfigForm {
  name: string;
  provider: string;
  baseUrl: string;
  model: string;
  apiKey: string;
  enabled: number;
  active: number;
  timeoutSeconds: number;
  remark: string;
}

export const llmConfigApi = {
  list: async (): Promise<LlmConfig[]> => {
    return request<LlmConfig[]>({ method: "GET", url: "/admin/llm-configs" });
  },

  getActive: async (): Promise<LlmConfig | null> => {
    return request<LlmConfig | null>({ method: "GET", url: "/admin/llm-configs/active" });
  },

  create: async (data: LlmConfigForm): Promise<LlmConfig> => {
    return request<LlmConfig>({ method: "POST", url: "/admin/llm-configs", data });
  },

  update: async (id: number, data: LlmConfigForm): Promise<LlmConfig> => {
    return request<LlmConfig>({ method: "PUT", url: `/admin/llm-configs/${id}`, data });
  },

  remove: async (id: number): Promise<void> => {
    return request<void>({ method: "DELETE", url: `/admin/llm-configs/${id}` });
  },

  activate: async (id: number): Promise<void> => {
    return request<void>({ method: "POST", url: `/admin/llm-configs/${id}/activate` });
  },

  test: async (id: number): Promise<string> => {
    const res = await request<any>({ method: "POST", url: `/admin/llm-configs/${id}/test` });
    return res?.data || "";
  },
};
