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
  hint: string;
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

export interface PuzzleResponse {
  success: boolean;
  puzzle: string;
  message?: string;
}

export interface EasterEggResponse {
  success: boolean;
  message: string;
  time?: string;
  levelName?: string;
  collectedAt?: number;
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

  // 获取彩蛋内容
  async getEasterEgg(easterEggId: string): Promise<EasterEggResponse> {
    try {
      const response = await fetch(
        `${API_BASE_URL}/easter/${easterEggId}?username=${getUsernameFromCookie()}`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      return await response.json();
    } catch (error) {
      console.error("获取彩蛋失败:", error);
      return {
        success: false,
        message: "网络连接失败，请检查后端服务是否启动",
      };
    }
  },

  // 获取谜题内容
  async getPuzzle(uuid: string): Promise<PuzzleResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/puzzle/${uuid}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      });

      return await response.json();
    } catch (error) {
      console.error("获取谜题内容失败:", error);
      return {
        success: false,
        puzzle: "",
        message: "网络连接失败，请检查后端服务是否启动",
      };
    }
  },

  // 完成谜题
  async completePuzzle(uuid: string, answer: string): Promise<CompleteResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/puzzle/${uuid}/complete`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ answer }),
      });

      return await response.json();
    } catch (error) {
      console.error("完成谜题请求失败:", error);
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
  // fixme:
  async completeGame(
    levelUuid: string,
    sessionId: string,
    gameData: any
  ): Promise<{ success: boolean; message: string; nextLevel?: string }> {
    try {
      const username = getUsernameFromCookie();

      // 处理不同类型的游戏数据
      let finalGameData: any;
      if (typeof gameData === "number") {
        // 兼容之前只传分数的情况
        finalGameData = { score: gameData };
      } else {
        finalGameData = gameData;
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
          data: finalGameData,
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
export const getPuzzle = apiService.getPuzzle;
export const completePuzzle = apiService.completePuzzle;
export const startGame = apiService.startGame;
export const completeGame = apiService.completeGame;
export const getEasterEgg = apiService.getEasterEgg;

// Wordle game API functions
export interface WordleStartRequest {
  username: string;
}

export interface WordleStartResponse {
  success: boolean;
  sessionId?: string;
  message: string;
}

export interface WordleGuessRequest {
  sessionId: string;
  guess: string;
}

export interface WordleGuessResponse {
  success: boolean;
  result?: string;
  guessCount?: number;
  answer?: string;
  message?: string;
}

// Wordle specific functions
export const wordleService = {
  // Start a new Wordle game
  async startWordle(username: string): Promise<WordleStartResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/wordle/start`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ username }),
      });

      return await response.json();
    } catch (error) {
      console.error("启动Wordle游戏失败:", error);
      return {
        success: false,
        message: "网络连接失败，请检查后端服务是否启动",
      };
    }
  },

  // Make a guess in Wordle
  async guessWordle(
    sessionId: string,
    guess: string
  ): Promise<WordleGuessResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/wordle/guess`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ sessionId, guess }),
      });

      return await response.json();
    } catch (error) {
      console.error("Wordle猜测失败:", error);
      return {
        success: false,
        message: "网络连接失败，请检查后端服务是否启动",
      };
    }
  },
};

// Export Wordle functions
export const startWordle = wordleService.startWordle;
export const guessWordle = wordleService.guessWordle;

export default apiService;
