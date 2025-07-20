// API服务模块 - 与Java后端通信
const API_BASE_URL = "http://localhost:8080/api";

// 辅助函数：从cookie获取用户名
function getUsernameFromCookie(): string {
  const cookies = document.cookie.split(";");
  for (let cookie of cookies) {
    const [name, value] = cookie.trim().split("=");
    if (name === "nazo_user") {
      return decodeURIComponent(value);
    }
  }
  return localStorage.getItem("nazo_user") || "";
}

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

  // 开始游戏
  async startGame(
    levelUuid: string
  ): Promise<{ success: boolean; sessionId?: string; message: string }> {
    try {
      const username = getUsernameFromCookie();
      const response = await fetch(`${API_BASE_URL}/game/start`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ levelUuid, username }),
      });

      return await response.json();
    } catch (error) {
      console.error("开始游戏失败:", error);
      return {
        success: false,
        message: "网络连接失败，请检查后端服务是否启动",
      };
    }
  },

  // 完成游戏
  async completeGame(
    levelUuid: string,
    sessionId: string,
    score: number
  ): Promise<{ success: boolean; message: string; nextLevel?: string }> {
    try {
      const username = getUsernameFromCookie();

      // 根据关卡类型使用不同的数据字段
      let gameData: any;
      if (levelUuid === "tetris-level-1") {
        gameData = { score };
      } else if (levelUuid === "correlation-level-2") {
        gameData = { score };
      } else {
        gameData = { score }; // 默认使用 score
      }

      const response = await fetch(`${API_BASE_URL}/game/complete`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username,
          levelUuid,
          sessionId,
          data: gameData,
        }),
      });

      return await response.json();
    } catch (error) {
      console.error("完成游戏失败:", error);
      return {
        success: false,
        message: "网络连接失败，请检查后端服务是否启动",
      };
    }
  },
};

// 便捷函数导出
export const login = apiService.login;
export const getLevel = apiService.getLevel;
export const completeLevel = apiService.completeLevel;
export const startGame = apiService.startGame;
export const completeGame = apiService.completeGame;

export default apiService;
