<template>
  <div
    class="min-h-screen bg-gradient-to-br from-indigo-500 via-purple-600 to-purple-800 flex items-center justify-center p-4"
  >
    <div
      class="bg-white/95 rounded-2xl p-6 shadow-2xl backdrop-blur-lg max-w-6xl w-full"
    >
      <div class="flex flex-col lg:flex-row gap-8 items-start justify-center">
        <!-- 散点图区域 -->
        <div class="relative">
          <div
            class="bg-white rounded-lg p-4 shadow-lg border-4 border-gray-300"
          >
            <canvas
              ref="scatterPlot"
              width="400"
              height="400"
              class="border border-gray-200 rounded bg-gray-50"
            ></canvas>
          </div>
        </div>

        <!-- 游戏信息面板 -->
        <div class="min-w-[300px] space-y-4">
          <!-- 资源显示区 -->
          <div class="flex gap-4 mb-6">
            <div
              class="bg-white/80 p-3 rounded-lg shadow-md flex items-center space-x-2"
            >
              <span class="text-2xl">❤️</span>
              <span class="text-xl font-bold text-red-600">{{ lives }}</span>
            </div>
            <div
              class="bg-white/80 p-3 rounded-lg shadow-md flex items-center space-x-2"
            >
              <span class="text-2xl">💰</span>
              <span class="text-xl font-bold text-yellow-600">{{ coins }}</span>
            </div>
          </div>

          <!-- 连击和平均误差 -->
          <div class="bg-white/80 p-4 rounded-lg shadow-md">
            <h3
              class="text-sm font-bold text-gray-600 mb-2 uppercase tracking-wide"
            >
              STREAKS
            </h3>
            <div class="text-2xl font-bold text-gray-800 text-center">
              {{ streaks }}
            </div>
          </div>

          <div class="bg-white/80 p-4 rounded-lg shadow-md">
            <h3
              class="text-sm font-bold text-gray-600 mb-2 uppercase tracking-wide"
            >
              SCORE
            </h3>
            <div class="text-2xl font-bold text-gray-800 text-center">
              {{ score }}
            </div>
          </div>

          <div class="bg-white/80 p-4 rounded-lg shadow-md">
            <h3
              class="text-sm font-bold text-gray-600 mb-2 uppercase tracking-wide"
            >
              MEAN ERROR
            </h3>
            <div class="text-2xl font-bold text-gray-800 text-center">
              {{ meanError.toFixed(3) }}
            </div>
          </div>

          <!-- 游戏状态区域 -->
          <div class="bg-white/80 p-4 rounded-lg shadow-md min-h-[200px]">
            <!-- 等待输入状态 -->
            <div v-if="gameState === 'waiting'" class="space-y-4">
              <h3 class="text-lg font-bold text-gray-700 text-center">
                GUESS THE CORRELATION
              </h3>
              <p class="text-sm text-gray-600 text-center">
                Enter a value between 0 and 1
              </p>
              <div class="space-y-3">
                <input
                  v-model="guessInput"
                  type="number"
                  step="0.001"
                  min="0"
                  max="1"
                  placeholder="0.000"
                  class="w-full px-4 py-3 text-lg border-2 border-gray-300 rounded-lg focus:border-blue-500 focus:outline-none text-center"
                  @keyup.enter="submitGuess"
                />
                <button
                  @click="submitGuess"
                  :disabled="!isValidGuess"
                  class="w-full py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-400 disabled:cursor-not-allowed transform hover:-translate-y-1 transition-all duration-300 font-bold uppercase tracking-wide shadow-lg"
                >
                  SUBMIT GUESS
                </button>
              </div>
            </div>

            <!-- 猜对状态 -->
            <div
              v-else-if="gameState === 'correct'"
              class="space-y-4 text-center"
            >
              <h3 class="text-xl font-bold text-green-600">CORRECT! 🎉</h3>
              <div class="space-y-2 text-sm">
                <p><strong>TRUE R:</strong> {{ trueR.toFixed(3) }}</p>
                <p><strong>GUESSED R:</strong> {{ lastGuess.toFixed(3) }}</p>
                <p>
                  <strong>DIFFERENCE:</strong>
                  {{ Math.abs(trueR - lastGuess).toFixed(3) }}
                </p>
              </div>
              <div class="text-lg">
                <span class="text-red-500">❤️+1</span>
                <span class="text-yellow-500 ml-2">💰+{{ lastReward }}</span>
              </div>
              <button
                @click="nextRound"
                class="w-full py-3 bg-green-500 text-white rounded-lg hover:bg-green-600 transform hover:-translate-y-1 transition-all duration-300 font-bold uppercase tracking-wide shadow-lg"
              >
                NEXT
              </button>
            </div>

            <!-- 猜错状态 -->
            <div
              v-else-if="gameState === 'wrong'"
              class="space-y-4 text-center"
            >
              <h3 class="text-xl font-bold text-red-600">WRONG! 😢</h3>
              <div class="space-y-2 text-sm">
                <p><strong>TRUE R:</strong> {{ trueR.toFixed(3) }}</p>
                <p><strong>GUESSED R:</strong> {{ lastGuess.toFixed(3) }}</p>
                <p>
                  <strong>DIFFERENCE:</strong>
                  {{ Math.abs(trueR - lastGuess).toFixed(3) }}
                </p>
              </div>
              <div class="text-lg">
                <span class="text-red-500">❤️-1</span>
              </div>
              <button
                @click="nextRound"
                class="w-full py-3 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transform hover:-translate-y-1 transition-all duration-300 font-bold uppercase tracking-wide shadow-lg"
              >
                NEXT
              </button>
            </div>

            <!-- 游戏结束状态 -->
            <div
              v-else-if="gameState === 'gameOver'"
              class="space-y-4 text-center"
            >
              <h2 class="text-2xl font-bold text-red-600">GAME OVER</h2>
              <div class="space-y-2">
                <p class="text-lg"><strong>SCORE:</strong> {{ score }}</p>
                <p class="text-lg"><strong>BEST:</strong> {{ bestScore }}</p>
                <p
                  v-if="isNewHighScore"
                  class="text-xl font-bold text-yellow-600 animate-pulse"
                >
                  NEW HIGHSCORE! 🏆
                </p>
              </div>
              <button
                @click="playAgain"
                class="w-full py-3 bg-purple-500 text-white rounded-lg hover:bg-purple-600 transform hover:-translate-y-1 transition-all duration-300 font-bold uppercase tracking-wide shadow-lg"
              >
                PLAY AGAIN
              </button>
            </div>
          </div>

          <!-- 调试面板 (仅开发环境) -->
          <div
            v-if="isDevelopment"
            class="bg-white/80 p-4 rounded-lg shadow-md border-2 border-red-500/30"
          >
            <h3
              class="text-lg font-bold text-red-600 mb-4 flex items-center space-x-2"
            >
              <span>🐛</span>
              <span>调试面板</span>
            </h3>

            <div class="space-y-3">
              <!-- 快速设置分数按钮 -->
              <div class="grid grid-cols-2 gap-2">
                <button
                  @click="setScore(10)"
                  class="px-3 py-2 bg-yellow-600 hover:bg-yellow-700 text-white rounded text-sm"
                >
                  10分
                </button>
                <button
                  @click="setScore(30)"
                  class="px-3 py-2 bg-yellow-600 hover:bg-yellow-700 text-white rounded text-sm"
                >
                  30分
                </button>
                <button
                  @click="setScore(50)"
                  class="px-3 py-2 bg-green-600 hover:bg-green-700 text-white rounded text-sm"
                >
                  50分(通关)
                </button>
                <button
                  @click="setScore(100)"
                  class="px-3 py-2 bg-green-600 hover:bg-green-700 text-white rounded text-sm"
                >
                  100分
                </button>
              </div>

              <!-- 直接输入分数 -->
              <div class="flex space-x-2">
                <input
                  v-model.number="debugScore"
                  type="number"
                  placeholder="输入分数"
                  class="flex-1 px-3 py-2 bg-gray-200 border border-gray-300 rounded text-black text-sm"
                />
                <button
                  @click="setScore(debugScore)"
                  class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded text-sm"
                >
                  设置
                </button>
              </div>

              <!-- 生命值控制 -->
              <div class="grid grid-cols-3 gap-2">
                <button
                  @click="setLives(1)"
                  class="px-3 py-2 bg-red-600 hover:bg-red-700 text-white rounded text-sm"
                >
                  1❤️
                </button>
                <button
                  @click="setLives(2)"
                  class="px-3 py-2 bg-orange-600 hover:bg-orange-700 text-white rounded text-sm"
                >
                  2❤️
                </button>
                <button
                  @click="setLives(3)"
                  class="px-3 py-2 bg-green-600 hover:bg-green-700 text-white rounded text-sm"
                >
                  3❤️
                </button>
              </div>

              <!-- 测试功能 -->
              <button
                @click="testGameComplete"
                class="w-full px-3 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded text-sm"
              >
                测试通关检测
              </button>

              <!-- 游戏状态重置 -->
              <button
                @click="resetGame"
                class="w-full px-3 py-2 bg-gray-600 hover:bg-gray-700 text-white rounded text-sm"
              >
                重置游戏状态
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 错误弹窗 -->
    <div
      v-if="showErrorModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 backdrop-blur-sm"
    >
      <div class="bg-white rounded-2xl p-6 shadow-2xl max-w-md w-full mx-4">
        <div class="text-center">
          <div class="text-6xl mb-4">⚠️</div>
          <h2 class="text-2xl font-bold text-red-600 mb-4">请求失败</h2>
          <p class="text-gray-600 mb-6">{{ errorMessage }}</p>
          <button
            @click="closeErrorModal"
            class="px-6 py-3 bg-red-500 text-white rounded-lg hover:bg-red-600 transform hover:-translate-y-1 transition-all duration-300 font-bold uppercase tracking-wide shadow-lg"
          >
            确定
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { startGame, completeGame } from "@/services/api";

