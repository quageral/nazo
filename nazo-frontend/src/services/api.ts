// API服务模块 - 与Java后端通信
const API_BASE_URL = "http://localhost:8080/api";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  token?: string;
  nextLevel?: string;
}

export interface LevelInfo {
  id: string;
  name: string;
  description: string;
  nextLevelUuid: string;
}

export interface LevelResponse {
  success: boolean;
  level: LevelInfo;
  message?: string;
}

export interface CompleteResponse {
  success: boolean;
  message: string;
  nextLevel?: string;
}

// API请求函数
export const apiService = {
  // 用户登录
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(credentials),
      });

      return await response.json();
    } catch (error) {
      console.error("登录请求失败:", error);
      return {
        success: false,
        message: "网络连接失败，请检查后端服务是否启动",
      };
    }
  },

  // 获取关卡信息
  async getLevel(uuid: string): Promise<LevelResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/level/${uuid}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      });

      return await response.json();
    } catch (error) {
      console.error("获取关卡信息失败:", error);
      return {
        success: false,
        level: { id: "", name: "", description: "", nextLevelUuid: "" },
        message: "网络连接失败，请检查后端服务是否启动",
      };
    }
  },

  // 完成关卡
  async completeLevel(uuid: string, answer: string): Promise<CompleteResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/level/${uuid}/complete`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ answer }),
      });

      return await response.json();
    } catch (error) {
      console.error("完成关卡请求失败:", error);
      return {
        success: false,
        message: "网络连接失败，请检查后端服务是否启动",
      };
    }
  },
};

export default apiService;
