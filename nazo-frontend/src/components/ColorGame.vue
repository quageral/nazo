<template>
  <div
    class="min-h-screen bg-gradient-to-br from-pink-500 via-purple-600 to-indigo-800 flex items-center justify-center p-4"
  >
    <div
      class="bg-white/95 rounded-2xl p-6 shadow-2xl backdrop-blur-lg max-w-4xl w-full"
    >
      <div class="flex flex-col items-center gap-8">
        <!-- 游戏标题和当前关卡信息 -->
        <div class="text-center">
          <h1 class="text-4xl font-bold text-gray-800 mb-2">Color 色块识别</h1>
          <div class="flex items-center justify-center gap-6 text-lg">
            <div class="bg-blue-100 px-4 py-2 rounded-lg">
              <span class="text-blue-700">当前关卡:</span>
              <span class="text-blue-900 font-bold ml-1">{{ currentLevel }}</span>
            </div>
            <div class="bg-green-100 px-4 py-2 rounded-lg">
              <span class="text-green-700">色块数量:</span>
              <span class="text-green-900 font-bold ml-1">{{ gridSize }}×{{ gridSize }}</span>
            </div>
            <div class="bg-purple-100 px-4 py-2 rounded-lg">
              <span class="text-purple-700">剩余时间:</span>
              <span class="text-purple-900 font-bold ml-1">{{ remainingTime }}s</span>
            </div>
          </div>
        </div>

        <!-- 游戏区域 -->
        <div class="relative">
          <!-- 色块网格 -->
          <div
            v-if="gameState === 'playing' || gameState === 'correct'"
            class="grid gap-2 p-4 bg-gray-100 rounded-lg shadow-inner"
            :style="gridStyle"
          >
            <div
              v-for="(block, index) in colorBlocks"
              :key="index"
              :style="{ backgroundColor: block.color }"
              @click="handleBlockClick(index)"
              class="aspect-square cursor-pointer transition-all duration-200 hover:scale-105 hover:shadow-lg rounded-md border border-gray-300"
              :class="{
                'ring-4 ring-green-400 ring-opacity-60': block.isClicked && block.isDifferent,
                'ring-4 ring-red-400 ring-opacity-60': block.isClicked && !block.isDifferent,
              }"
            ></div>
          </div>

          <!-- 游戏状态显示 -->
          <div
            v-if="gameState === 'waiting'"
            class="flex flex-col items-center justify-center bg-gray-100 rounded-lg p-8 min-h-[300px]"
          >
            <h2 class="text-2xl font-bold text-gray-700 mb-4">准备开始</h2>
            <p class="text-gray-600 mb-6 text-center">
              找出颜色不同的色块并点击它！<br />
              随着关卡提升，颜色差异会越来越小
            </p>
            <button
              @click="startGame"
              class="px-8 py-3 bg-blue-500 hover:bg-blue-600 text-white rounded-lg font-semibold transition-colors"
            >
              开始游戏
            </button>
          </div>

          <div
            v-if="gameState === 'correct'"
            class="absolute inset-0 flex items-center justify-center bg-green-400/90 rounded-lg backdrop-blur-sm"
          >
            <div class="text-center text-white">
              <div class="text-6xl mb-4">🎉</div>
              <h2 class="text-2xl font-bold mb-2">找对了！</h2>
              <p class="text-lg">准备下一关...</p>
            </div>
          </div>

          <div
            v-if="gameState === 'wrong'"
            class="absolute inset-0 flex items-center justify-center bg-red-400/90 rounded-lg backdrop-blur-sm"
          >
            <div class="text-center text-white">
              <div class="text-6xl mb-4">❌</div>
              <h2 class="text-2xl font-bold mb-2">答案错误</h2>
              <p class="text-lg mb-4">时间到了或者点击了错误的色块</p>
              <button
                @click="restartGame"
                class="px-6 py-3 bg-white text-red-600 rounded-lg font-semibold hover:bg-gray-100 transition-colors"
              >
                重新开始
              </button>
            </div>
          </div>

          <div
            v-if="gameState === 'gameOver'"
            class="absolute inset-0 flex items-center justify-center bg-gray-900/90 rounded-lg backdrop-blur-sm"
          >
            <div class="text-center text-white">
              <div class="text-6xl mb-4">🏆</div>
              <h2 class="text-2xl font-bold mb-2">恭喜通关！</h2>
              <p class="text-lg mb-4">您已完成第{{ completedLevels }}关！</p>
              <div class="space-y-3">
                <button
                  v-if="completedLevels >= 10"
                  @click="completeLevel"
                  :disabled="isSubmitting"
                  class="w-full px-6 py-3 bg-green-500 hover:bg-green-600 text-white rounded-lg font-semibold transition-colors disabled:opacity-50"
                >
                  {{ isSubmitting ? '提交中...' : '完成关卡' }}
                </button>
                <button
                  v-else
                  @click="restartGame"
                  class="w-full px-6 py-3 bg-blue-500 hover:bg-blue-600 text-white rounded-lg font-semibold transition-colors"
                >
                  继续挑战
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作说明 -->
        <div class="text-center text-gray-600 max-w-2xl">
          <p class="text-sm">
            游戏规则：在限定时间内找出颜色不同的色块。每通过一关，色块数量增加，颜色差异减小。
          </p>
          <p class="text-sm mt-1">
            目标：通过第10关即可完成整个关卡！
          </p>
        </div>

        <!-- 调试面板 (仅开发环境) -->
        <div
          v-if="isDevelopment"
          class="w-full max-w-2xl bg-yellow-50 rounded-lg border border-yellow-200 p-4"
        >
          <h3 class="text-lg font-semibold text-yellow-800 mb-3">🔧 调试面板</h3>
          <div class="space-y-3">
            <div class="flex items-center justify-between text-sm">
              <span class="text-yellow-700">当前关卡:</span>
              <span class="font-mono text-yellow-900">{{ currentLevel }}</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-yellow-700">游戏状态:</span>
              <span class="font-mono text-yellow-900">{{ gameState }}</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-yellow-700">会话ID:</span>
              <span class="font-mono text-yellow-900 text-xs">{{ sessionId }}</span>
            </div>
            <div class="grid grid-cols-2 gap-2">
              <button
                @click="setLevel(5)"
                class="px-3 py-2 bg-yellow-600 hover:bg-yellow-700 text-white rounded text-sm"
              >
                跳到第5关
              </button>
              <button
                @click="setLevel(10)"
                class="px-3 py-2 bg-yellow-600 hover:bg-yellow-700 text-white rounded text-sm"
              >
                跳到第10关
              </button>
              <button
                @click="testGameComplete"
                class="px-3 py-2 bg-green-600 hover:bg-green-700 text-white rounded text-sm col-span-2"
              >
                测试通关检测
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { startGame as startGameAPI, completeGame } from "@/services/api";

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
const gameState = ref<"waiting" | "playing" | "correct" | "wrong" | "gameOver">("waiting");
const currentLevel = ref(1);
const completedLevels = ref(0);
const remainingTime = ref(0);
const colorBlocks = ref<Array<{ color: string; isDifferent: boolean; isClicked: boolean }>>([]);
const sessionId = ref("");
const isSubmitting = ref(false);

// 游戏计时器
let gameTimer: number | null = null;

// 开发环境标识
const isDevelopment = ref(import.meta.env.DEV);

// 获取用户信息
const username = ref(localStorage.getItem("nazo_user") || "");

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
      console.log("Color game session initialized with sessionId:", sessionId.value);
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
  gameState.value = "wrong";
  clearInterval(gameTimer!);
};

// 重新开始游戏
const restartGame = () => {
  currentLevel.value = 1;
  completedLevels.value = 0;
  gameState.value = "waiting";
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
      level: completedLevels.value,
      score: completedLevels.value * 10,
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
    console.log(`当前完成关卡: ${completedLevels.value}, 通关条件: 10关, 是否通关: ${completedLevels.value >= 10}`);
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
});

// 组件卸载时清理计时器
onUnmounted(() => {
  if (gameTimer) {
    clearInterval(gameTimer);
  }
});
</script>

<style scoped>
.aspect-square {
  aspect-ratio: 1 / 1;
}
</style> 