const router = useRouter();
const route = useRoute();

// 定义 props
interface Props {
  levelUuid: string;
}

const props = defineProps<Props>();

// 定义 emit 事件
const emit = defineEmits<{
  gameComplete: [
    data: { success: boolean; message: string; nextLevel?: string }
  ];
}>();

// 获取用户信息和会话
const username = ref(localStorage.getItem("nazo_user") || "");
const sessionId = ref("");

// 游戏状态
const gameState = ref<"waiting" | "correct" | "wrong" | "gameOver">("waiting");
const lives = ref(3);
const coins = ref(0);
const streaks = ref(0);
const meanError = ref(0);
const score = ref(0);
const bestScore = ref(0);
const isNewHighScore = ref(false);

// 散点图数据
const scatterData = ref<{ x: number; y: number }[]>([]);
const trueR = ref(0);
const guessInput = ref("");
const lastGuess = ref(0);
const lastReward = ref(0);

// 统计数据
const totalGuesses = ref(0);
const totalError = ref(0);

// 调试相关变量
const isDevelopment = ref(import.meta.env.DEV);
const debugScore = ref(0);

// 错误处理状态
const showErrorModal = ref(false);
const errorMessage = ref("");

const scatterPlot = ref<HTMLCanvasElement>();

// 计算属性
const isValidGuess = computed(() => {
  const value = parseFloat(guessInput.value);
  return !isNaN(value) && value >= 0 && value <= 1;
});

