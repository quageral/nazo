<template>
  <div class="min-h-screen" :class="props.uuid === CONGRATULATIONS_UUID ? '' : 'bg-gradient-game-dark'">
    <!-- 顶部关卡信息栏 - 不在恭喜页面显示 -->
    <header v-if="props.uuid !== CONGRATULATIONS_UUID" class="glass-card border-b-0 rounded-none shadow-2xl">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 lg:py-6">
        <!-- 移动端垂直布局，桌面端水平布局 -->
        <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 lg:gap-6">
          <!-- 左侧关卡信息 -->
          <div class="flex flex-col sm:flex-row sm:items-center gap-4 sm:gap-6">
            <div class="flex items-center gap-3">
              <div
                class="w-10 h-10 sm:w-12 sm:h-12 bg-gradient-game rounded-full flex items-center justify-center text-white font-bold text-lg sm:text-xl">
                {{ levelInfo?.id }}
              </div>
              <div>
                <h1 class="text-2xl sm:text-3xl font-bold text-white text-shadow-lg">
                  第{{ levelInfo?.id }}关
                </h1>
                <p class="text-lg sm:text-xl text-gray-300 font-medium">
                  {{ levelInfo?.name }}
                </p>
              </div>
            </div>

            <!-- 用户名显示 -->
            <div v-if="currentUser"
              class="flex items-center gap-2 bg-white/10 px-3 py-2 rounded-md backdrop-blur-sm w-fit">
              <span class="text-base sm:text-lg">👤</span>
              <span class="text-white font-medium text-sm sm:text-base">{{
                currentUser
              }}</span>
            </div>
          </div>

          <!-- 右侧提示按钮 -->
          <button @click="showHint = true"
            class="game-button bg-yellow-500 hover:bg-yellow-600 text-white flex items-center justify-center gap-2 w-full sm:w-auto px-4 py-3 text-base sm:text-lg">
            <span class="text-lg sm:text-xl">💡</span>
            <span>获取提示</span>
          </button>
        </div>
      </div>
    </header>

    <!-- 主游戏区域 - 恭喜页面全屏显示 -->
    <main :class="props.uuid === CONGRATULATIONS_UUID ? '' : 'p-8 flex items-center justify-center'">
      <div v-if="props.uuid === CONGRATULATIONS_UUID" class="w-full h-screen">
        <!-- 恭喜页面全屏显示 -->
        <component :is="currentLevelComponent" :level-uuid="uuid" @game-complete="handleGameComplete" />
      </div>
      <div v-else class="max-w-7xl w-full h-[calc(100vh-200px)] flex items-center justify-center">
        <!-- 根据关卡类型加载不同组件 -->
        <component :is="currentLevelComponent" :level-uuid="uuid" @game-complete="handleGameComplete"
          class="w-full max-w-9xl" />
      </div>
    </main>

    <!-- 提示弹窗 - 响应式设计 -->
    <div v-if="showHint" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
      @click.self="showHint = false">
      <div class="bg-white rounded-2xl p-4 sm:p-6 max-w-sm sm:max-w-md lg:max-w-lg w-full shadow-2xl mx-4">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg sm:text-xl font-bold text-gray-800">
            💡 关卡提示
          </h3>
          <button @click="showHint = false"
            class="text-gray-500 hover:text-gray-700 text-xl sm:text-2xl p-1 hover:bg-gray-100 rounded-full transition-colors">
            ×
          </button>
        </div>
        <p class="text-gray-600 leading-relaxed text-sm sm:text-base">
          {{ levelInfo?.hint || "暂无提示信息" }}
        </p>
        <div class="mt-6 flex justify-end">
          <button @click="showHint = false"
            class="bg-purple-600 hover:bg-purple-700 text-white px-4 sm:px-6 py-2 sm:py-3 rounded-md font-semibold text-sm sm:text-base transition-colors">
            知道了
          </button>
        </div>
      </div>
    </div>

    <!-- 通关成功弹窗 - 响应式设计 -->
    <div v-if="showSuccessModal" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <!-- 烟花效果 -->
      <div class="fireworks-container">
        <div v-for="firework in fireworks" :key="firework.id" class="firework-burst"
          :style="{ left: firework.x + '%', top: firework.y + '%' }">
          <div v-for="particle in firework.particles" :key="particle.id" class="firework-particle" :style="{
            backgroundColor: particle.color,
            '--dx': particle.dx + 'px',
            '--dy': particle.dy + 'px',
            animationDelay: particle.delay + 's',
          }"></div>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-6 sm:p-8 max-w-sm sm:max-w-md w-full shadow-2xl text-center mx-4">
        <div class="text-4xl sm:text-6xl mb-4">🎉</div>
        <h3 class="text-xl sm:text-2xl font-bold text-gray-800 mb-2">
          恭喜通关！
        </h3>
        <p class="text-gray-600 mb-4 text-sm sm:text-base">
          {{ successMessage }}
        </p>

        <!-- 显示下一关信息 -->
        <div v-if="nextLevelUuid" class="bg-gray-50 rounded-lg p-3 mb-4">
          <p class="text-xs text-gray-500 mb-1">下一关地址（可记住下次直接访问）</p>
          <p class="text-sm text-gray-700 font-mono break-all">
            /level/{{ nextLevelUuid }}
          </p>
        </div>

        <div class="space-y-3">
          <button v-if="nextLevelUuid" @click="goToNextLevel"
            class="w-full bg-green-500 hover:bg-green-600 text-white py-3 sm:py-4 rounded-md font-semibold text-sm sm:text-base transition-colors">
            进入下一关
          </button>
          <button @click="stayOnCurrentLevel"
            class="w-full bg-gray-500 hover:bg-gray-600 text-white py-3 sm:py-4 rounded-md font-semibold text-sm sm:text-base transition-colors">
            暂时留下
          </button>
        </div>
      </div>
    </div>

    <!-- 加载中状态 - 响应式设计 -->
    <div v-if="isLoading" class="fixed inset-0 bg-black/50 flex items-center justify-center z-40 p-4">
      <div class="bg-white rounded-lg p-6 sm:p-8 text-center mx-4">
        <div class="animate-spin rounded-full h-10 w-10 sm:h-12 sm:w-12 border-b-2 border-purple-600 mx-auto mb-4">
        </div>
        <p class="text-gray-600 text-sm sm:text-base">加载中...</p>
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
import NumberSequenceGame from "@/components/NumberSequenceGame.vue";
import FriendsGame from "@/components/FriendsGame.vue";
import MinecraftQuizGame from "@/components/MinecraftQuizGame.vue";
import CatGame from "@/components/CatGame.vue";
import GeographyGame from "@/components/GeographyGame.vue";
import GenericPuzzle from "@/components/GenericPuzzle.vue";
import CongratulationsView from "@/views/CongratulationsView.vue";

