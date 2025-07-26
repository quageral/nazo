<template>
  <!-- 可拖拽的用户图标 - 只在Color关卡显示，绝对定位与LevelView中的👤重合 -->
  <div v-if="isColorLevel" class="fixed top-0 left-0 z-50 w-full">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 lg:py-6">
      <div
        class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 lg:gap-6"
      >
        <div class="flex flex-col sm:flex-row sm:items-center gap-4 sm:gap-6">
          <div class="flex items-center gap-3">
            <div
              class="w-10 h-10 sm:w-12 sm:h-12 bg-gradient-game rounded-full flex items-center justify-center text-white font-bold text-lg sm:text-xl opacity-0"
            >
              5
            </div>
            <div class="opacity-0">
              <h1
                class="text-2xl sm:text-3xl font-bold text-white text-shadow-lg"
              >
                第13关
              </h1>
              <p class="text-lg sm:text-xl text-gray-300 font-medium">Color</p>
            </div>
          </div>
          <!-- 用户名显示 - 可拖拽的版本 -->
          <div
            class="flex items-center gap-2 bg-white/10 px-3 py-2 rounded-md backdrop-blur-sm w-fit"
          >
            <span
              class="text-base sm:text-lg cursor-grab hover:scale-110 transition-transform duration-200 select-none"
              draggable="true"
              @dragstart="handleDragStart"
              @dragend="handleDragEnd"
              :class="{ 'cursor-grabbing opacity-50': isDragging }"
            >
              👤
            </span>
            <span class="text-white font-medium text-sm sm:text-base">{{
              username
            }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div
    class="min-h-screen bg-gradient-to-br from-pink-500 via-purple-600 to-indigo-800 flex items-center justify-center p-4"
  >
    <div
      class="bg-white/95 rounded-2xl p-4 shadow-2xl backdrop-blur-lg max-w-5xl w-full"
    >
      <div class="flex flex-col items-center gap-4">
        <!-- 游戏标题和当前关卡信息 -->
        <div class="text-center">
          <h1
            class="text-3xl font-bold text-gray-800 mb-4 transition-all duration-200"
            :class="{ 'bg-yellow-200 scale-105': isDropZoneActive }"
            @dragover="handleDragOver"
            @dragenter="handleDragEnter"
            @dragleave="handleDragLeave"
            @drop="handleDrop"
          >
            🌼Color🌼
          </h1>
          <div class="flex items-center justify-center gap-6 text-lg">
            <div class="bg-blue-100 px-4 py-2 rounded-xl">
              <span class="text-blue-700 text-lg">当前关卡:</span>
              <span class="text-blue-900 font-bold ml-2 text-xl">{{
                currentLevel
              }}</span>
            </div>
            <div class="bg-purple-100 px-4 py-2 rounded-xl">
              <span class="text-purple-700 text-lg">剩余时间:</span>
              <span class="text-purple-900 font-bold ml-2 text-xl"
                >{{ remainingTime }}s</span
              >
            </div>
          </div>
        </div>

        <!-- 游戏区域 -->
        <div
          class="relative w-full max-w-2xl mx-auto flex items-center justify-center"
        >
          <!-- 色块网格 - 固定大小的容器 -->
          <div
            v-if="gameState === 'playing' || gameState === 'correct'"
            class="grid gap-1 p-4 bg-gray-100 rounded-md shadow-inner mx-auto"
            :style="gridStyle"
            style="width: 500px; height: 500px"
          >
            <div
              v-for="(block, index) in colorBlocks"
              :key="index"
              :style="{ backgroundColor: block.color }"
              @click="handleBlockClick(index)"
              class="cursor-pointer transition-all duration-200 hover:scale-105 hover:shadow-lg rounded-sm border border-gray-300"
              :class="{
                'ring-2 ring-green-400 ring-opacity-60':
                  block.isClicked && block.isDifferent,
                'ring-2 ring-red-400 ring-opacity-60':
                  block.isClicked && !block.isDifferent,
              }"
            ></div>
          </div>

          <!-- 游戏状态显示 -->
          <div
            v-if="gameState === 'waiting'"
            class="flex flex-col items-center justify-center bg-gray-100 rounded-md p-6 mx-auto"
            style="width: 500px; height: 500px"
          >
            <h2 class="text-2xl font-bold text-gray-700 mb-3">准备开始</h2>
            <p class="text-gray-600 mb-4 text-center text-lg">
              找出颜色不同的色块并点击它！<br />
              随着关卡提升，颜色差异会越来越小
            </p>
            <button
              @click="startGame"
              class="px-6 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-md font-bold transition-colors text-2xl"
            >
              开始游戏
            </button>
          </div>

          <div
            v-if="gameState === 'correct'"
            class="absolute inset-0 flex items-center justify-center bg-green-400/90 rounded-md backdrop-blur-sm z-10"
          >
            <div class="text-center text-white">
              <div class="text-6xl mb-4">🎉</div>
              <h2 class="text-3xl font-bold mb-2">找对了！</h2>
              <p class="text-lg">准备下一关...</p>
            </div>
          </div>

          <div
            v-if="gameState === 'wrong'"
            class="flex flex-col items-center justify-center bg-red-100 rounded-md p-6 mx-auto"
            style="width: 500px; height: 500px"
          >
            <div class="text-center">
              <div class="text-5xl mb-4">❌</div>
              <h2 class="text-4xl font-bold mb-3 text-red-700">
                {{ errorReason === "timeout" ? "时间到了！" : "错误的色块！" }}
              </h2>
              <button
                @click="restartGame"
                class="px-12 py-12 bg-red-600 hover:bg-red-700 text-white rounded-md font-semibold transition-colors disabled:opacity-50 text-2xl"
              >
                重新开始
              </button>
            </div>
          </div>

          <div
            v-if="gameState === 'gameOver'"
            class="flex flex-col items-center justify-center bg-red-100 rounded-md p-6 mx-auto"
            style="width: 500px; height: 500px"
          >
            <div class="text-center">
              <div class="text-5xl mb-4">🏆</div>
              <h2 class="text-4xl font-bold mb-3 text-green-700">恭喜通关!</h2>
              <p class="text-sm mb-3 text-green-700">
                您已完成第{{ completedLevels }}关！
              </p>
              <div class="space-y-2">
                <button
                  @click="completeLevel"
                  :disabled="isSubmitting"
                  class="w-full px-4 py-2 bg-green-500 hover:bg-green-600 text-white rounded-md font-semibold transition-colors disabled:opacity-50 text-2xl"
                >
                  {{ isSubmitting ? "提交中..." : "完成关卡" }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 调试面板 (仅开发环境) -->
        <!-- <div v-if="isDevelopment" class="w-full max-w-xl bg-yellow-50 rounded-md border border-yellow-200 p-3">
          <h3 class="text-sm font-semibold text-yellow-800 mb-2">🔧 调试面板</h3>
          <div class="space-y-2">
            <div class="flex items-center justify-between text-xs">
              <span class="text-yellow-700">当前关卡:</span>
              <span class="font-mono text-yellow-900">{{ currentLevel }}</span>
            </div>
            <div class="flex items-center justify-between text-xs">
              <span class="text-yellow-700">游戏状态:</span>
              <span class="font-mono text-yellow-900">{{ gameState }}</span>
            </div>
            <div class="flex items-center justify-between text-xs">
              <span class="text-yellow-700">会话ID:</span>
              <span class="font-mono text-yellow-900 text-xs">{{ sessionId }}</span>
            </div>
            <div class="grid grid-cols-2 gap-1">
              <button @click="setLevel(5)"
                class="px-2 py-1 bg-yellow-600 hover:bg-yellow-700 text-white rounded text-xs">
                跳到第5关
              </button>
              <button @click="setLevel(10)"
                class="px-2 py-1 bg-yellow-600 hover:bg-yellow-700 text-white rounded text-xs">
                跳到第10关
              </button>
              <button @click="completeLevel"
                class="px-2 py-1 bg-green-600 hover:bg-green-700 text-white rounded text-xs col-span-2">
                测试通关检测
              </button>
            </div>
          </div>
        </div> -->
      </div>
    </div>

    <!-- 彩蛋弹窗 -->
    <div
      v-if="showEasterEgg"
      class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
      @click.self="showEasterEgg = false"
    >
      <div class="bg-white rounded-2xl p-6 max-w-md w-full shadow-2xl mx-4">
        <div class="text-center">
          <div class="text-6xl mb-4">🥚</div>
          <h3 class="text-2xl font-bold text-gray-800 mb-4">
            恭喜发现彩蛋！请保存彩蛋码
          </h3>
          <p class="text-gray-600 leading-relaxed mb-6">
            {{ easterEggMessage }}
          </p>
          <button
            @click="showEasterEgg = false"
            class="bg-gradient-to-r from-pink-500 to-purple-600 hover:from-pink-600 hover:to-purple-700 text-white px-6 py-3 rounded-xl font-semibold transition-all duration-200 transform hover:scale-105"
          >
            太棒了！
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from "vue";
import {
  startGame as startGameAPI,
  completeGame,
  getEasterEgg,
} from "@/services/api";
import { COLOR_EASTER_EGG_UUID, LEVEL_5_UUID } from "@/constants/levels";

interface Props {
  levelUuid: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  gameComplete: [
    data: { success: boolean; message: string; nextLevel?: string }
  ];
}>();

// 游戏状态
const gameState = ref<"waiting" | "playing" | "correct" | "wrong" | "gameOver">(
  "waiting"
);
const currentLevel = ref(1);
const completedLevels = ref(0);
const remainingTime = ref(0);
const colorBlocks = ref<
  Array<{ color: string; isDifferent: boolean; isClicked: boolean }>
>([]);
const sessionId = ref("");
const isSubmitting = ref(false);
const errorReason = ref<"timeout" | "wrongClick" | "">("");

// 游戏计时器
let gameTimer: number | null = null;

// 开发环境标识
const isDevelopment = ref(import.meta.env.DEV);

// 获取用户信息
const username = ref(localStorage.getItem("nazo_user") || "");

// 判断是否为Color关卡
const isColorLevel = computed(() => props.levelUuid === LEVEL_5_UUID);

// 拖拽相关状态
const isDragging = ref(false);
const isDropZoneActive = ref(false);
const showEasterEgg = ref(false);
const easterEggMessage = ref("");

// 可拖拽图标的动态定位
const draggableIconStyle = ref({
  left: "0px",
  top: "0px",
  visibility: "hidden" as "hidden" | "visible",
});

// 计算并设置可拖拽图标的位置
const updateDraggableIconPosition = () => {
  if (!isColorLevel.value) return;

  // 等待DOM更新后再计算位置
  nextTick(() => {
    // 尝试找到LevelView中的用户图标
    const levelViewUserIcon = document.querySelector(
      "header .flex.items-center.gap-2 span:first-child"
    );

    if (levelViewUserIcon) {
      const rect = levelViewUserIcon.getBoundingClientRect();
      draggableIconStyle.value = {
        left: `${rect.left}px`,
        top: `${rect.top}px`,
        visibility: "visible",
      };
    } else {
      // 如果找不到原始图标，使用计算的位置作为后备
      draggableIconStyle.value = {
        left: "calc(1rem + 2.5rem + 0.75rem + 3rem + 1.5rem)",
        top: "calc(1rem + 0.5rem)",
        visibility: "visible",
      };
    }
  });
};

// 拖拽事件处理
const handleDragStart = (event: DragEvent) => {
  isDragging.value = true;
  event.dataTransfer?.setData("text/plain", "user-icon");
};

const handleDragEnd = () => {
  isDragging.value = false;
};

const handleDragOver = (event: DragEvent) => {
  event.preventDefault(); // 允许放置
  isDropZoneActive.value = true;
};

const handleDragEnter = () => {
  isDropZoneActive.value = true;
};

const handleDragLeave = () => {
  isDropZoneActive.value = false;
};

const handleDrop = async (event: DragEvent) => {
  event.preventDefault();
  isDropZoneActive.value = false;

  const data = event.dataTransfer?.getData("text/plain");
  if (data === "user-icon") {
    easterEggMessage.value = COLOR_EASTER_EGG_UUID;
    showEasterEgg.value = true;
  }
};

// 计算属性
const gridSize = computed(() => {
  // 初始关卡为2x2 (4个色块)，每关+1
  return currentLevel.value + 1;
});

const gridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${gridSize.value}, 1fr)`,
}));

const gameTime = computed(() => {
  // 基础时间5秒 + 关卡补偿时间n秒
  return 5 + currentLevel.value;
});

// 初始化游戏会话
const initializeGameSession = async () => {
  try {
    const result = await startGameAPI(props.levelUuid);
    if (result.success && result.sessionId) {
      sessionId.value = result.sessionId;
      console.log(
        "Color game session initialized with sessionId:",
        sessionId.value
      );
    } else {
      console.error("Failed to initialize game session:", result.message);
      throw new Error(result.message);
    }
  } catch (error) {
    console.error("Failed to initialize game session:", error);
  }
};

// 生成颜色
const generateColors = () => {
  // 随机生成基础颜色 (RGB)
  const baseR = Math.floor(Math.random() * 256);
  const baseG = Math.floor(Math.random() * 256);
  const baseB = Math.floor(Math.random() * 256);
  const baseColor = `rgb(${baseR}, ${baseG}, ${baseB})`;

  // 计算差异颜色（随关卡提升减小颜色差异值）
  // 初始差异值30，每关卡减少2，最小差异值为6
  const colorDifference = Math.max(6, 30 - (currentLevel.value - 1) * 2);

  // 随机选择要改变的颜色通道
  const channelToChange = Math.floor(Math.random() * 3);
  const direction = Math.random() < 0.5 ? 1 : -1; // 增加或减少

  let diffR = baseR;
  let diffG = baseG;
  let diffB = baseB;

  if (channelToChange === 0) {
    diffR = Math.max(0, Math.min(255, baseR + direction * colorDifference));
  } else if (channelToChange === 1) {
    diffG = Math.max(0, Math.min(255, baseG + direction * colorDifference));
  } else {
    diffB = Math.max(0, Math.min(255, baseB + direction * colorDifference));
  }

  const differentColor = `rgb(${diffR}, ${diffG}, ${diffB})`;

  return { baseColor, differentColor };
};

// 生成色块
const generateColorBlocks = () => {
  const totalBlocks = gridSize.value * gridSize.value;
  const { baseColor, differentColor } = generateColors();

  // 随机选择差异色块的位置
  const differentBlockIndex = Math.floor(Math.random() * totalBlocks);

  const blocks = [];
  for (let i = 0; i < totalBlocks; i++) {
    blocks.push({
      color: i === differentBlockIndex ? differentColor : baseColor,
      isDifferent: i === differentBlockIndex,
      isClicked: false,
    });
  }

  colorBlocks.value = blocks;
};

// 开始游戏
const startGame = async () => {
  // 如果还没有sessionId，初始化会话
  if (!sessionId.value) {
    await initializeGameSession();
  }

  gameState.value = "playing";
  remainingTime.value = gameTime.value;
  generateColorBlocks();

  // 开始倒计时
  gameTimer = setInterval(() => {
    remainingTime.value--;
    if (remainingTime.value <= 0) {
      handleTimeUp();
    }
  }, 1000);
};

// 处理色块点击
const handleBlockClick = (index: number) => {
  if (gameState.value !== "playing") return;

  const block = colorBlocks.value[index];
  block.isClicked = true;

  if (block.isDifferent) {
    // 点击正确
    gameState.value = "correct";
    clearInterval(gameTimer!);

    // 1.5秒后进入下一关
    setTimeout(() => {
      nextLevel();
    }, 1500);
  } else {
    // 点击错误
    errorReason.value = "wrongClick";
    gameState.value = "wrong";
    clearInterval(gameTimer!);
  }
};

// 进入下一关
const nextLevel = () => {
  currentLevel.value++;
  completedLevels.value++;

  if (completedLevels.value >= 10) {
    // 达到通关条件
    gameState.value = "gameOver";
  } else {
    // 继续下一关
    gameState.value = "playing";
    remainingTime.value = gameTime.value;
    generateColorBlocks();

    // 重新开始倒计时
    gameTimer = setInterval(() => {
      remainingTime.value--;
      if (remainingTime.value <= 0) {
        handleTimeUp();
      }
    }, 1000);
  }
};

// 时间到
const handleTimeUp = () => {
  errorReason.value = "timeout";
  gameState.value = "wrong";
  clearInterval(gameTimer!);
};

// 重新开始游戏
const restartGame = () => {
  currentLevel.value = 1;
  completedLevels.value = 0;
  gameState.value = "waiting";
  errorReason.value = "";
  if (gameTimer) {
    clearInterval(gameTimer);
    gameTimer = null;
  }
};

// 完成关卡
const completeLevel = async () => {
  if (isSubmitting.value) return;

  isSubmitting.value = true;

  try {
    const result = await completeGame(props.levelUuid, sessionId.value, {
      gameWon: true,
    });

    if (result.success) {
      emit("gameComplete", {
        success: true,
        message: `恭喜！您成功通过了Color游戏的第${completedLevels.value}关！`,
        nextLevel: result.nextLevel,
      });
    } else {
      alert("通关验证失败，请重试");
    }
  } catch (error) {
    console.error("Failed to complete level:", error);
    alert("网络错误，请重试");
  } finally {
    isSubmitting.value = false;
  }
};

// 调试函数：设置关卡
const setLevel = (level: number) => {
  if (isDevelopment.value && level >= 1 && level <= 10) {
    currentLevel.value = level;
    completedLevels.value = level - 1;
    console.log(`调试：关卡已设置为第${level}关`);
  }
};

// 调试函数：测试通关条件
const testGameComplete = () => {
  if (isDevelopment.value) {
    console.log(
      `当前完成关卡: ${completedLevels.value}, 通关条件: 10关, 是否通关: ${
        completedLevels.value >= 10
      }`
    );
    if (completedLevels.value >= 10) {
      console.log("满足通关条件！");
      gameState.value = "gameOver";
    } else {
      console.log("未满足通关条件");
    }
  }
};

// 组件挂载
onMounted(async () => {
  await initializeGameSession();
  updateDraggableIconPosition(); // 在组件挂载时更新图标位置

  // 添加窗口大小变化监听器
  window.addEventListener("resize", updateDraggableIconPosition);
});

// 组件卸载时清理计时器
onUnmounted(() => {
  if (gameTimer) {
    clearInterval(gameTimer);
  }
  // 清理窗口大小变化监听器
  window.removeEventListener("resize", updateDraggableIconPosition);
});
</script>

<style scoped>
/* 色块会通过CSS Grid自动适应容器大小 */
</style>
