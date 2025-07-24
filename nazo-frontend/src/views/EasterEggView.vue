<template>
  <div class="min-h-screen bg-black relative overflow-hidden">
    <!-- Main Content -->
    <div class="relative z-10 flex flex-col items-center justify-center min-h-screen px-4">
      <!-- Header -->
      <div class="text-center mb-8">
        <div class="flex items-center justify-center mb-4">
          <img src="https://geektyper.com/ASSETS/logos/shield.png" alt="SHIELD Logo" width="200" height="200"
            class="mr-3" />
        </div>
        <p class="text-gray-400">输入彩蛋ID以解锁隐藏消息</p>
        <p class="text-gray-500 text-sm mt-2">所有的无PS提示的关卡都可以Command-A</p>
      </div>

      <!-- Input Section -->
      <div class="w-full max-w-md mb-8">
        <div class="bg-gray-900 bg-opacity-80 backdrop-blur-sm rounded-lg p-6 border border-gray-700">
          <div class="mb-4">
            <label for="easterEggId" class="block text-white text-sm font-medium mb-2">
              彩蛋 ID
            </label>
            <input id="easterEggId" v-model="inputId" type="text" placeholder="请输入彩蛋ID..."
              class="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              @keyup.enter="unlockEasterEgg" />
          </div>
          <button @click="unlockEasterEgg" :disabled="isLoading || !inputId.trim()"
            class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white font-medium py-3 px-4 rounded-lg transition-colors duration-200">
            <span v-if="isLoading" class="flex items-center justify-center">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none"
                viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                </path>
              </svg>
              解锁中...
            </span>
            <span v-else>解锁彩蛋</span>
          </button>
        </div>
      </div>

      <!-- Messages List -->
      <div class="w-full max-w-2xl">
        <div class="bg-gray-900 bg-opacity-80 backdrop-blur-sm rounded-lg p-6 border border-gray-700">
          <h2 class="text-xl font-semibold text-white mb-4 flex items-center">
            <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            已收集的彩蛋
            <span class="ml-2 text-sm text-gray-400">({{ easterEggs.length }})</span>
          </h2>

          <div v-if="easterEggs.length === 0" class="text-center py-8">
            <div class="text-gray-500 mb-2">
              <svg class="w-16 h-16 mx-auto mb-4 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-4m-12 0H4m8 6v-6">
                </path>
              </svg>
            </div>
            <p class="text-gray-400">还没有收集到任何彩蛋</p>
            <p class="text-gray-500 text-sm mt-1">
              输入正确的彩蛋ID来开始收集吧！
            </p>
          </div>

          <div v-else class="space-y-4">
            <div v-for="egg in easterEggs" :key="egg.easterEggId + '_' + egg.collectedAt"
              class="bg-gray-800 bg-opacity-60 rounded-lg p-4 border border-gray-600 transform transition-all duration-300 hover:scale-105">
              <div class="flex justify-between items-start mb-2">
                <div class="flex items-center">
                  <span class="inline-block w-3 h-3 bg-green-500 rounded-full mr-2"></span>
                  <div class="flex flex-col">
                    <span class="text-blue-400 font-medium">{{
                      egg.easterEggId
                      }}</span>
                    <span class="text-purple-400 text-sm">关卡: {{ egg.levelName }}</span>
                  </div>
                </div>
                <div class="text-right">
                  <span class="text-yellow-400 font-semibold">{{
                    egg.time
                    }}</span>
                  <div class="text-gray-500 text-xs mt-1">
                    {{ formatCollectedTime(egg.collectedAt) }}
                  </div>
                </div>
              </div>
              <p class="text-gray-300 leading-relaxed">{{ egg.message }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Error Message -->
      <div v-if="errorMessage"
        class="fixed top-4 right-4 bg-red-600 text-white px-6 py-3 rounded-lg shadow-lg border border-red-700 z-50">
        <div class="flex items-center">
          <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd"
              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
              clip-rule="evenodd"></path>
          </svg>
          {{ errorMessage }}
        </div>
      </div>

      <!-- Success Message -->
      <div v-if="successMessage"
        class="fixed top-4 right-4 bg-green-600 text-white px-6 py-3 rounded-lg shadow-lg border border-green-700 z-50">
        <div class="flex items-center">
          <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
              clip-rule="evenodd"></path>
          </svg>
          {{ successMessage }}
        </div>
      </div>
    </div>

    <!-- 隐藏消息 -->
    <div
      class="absolute bottom-4 right-4 text-transparent text-xs opacity-5 pointer-events-none select-none max-w-xs text-right">
      只要提示里有PS的关卡就有隐藏彩蛋，非关卡页面也可以Command-A
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getEasterEgg } from "@/services/api";

// Import API_BASE_URL from api service
const API_BASE_URL = "http://43.138.133.3:8080/api";

interface CollectedEasterEgg {
  easterEggId: string;
  time: string;
  levelName: string;
  message: string;
  collectedAt: number;
}

const inputId = ref("");
const isLoading = ref(false);
const errorMessage = ref("");
const successMessage = ref("");
const easterEggs = ref<CollectedEasterEgg[]>([]);

// 格式化收集时间
const formatCollectedTime = (timestamp: number): string => {
  const date = new Date(timestamp);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
};

// 显示消息的辅助函数
const showMessage = (message: string, type: "success" | "error") => {
  if (type === "success") {
    successMessage.value = message;
    errorMessage.value = "";
    setTimeout(() => {
      successMessage.value = "";
    }, 3000);
  } else {
    errorMessage.value = message;
    successMessage.value = "";
    setTimeout(() => {
      errorMessage.value = "";
    }, 5000);
  }
};

// 解锁彩蛋
const unlockEasterEgg = async () => {
  if (!inputId.value.trim()) {
    showMessage("请输入彩蛋ID", "error");
    return;
  }

  isLoading.value = true;

  try {
    const response = await getEasterEgg(inputId.value.trim());

    if (response.success) {
      // 添加新的彩蛋到列表
      const newEgg: CollectedEasterEgg = {
        easterEggId: inputId.value.trim(),
        time: response.time || "",
        levelName: response.levelName || "",
        message: response.message || "",
        collectedAt: response.collectedAt || Date.now(),
      };

      easterEggs.value.unshift(newEgg);
      inputId.value = "";
      showMessage("彩蛋解锁成功！", "success");
    } else {
      showMessage(response.message || "彩蛋不存在", "error");
    }
  } catch (error) {
    console.error("解锁彩蛋失败:", error);
    showMessage("网络错误，请稍后重试", "error");
  } finally {
    isLoading.value = false;
  }
};

// 加载已收集的彩蛋
const loadCollectedEasterEggs = async () => {
  try {
    // 从 localStorage 获取用户名
    const username = localStorage.getItem("nazo_user") || "";

    const response = await fetch(
      `${API_BASE_URL}/easter/collected?username=${encodeURIComponent(
        username
      )}`,
      {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      console.error("HTTP 错误:", response.status, response.statusText);
      return;
    }

    const data = await response.json();
    console.log("已收集彩蛋数据:", data);

    if (data.success && data.eggs) {
      easterEggs.value = data.eggs;
    }
  } catch (error) {
    console.error("加载已收集彩蛋失败:", error);
    showMessage("加载彩蛋失败，请检查网络连接", "error");
  }
};

// 组件挂载时加载已收集的彩蛋
onMounted(() => {
  loadCollectedEasterEggs();
});
</script>

<style scoped>
/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.bg-gray-900 {
  animation: fadeInUp 0.6s ease-out;
}
</style>