// 实时监听分数变化，达到阈值自动通关
watch(score, (newScore) => {
  if (
    newScore >= 50 &&
    sessionId.value &&
    username.value &&
    gameState.value !== "gameOver"
  ) {
    // 延迟一点再检测，避免在正在提交时重复触发
    setTimeout(() => {
      autoCompleteGame();
    }, 500);
  }
});

// 初始化游戏
onMounted(async () => {
  loadBestScore();
  generateNewScatterPlot();

  // 如果是通过关卡系统进入，初始化游戏会话
  if (props.levelUuid && username.value) {
    try {
      const response = await startGame(props.levelUuid);
      if (response.success && response.sessionId) {
        sessionId.value = response.sessionId;
      }
    } catch (error) {
      console.error("Failed to start game session:", error);
    }
  }
});

// 生成新的散点图
function generateNewScatterPlot() {
  const points: { x: number; y: number }[] = [];
  const numPoints = 30 + Math.floor(Math.random() * 20); // 30-50个点

  // 随机生成相关性强度
  const correlationStrength = Math.random() * 2 - 1; // -1 to 1
  const noise = 0.1 + Math.random() * 0.4; // 噪声水平

  for (let i = 0; i < numPoints; i++) {
    const x = Math.random();
    // 基于相关性生成y值，加上噪声
    let y =
      correlationStrength * x +
      (1 - Math.abs(correlationStrength)) * Math.random();
    y += (Math.random() - 0.5) * noise;

    // 确保y在0-1范围内
    y = Math.max(0, Math.min(1, y));

    points.push({ x, y });
  }

  scatterData.value = points;
  trueR.value = calculateCorrelation(points);

  nextTick(() => {
    drawScatterPlot();
  });
}