import {
  LEVEL_1_UUID,
  LEVEL_2_UUID,
  LEVEL_3_UUID,
  LEVEL_4_UUID,
  LEVEL_5_UUID,
  LEVEL_6_UUID,
  LEVEL_7_UUID,
  LEVEL_8_UUID,
  LEVEL_9_UUID,
  LEVEL_10_UUID,
  PUZZLE_1_UUID,
  PUZZLE_2_UUID,
  PUZZLE_3_UUID,
  PUZZLE_4_UUID,
  CONGRATULATIONS_UUID,
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

// 获取当前页面的 location
const location = window.location;

// 烟花效果数据
const fireworks = ref<
  Array<{
    id: number;
    x: number;
    y: number;
    particles: Array<{
      id: number;
      color: string;
      dx: number;
      dy: number;
      delay: number;
    }>;
  }>
>([]);

// 烟花颜色数组
const fireworkColors = [
  "#ff6b6b",
  "#4ecdc4",
  "#45b7d1",
  "#96ceb4",
  "#feca57",
  "#ff9ff3",
  "#54a0ff",
  "#5f27cd",
  "#00d2d3",
  "#ff9f43",
  "#10ac84",
  "#ee5a6f",
  "#c44569",
  "#f8b500",
  "#7bed9f",
];

// 生成烟花效果
const generateFireworks = () => {
  const newFireworks = [];
  for (let i = 0; i < 15; i++) {
    const firework = {
      id: Date.now() + i,
      x: 5 + Math.random() * 90, // 5% 到 95% 的位置
      y: 5 + Math.random() * 70, // 5% 到 75% 的位置
      particles: [] as Array<{
        id: number;
        color: string;
        dx: number;
        dy: number;
        delay: number;
      }>,
    };

    // 为每个烟花生成更多粒子
    for (let j = 0; j < 30; j++) {
      const angle = (j / 30) * Math.PI * 2;
      const velocity = 80 + Math.random() * 120; // 增加粒子扩散距离
      firework.particles.push({
        id: j,
        color:
          fireworkColors[Math.floor(Math.random() * fireworkColors.length)],
        dx: Math.cos(angle) * velocity,
        dy: Math.sin(angle) * velocity,
        delay: i * 0.15 + Math.random() * 0.4,
      });
    }

    newFireworks.push(firework);
  }
  fireworks.value = newFireworks;

  // 6秒后清除烟花
  setTimeout(() => {
    fireworks.value = [];
  }, 6000);
};

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
    case LEVEL_6_UUID:
      return NumberSequenceGame;
    case LEVEL_7_UUID:
      return FriendsGame;
    case LEVEL_8_UUID:
      return MinecraftQuizGame;
    case LEVEL_9_UUID:
      return CatGame;
    case LEVEL_10_UUID:
      return GeographyGame;
    case PUZZLE_1_UUID:
    case PUZZLE_2_UUID:
    case PUZZLE_3_UUID:
    case PUZZLE_4_UUID:
      return GenericPuzzle;
    case CONGRATULATIONS_UUID:
      return CongratulationsView;
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
    generateFireworks(); // 游戏完成后生成烟花
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

// 暂时留下当前关卡
const stayOnCurrentLevel = () => {
  showSuccessModal.value = false; // 隐藏通关成功弹窗
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
