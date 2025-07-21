<template>
  <div class="min-h-screen bg-gradient-game-dark">
    <!-- 顶部关卡信息栏 -->
    <header class="glass-card border-b-0 rounded-none shadow-2xl">
      <div
        class="max-w-7xl mx-auto px-8 py-6 flex items-center justify-between"
      >
        <!-- 左侧关卡信息 -->
        <div class="flex items-center space-x-6">
          <div class="flex items-center space-x-3">
            <div
              class="w-12 h-12 bg-gradient-game rounded-full flex items-center justify-center text-white font-bold text-xl"
            >
              {{ levelInfo?.id }}
            </div>
            <div>
              <h1 class="text-3xl font-bold text-white text-shadow-lg">
                第{{ levelInfo?.id }}关
              </h1>
              <p class="text-xl text-gray-300 font-medium">
                {{ levelInfo?.name }}
              </p>
            </div>
          </div>
          <!-- 用户名显示 -->
          <div
            v-if="currentUser"
            class="flex items-center space-x-2 bg-white/10 px-4 py-2 rounded-md backdrop-blur-sm"
          >
            <span class="text-lg">👤</span>
            <span class="text-white font-medium">{{ currentUser }}</span>
          </div>
        </div>

        <!-- 右侧提示按钮 -->
        <button
          @click="showHint = true"
          class="game-button bg-yellow-500 hover:bg-yellow-600 text-white flex items-center space-x-2"
        >
          <span class="text-xl">💡</span>
          <span>获取提示</span>
        </button>
      </div>
    </header>

    <!-- 主游戏区域 - 使用80%的页面空间 -->
    <main class="p-8">
      <div class="max-w-7xl mx-auto h-[calc(100vh-200px)]">
        <!-- 根据关卡类型加载不同组件 -->
        <component
          :is="currentLevelComponent"
          :level-uuid="uuid"
          @game-complete="handleGameComplete"
          class="h-full"
        />
      </div>
    </main>

    <!-- 提示弹窗 -->
    <div
      v-if="showHint"
      class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
      @click.self="showHint = false"
    >
      <div class="bg-white rounded-2xl p-6 max-w-md w-full shadow-2xl">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold text-gray-800">💡 关卡提示</h3>
          <button
            @click="showHint = false"
            class="text-gray-500 hover:text-gray-700 text-2xl"
          >
            ×
          </button>
        </div>
        <p class="text-gray-600 leading-relaxed">
          {{ levelInfo?.description || "暂无提示信息" }}
        </p>
        <div class="mt-6 flex justify-end">
          <button
            @click="showHint = false"
            class="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-md font-semibold"
          >
            知道了
          </button>
        </div>
      </div>
    </div>

    <!-- 通关成功弹窗 -->
    <div
      v-if="showSuccessModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
    >
      <div
        class="bg-white rounded-2xl p-8 max-w-md w-full shadow-2xl text-center"
      >
        <div class="text-6xl mb-4">🎉</div>
        <h3 class="text-2xl font-bold text-gray-800 mb-2">恭喜通关！</h3>
        <p class="text-gray-600 mb-6">你可以记住下一关的网址，下次直接访问</p>

        <div class="space-y-3">
          <button
            @click="goToNextLevel"
            class="w-full bg-green-500 hover:bg-green-600 text-white py-3 rounded-md font-semibold"
          >
            进入下一关
          </button>
        </div>
      </div>
    </div>

    <!-- 加载中状态 -->
    <div
      v-if="isLoading"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-40"
    >
      <div class="bg-white rounded-md p-6 text-center">
        <div
          class="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto mb-4"
        ></div>
        <p class="text-gray-600">加载中...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { getLevel } from "@/services/api";
import type { LevelInfo } from "@/services/api";
import TetrisLevel from "@/components/TetrisLevel.vue";
import CorrelationGame from "@/components/CorrelationGame.vue";
import MinesweeperGame from "@/components/MinesweeperGame.vue";
import WordleGame from "@/components/WordleGame.vue";
import ColorGame from "@/components/ColorGame.vue";
import {
  LEVEL_1_UUID,
  LEVEL_2_UUID,
  LEVEL_3_UUID,
  LEVEL_4_UUID,
  LEVEL_5_UUID,
} from "@/constants/levels";

interface Props {
  uuid: string;
}

const props = defineProps<Props>();
const router = useRouter();

const levelInfo = ref<LevelInfo | null>(null);
const showHint = ref(false);
const showSuccessModal = ref(false);
const successMessage = ref("");
const nextLevelUuid = ref("");
const isLoading = ref(true);

// 获取当前用户名
const currentUser = ref(localStorage.getItem("nazo_user") || "");

// 根据UUID确定当前关卡组件
const currentLevelComponent = computed(() => {
  switch (props.uuid) {
    case LEVEL_1_UUID:
      return TetrisLevel;
    case LEVEL_2_UUID:
      return CorrelationGame;
    case LEVEL_3_UUID:
      return MinesweeperGame;
    case LEVEL_4_UUID:
      return WordleGame;
    case LEVEL_5_UUID:
      return ColorGame;
    default:
      return null;
  }
});

// 加载关卡信息
const loadLevel = async () => {
  isLoading.value = true;
  try {
    const response = await getLevel(props.uuid);
    if (response.success) {
      levelInfo.value = response.level;
    } else {
      // 关卡不存在，跳转到错误页面
      router.push("/error");
    }
  } catch (error) {
    console.error("Failed to load level:", error);
    router.push("/error");
  } finally {
    isLoading.value = false;
  }
};

// 处理游戏完成
const handleGameComplete = (data: {
  success: boolean;
  message: string;
  nextLevel?: string;
}) => {
  if (data.success) {
    successMessage.value = data.message;
    nextLevelUuid.value = data.nextLevel || "";
    showSuccessModal.value = true;
  }
};

// 前往下一关
const goToNextLevel = () => {
  showSuccessModal.value = false; // 隐藏通关成功弹窗
  if (nextLevelUuid.value) {
    router.push(`/level/${nextLevelUuid.value}`);
  } else {
    // 没有下一关，返回登录页
    logout();
  }
};

// 退出登录
const logout = () => {
  localStorage.removeItem("nazo_token");
  localStorage.removeItem("nazo_user");
  router.push("/login");
};

// 监听UUID变化，重新加载关卡信息
watch(
  () => props.uuid,
  () => {
    loadLevel();
  },
  { immediate: true }
);
</script>