// 计算皮尔逊相关系数
function calculateCorrelation(points: { x: number; y: number }[]): number {
  const n = points.length;
  if (n === 0) return 0;

  const sumX = points.reduce((sum, p) => sum + p.x, 0);
  const sumY = points.reduce((sum, p) => sum + p.y, 0);
  const sumXY = points.reduce((sum, p) => sum + p.x * p.y, 0);
  const sumX2 = points.reduce((sum, p) => sum + p.x * p.x, 0);
  const sumY2 = points.reduce((sum, p) => sum + p.y * p.y, 0);

  const numerator = n * sumXY - sumX * sumY;
  const denominator = Math.sqrt(
    (n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY)
  );

  if (denominator === 0) return 0;
  return numerator / denominator;
}

// 绘制散点图
function drawScatterPlot() {
  if (!scatterPlot.value) return;

  const canvas = scatterPlot.value;
  const ctx = canvas.getContext("2d");
  if (!ctx) return;

  // 清空画布
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // 绘制坐标轴
  ctx.strokeStyle = "#666";
  ctx.lineWidth = 2;

  // X轴
  ctx.beginPath();
  ctx.moveTo(40, canvas.height - 40);
  ctx.lineTo(canvas.width - 20, canvas.height - 40);
  ctx.stroke();

  // Y轴
  ctx.beginPath();
  ctx.moveTo(40, 20);
  ctx.lineTo(40, canvas.height - 40);
  ctx.stroke();

  // 绘制刻度标签
  ctx.fillStyle = "#666";
  ctx.font = "12px Arial";
  ctx.textAlign = "center";

  // X轴标签
  ctx.fillText("0", 40, canvas.height - 20);
  ctx.fillText("0.5", (canvas.width - 60) / 2 + 40, canvas.height - 20);
  ctx.fillText("1", canvas.width - 20, canvas.height - 20);

  // Y轴标签
  ctx.textAlign = "right";
  ctx.fillText("0", 30, canvas.height - 35);
  ctx.fillText("0.5", 30, (canvas.height - 60) / 2 + 20);
  ctx.fillText("1", 30, 25);

  // 绘制散点
  const plotWidth = canvas.width - 60;
  const plotHeight = canvas.height - 60;

  ctx.fillStyle = "#3b82f6";
  scatterData.value.forEach((point) => {
    const x = 40 + point.x * plotWidth;
    const y = canvas.height - 40 - point.y * plotHeight;

    ctx.beginPath();
    ctx.arc(x, y, 4, 0, 2 * Math.PI);
    ctx.fill();
  });
}

// 提交猜测
function submitGuess() {
  if (!isValidGuess.value) return;

  const guess = parseFloat(guessInput.value);
  lastGuess.value = guess;
  const difference = Math.abs(trueR.value - guess);

  // 更新统计
  totalGuesses.value++;
  totalError.value += difference;
  meanError.value = totalError.value / totalGuesses.value;

  // 判断正确性（误差小于0.1算正确）
  const threshold = 0.1;
  if (difference <= threshold) {
    // 猜对
    lives.value = Math.min(lives.value + 1, 3); // 限制最大生命值为3
    const reward = Math.max(1, Math.floor((threshold - difference) * 50));
    coins.value += reward;
    lastReward.value = reward;
    streaks.value++;
    score.value += 10 + reward;
    gameState.value = "correct";
  } else {
    // 猜错
    lives.value--;
    streaks.value = 0;
    gameState.value = "wrong";

    if (lives.value <= 0) {
      gameState.value = "gameOver";
      checkNewHighScore();
    }
  }

  guessInput.value = "";
}

