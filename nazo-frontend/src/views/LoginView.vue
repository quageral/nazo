<template>
  <div
    class="min-h-screen bg-gradient-game-dark flex items-center justify-center p-8"
  >
    <div class="w-full max-w-2xl">
      <!-- 主登录卡片 -->
      <div class="game-card">
        <!-- 游戏标题区域 -->
        <div class="text-center mb-12">
          <h1
            class="text-7xl font-black text-transparent bg-clip-text bg-gradient-game mb-6 text-shadow-lg animate-float"
          >
            NAZO
          </h1>
          <p class="text-2xl text-gray-300 font-medium tracking-wide">
            to game or not to game?
          </p>
          <div
            class="mt-6 h-1 w-32 bg-gradient-game mx-auto rounded-full"
          ></div>
        </div>

        <!-- 登录表单区域 -->
        <form @submit.prevent="handleLogin" class="space-y-8">
          <div class="space-y-2">
            <label
              for="username"
              class="block text-lg font-semibold text-gray-200 mb-3"
            >
              🎮 用户名
            </label>
            <input
              id="username"
              v-model="loginForm.username"
              type="text"
              required
              class="w-full px-6 py-4 text-lg bg-white/10 border-2 border-white/20 rounded-xl text-white placeholder-gray-400 focus:border-primary focus:ring-4 focus:ring-primary/30 focus:outline-none transition-all duration-300 backdrop-blur-sm"
              placeholder="请输入你的名字"
            />
          </div>

          <div class="space-y-2">
            <label
              for="password"
              class="block text-lg font-semibold text-gray-200 mb-3"
            >
              🔐 密码
            </label>
            <input
              id="password"
              v-model="loginForm.password"
              type="password"
              required
              class="w-full px-6 py-4 text-lg bg-white/10 border-2 border-white/20 rounded-xl text-white placeholder-gray-400 focus:border-primary focus:ring-4 focus:ring-primary/30 focus:outline-none transition-all duration-300 backdrop-blur-sm"
              placeholder="提示：Ctrl-A"
            />
          </div>

          <button
            type="submit"
            :disabled="isLoading"
            class="w-full game-button bg-gradient-game text-white font-bold disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
          >
            <span
              v-if="isLoading"
              class="flex items-center justify-center space-x-3"
            >
              <div
                class="animate-spin rounded-full h-6 w-6 border-b-2 border-white"
              ></div>
              <span>登录中...</span>
            </span>
            <span v-else class="flex items-center justify-center space-x-2">
              <span>🚀</span>
              <span>开始冒险</span>
            </span>
          </button>
        </form>

        <!-- 错误提示 -->
        <div
          v-if="errorMessage"
          class="mt-6 p-4 bg-red-500/20 border-2 border-red-500/50 text-red-200 rounded-xl backdrop-blur-sm"
        >
          <div class="flex items-center space-x-2">
            <span class="text-xl">⚠️</span>
            <span class="font-medium">{{ errorMessage }}</span>
          </div>
        </div>

        <!-- 测试账号信息 -->
        <div
          class="mt-10 p-6 bg-yellow-500/10 border-2 border-yellow-500/30 rounded-xl backdrop-blur-sm relative"
        >
          <h3
            class="text-lg font-semibold mb-3 flex items-center space-x-2 absolute z-0"
            style="color: transparent"
          >
            <span>💡</span>
            <span class="text-xs">U1RBUlRUSEVHQU1F</span>
          </h3>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { login } from "@/services/api";
import { LEVEL_1_UUID } from "@/constants/levels";

const router = useRouter();

const loginForm = ref({
  username: "",
  password: "",
});

const isLoading = ref(false);
const errorMessage = ref("");

const handleLogin = async () => {
  isLoading.value = true;
  errorMessage.value = "";

  console.log("开始登录流程，用户名:", loginForm.value.username);

  try {
    const response = await login({
      username: loginForm.value.username,
      password: loginForm.value.password,
    });

    console.log("登录API响应:", response);

    if (response.success) {
      console.log("登录成功，保存token:", response.token);

      // 保存登录状态到localStorage和cookie
      localStorage.setItem("nazo_token", response.token || "");
      localStorage.setItem("nazo_user", loginForm.value.username);

      // 将用户名保存到cookie（用于API请求）
      document.cookie = `nazo_user=${encodeURIComponent(
        loginForm.value.username
      )}; path=/; max-age=86400`; // 24小时过期

      console.log("准备跳转到第一关");

      // 跳转到第一关
      router.push(`/level/${LEVEL_1_UUID}`);

      console.log("router.push调用完成");
    } else {
      console.log("登录失败:", response.message);
      errorMessage.value = response.message || "登录失败";
    }
  } catch (error) {
    console.error("Login error:", error);
    errorMessage.value = "网络错误，请重试";
  } finally {
    isLoading.value = false;
  }
};
</script>