// 下一轮
function nextRound() {
  if (lives.value <= 0) {
    gameState.value = "gameOver";
    return;
  }

  generateNewScatterPlot();
  gameState.value = "waiting";
}

// 自动完成游戏（达到阈值时调用）
const isSubmitting = ref(false);

async function autoCompleteGame() {
  if (isSubmitting.value) return;

  isSubmitting.value = true;

  try {
    const response = await completeGame(
      props.levelUuid,
      sessionId.value,
      score.value
    );

    if (response.success && response.nextLevel) {
      // 发送游戏完成事件
      emit("gameComplete", {
        success: true,
        message: `恭喜！您以 ${score.value} 分的成绩通关了！`,
        nextLevel: response.nextLevel,
      });
    } else {
      // 显示服务器返回的错误信息
      showErrorMessage(response.message || "游戏完成请求失败，请稍后重试");
    }
  } catch (error: any) {
    console.error("Failed to complete game:", error);
    // 显示网络或其他错误
    const errorMsg =
      error?.message ||
      error?.response?.data?.error ||
      "网络连接失败，请检查网络后重试";
    showErrorMessage(errorMsg);
  } finally {
    isSubmitting.value = false;
  }
}

// 检查新记录和通关条件
async function checkNewHighScore() {
  if (score.value > bestScore.value) {
    bestScore.value = score.value;
    isNewHighScore.value = true;
    saveBestScore();
  } else {
    isNewHighScore.value = false;
  }

  // 检查是否达到通关条件（50分）并且有有效会话
  if (score.value >= 50 && sessionId.value && username.value) {
    autoCompleteGame();
  }
}

// 重新开始游戏
function playAgain() {
  lives.value = 3;
  coins.value = 0;
  streaks.value = 0;
  score.value = 0;
  totalGuesses.value = 0;
  totalError.value = 0;
  meanError.value = 0;
  isNewHighScore.value = false;
  generateNewScatterPlot();
  gameState.value = "waiting";
}

// 返回主菜单
function goToMainMenu() {
  router.push("/login");
}

// 本地存储最佳成绩
function saveBestScore() {
  localStorage.setItem(
    "correlation_game_best_score",
    bestScore.value.toString()
  );
}

function loadBestScore() {
  const saved = localStorage.getItem("correlation_game_best_score");
  if (saved) {
    bestScore.value = parseInt(saved);
  }
}

// 调试函数：设置分数
function setScore(newScore: number) {
  if (isDevelopment.value && typeof newScore === "number" && newScore >= 0) {
    score.value = newScore;
    console.log(`调试：分数已设置为 ${newScore}`);
  }
}

// 调试函数：设置生命值
function setLives(newLives: number) {
  if (
    isDevelopment.value &&
    typeof newLives === "number" &&
    newLives >= 0 &&
    newLives <= 3
  ) {
    lives.value = newLives;
    console.log(`调试：生命值已设置为 ${newLives}`);
  }
}

// 调试函数：测试通关检测
function testGameComplete() {
  if (isDevelopment.value) {
    console.log(
      `当前分数: ${score.value}, 通关条件: 50, 是否通关: ${score.value >= 50}`
    );
    if (score.value >= 50) {
      console.log("满足通关条件！");
      autoCompleteGame();
    } else {
      console.log("未满足通关条件");
    }
  }
}

// 调试函数：重置游戏状态
function resetGame() {
  if (isDevelopment.value) {
    playAgain();
    console.log("游戏状态已重置");
  }
}

// 关闭错误弹窗
function closeErrorModal() {
  showErrorModal.value = false;
  errorMessage.value = "";
}

// 显示错误弹窗
function showErrorMessage(message: string) {
  errorMessage.value = message;
  showErrorModal.value = true;
}
</script>

<style scoped>
.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
