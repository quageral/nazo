<template>
  <div class="h-full flex items-center justify-center p-4">
    <div class="game-card w-full max-w-7xl h-full">
      <div
        class="flex flex-col xl:flex-row gap-12 h-full items-center justify-center"
      >
        <!-- 游戏区域 -->
        <div class="flex items-center justify-center">
          <div class="relative">
            <canvas
              ref="gameBoard"
              width="450"
              height="900"
              class="border-4 border-primary rounded-2xl bg-gray-900 shadow-2xl mx-auto"
              style="image-rendering: pixelated"
            ></canvas>

            <!-- 游戏结束弹窗 -->
            <div
              v-if="gameOver"
              class="absolute inset-0 flex items-center justify-center bg-black/80 rounded-2xl backdrop-blur-sm z-10"
            >
              <div class="game-card text-center max-w-md">
                <div class="text-6xl mb-6">{{ hasWon ? "🎉" : "💥" }}</div>
                <h2 class="text-4xl font-black text-white mb-6">
                  {{ hasWon ? "恭喜通关！" : "游戏结束" }}
                </h2>

                <div class="space-y-4 mb-8">
                  <div class="flex justify-between items-center text-xl">
                    <span class="text-gray-300">最终得分:</span>
                    <span class="text-yellow-400 font-bold">{{
                      score.toLocaleString()
                    }}</span>
                  </div>
                  <div class="flex justify-between items-center text-xl">
                    <span class="text-gray-300">通关条件:</span>
                    <span class="text-yellow-400 font-bold"
                      >{{ winCondition }} 分</span
                    >
                  </div>
                </div>

                <!-- 根据通关条件显示不同按钮 -->
                <div class="space-y-4">
                  <button
                    v-if="hasWon"
                    @click="completeLevel"
                    :disabled="isSubmitting"
                    class="w-full game-button bg-gradient-to-r from-green-500 to-green-600 text-white disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none flex items-center justify-center space-x-3"
                  >
                    <span
                      v-if="isSubmitting"
                      class="flex items-center space-x-2"
                    >
                      <div
                        class="animate-spin rounded-full h-5 w-5 border-b-2 border-white"
                      ></div>
                      <span>提交中...</span>
                    </span>
                    <span v-else class="flex items-center space-x-2">
                      <span>🏆</span>
                      <span>提交通关</span>
                    </span>
                  </button>

                  <button
                    @click="restartGame"
                    class="w-full game-button bg-gradient-to-r from-blue-500 to-blue-600 text-white flex items-center justify-center space-x-3"
                  >
                    <span>🔄</span>
                    <span>重新开始</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 信息面板 -->
        <div class="xl:w-96 space-y-6">
          <!-- 通关条件说明 -->
          <div class="glass-card p-6 border-2 border-yellow-500/30">
            <h3
              class="text-xl font-bold text-yellow-300 mb-3 flex items-center space-x-2"
            >
              <span class="text-2xl">🎯</span>
              <span>通关条件</span>
            </h3>
            <p class="text-yellow-200 text-lg">
              达到
              <span class="font-bold text-yellow-100">{{ winCondition }}</span>
              分即可通关
            </p>
          </div>

          <!-- 游戏状态面板 -->
          <div class="grid grid-cols-1 gap-4">
            <!-- 得分 -->
            <div
              class="glass-card p-6 text-center"
              :class="hasWon ? 'border-2 border-green-400 animate-glow' : ''"
            >
              <h3
                class="text-lg font-bold mb-2 flex items-center justify-center space-x-2"
                :class="hasWon ? 'text-green-300' : 'text-blue-300'"
              >
                <span>{{ hasWon ? "✅" : "🏆" }}</span>
                <span>得分</span>
              </h3>
              <div
                class="text-4xl font-black"
                :class="hasWon ? 'text-green-400' : 'text-white'"
              >
                {{ score.toLocaleString() }}
              </div>
              <div
                v-if="hasWon"
                class="mt-2 text-green-300 font-semibold animate-pulse"
              >
                🎉 可以通关了！
              </div>
            </div>
          </div>

          <!-- 下一个方块 -->
          <div class="glass-card p-6">
            <h3
              class="text-lg font-bold text-cyan-300 mb-4 flex items-center justify-center space-x-2"
            >
              <span>👀</span>
              <span>下一个方块</span>
            </h3>
            <canvas
              ref="nextPiece"
              width="180"
              height="180"
              class="border-2 border-cyan-500/30 rounded-xl bg-gray-900 mx-auto block"
              style="image-rendering: pixelated"
            ></canvas>
          </div>

          <!-- 操作说明 -->
          <div class="glass-card p-6">
            <h3
              class="text-lg font-bold text-gray-300 mb-4 flex items-center space-x-2"
            >
              <span>🎮</span>
              <span>操作说明</span>
            </h3>
            <div class="grid grid-cols-2 gap-3 text-gray-300">
              <div class="flex items-center space-x-2">
                <span class="bg-gray-700 px-3 py-2 rounded text-lg font-mono"
                  >←→</span
                >
                <span>移动</span>
              </div>
              <div class="flex items-center space-x-2">
                <span class="bg-gray-700 px-3 py-2 rounded text-lg font-mono"
                  >↓</span
                >
                <span>快降</span>
              </div>
              <div class="flex items-center space-x-2">
                <span class="bg-gray-700 px-3 py-2 rounded text-base font-mono"
                  >空格</span
                >
                <span>旋转</span>
              </div>
              <div class="flex items-center space-x-2">
                <span class="bg-gray-700 px-3 py-2 rounded text-lg font-mono"
                  >P</span
                >
                <span>暂停</span>
              </div>
            </div>
          </div>

          <!-- 控制按钮 -->
          <div class="space-y-4">
            <button
              v-if="!gameRunning"
              @click="startGame"
              class="w-full game-button bg-gradient-to-r from-green-500 to-green-600 text-white flex items-center justify-center space-x-3"
            >
              <span class="text-xl">🚀</span>
              <span>开始游戏</span>
            </button>

            <button
              v-if="gameRunning"
              @click="togglePause"
              class="w-full game-button bg-gradient-to-r from-orange-500 to-orange-600 text-white flex items-center justify-center space-x-3"
            >
              <span class="text-xl">{{ gamePaused ? "▶️" : "⏸️" }}</span>
              <span>{{ gamePaused ? "继续游戏" : "暂停游戏" }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from "vue";

interface Props {
  levelUuid: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  gameComplete: [
    data: { success: boolean; message: string; nextLevel?: string }
  ];
}>();

// 模板引用
const gameBoard = ref<HTMLCanvasElement | null>(null);
const nextPiece = ref<HTMLCanvasElement | null>(null);

// 游戏状态
const score = ref(0);
const gameRunning = ref(false);
const gamePaused = ref(false);
const gameOver = ref(false);
const isSubmitting = ref(false);

// 通关条件：得分超过600
const winCondition = 600;

// 判断是否已通关
const hasWon = computed(() => score.value >= winCondition);

// 游戏配置
const BOARD_WIDTH = 10;
const BOARD_HEIGHT = 20;
const BLOCK_SIZE = 45; // 增大方块尺寸以适应更大的canvas

// 游戏变量
let ctx: CanvasRenderingContext2D | null = null;
let nextCtx: CanvasRenderingContext2D | null = null;
let board: number[][] = [];
let currentPiece: any = null;
let nextPieceData: any = null;
let gameLoop: number | null = null;
let dropTimer = 0;
let dropInterval = 1000;
let sessionId = ""; // 防作弊校验ID

// 方块形状定义
const pieces = {
  I: [[[1, 1, 1, 1]], [[1], [1], [1], [1]]],
  O: [
    [
      [1, 1],
      [1, 1],
    ],
  ],
  T: [
    [
      [0, 1, 0],
      [1, 1, 1],
    ],
    [
      [1, 0],
      [1, 1],
      [1, 0],
    ],
    [
      [1, 1, 1],
      [0, 1, 0],
    ],
    [
      [0, 1],
      [1, 1],
      [0, 1],
    ],
  ],
  S: [
    [
      [0, 1, 1],
      [1, 1, 0],
    ],
    [
      [1, 0],
      [1, 1],
      [0, 1],
    ],
  ],
  Z: [
    [
      [1, 1, 0],
      [0, 1, 1],
    ],
    [
      [0, 1],
      [1, 1],
      [1, 0],
    ],
  ],
  J: [
    [
      [1, 0, 0],
      [1, 1, 1],
    ],
    [
      [1, 1],
      [1, 0],
      [1, 0],
    ],
    [
      [1, 1, 1],
      [0, 0, 1],
    ],
    [
      [0, 1],
      [0, 1],
      [1, 1],
    ],
  ],
  L: [
    [
      [0, 0, 1],
      [1, 1, 1],
    ],
    [
      [1, 0],
      [1, 0],
      [1, 1],
    ],
    [
      [1, 1, 1],
      [1, 0, 0],
    ],
    [
      [1, 1],
      [0, 1],
      [0, 1],
    ],
  ],
};

// 方块颜色
const colors: { [key: string]: string } = {
  I: "#00f5ff",
  O: "#ffff00",
  T: "#a000ff",
  S: "#00ff00",
  Z: "#ff0000",
  J: "#0000ff",
  L: "#ff8000",
};

// 监听得分变化，自动提交通关
watch(score, (newScore) => {
  if (newScore >= winCondition && gameRunning.value && !isSubmitting.value) {
    // 延迟一点再检测，避免频繁触发
    setTimeout(() => {
      if (
        score.value >= winCondition &&
        gameRunning.value &&
        !isSubmitting.value
      ) {
        autoCompleteLevel();
      }
    }, 1000);
  }
});

// 生成会话ID（简单版本，实际应由后端生成）
const generateSessionId = () => {
  return (
    Math.random().toString(36).substring(2, 15) +
    Math.random().toString(36).substring(2, 15)
  );
};

// 初始化游戏板
const createBoard = () => {
  return Array(BOARD_HEIGHT)
    .fill(null)
    .map(() => Array(BOARD_WIDTH).fill(0));
};

// 创建新方块
const createPiece = () => {
  const types = Object.keys(pieces) as Array<keyof typeof pieces>;
  const type = types[Math.floor(Math.random() * types.length)];
  return {
    type,
    shape: pieces[type][0],
    x: Math.floor(BOARD_WIDTH / 2) - 1,
    y: 0,
    rotation: 0,
  };
};

// 开始游戏
const startGame = async () => {
  gameRunning.value = true;
  gamePaused.value = false;
  gameOver.value = false;
  board = createBoard();
  score.value = 0;
  dropInterval = 1000;
  sessionId = generateSessionId();

  // 调用后端开始游戏
  try {
    const response = await fetch(`http://localhost:8080/api/game/start`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        levelUuid: props.levelUuid,
      }),
    });

    const result = await response.json();
    if (result.success) {
      sessionId = result.sessionId;
    }
  } catch (error) {
    console.error("Failed to start game session:", error);
  }

  currentPiece = createPiece();
  nextPieceData = createPiece();

  if (gameLoop) {
    clearInterval(gameLoop);
  }
  gameLoop = setInterval(update, 50);
};

// 暂停/继续游戏
const togglePause = () => {
  if (!gameRunning.value) return;
  gamePaused.value = !gamePaused.value;
};

// 重新开始游戏
const restartGame = () => {
  stopGame();
  startGame();
};

// 停止游戏
const stopGame = () => {
  gameRunning.value = false;
  gamePaused.value = false;
  if (gameLoop) {
    clearInterval(gameLoop);
    gameLoop = null;
  }
};

// 自动提交通关
const autoCompleteLevel = async () => {
  if (isSubmitting.value) return;

  isSubmitting.value = true;

  try {
    const response = await fetch(`http://localhost:8080/api/game/complete`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        levelUuid: props.levelUuid,
        sessionId: sessionId,
        score: score.value,
      }),
    });

    const result = await response.json();

    if (result.success) {
      stopGame();
      emit("gameComplete", {
        success: true,
        message: `恭喜！您以 ${score.value} 分的成绩通关了！`,
        nextLevel: result.nextLevel,
      });
    }
  } catch (error) {
    console.error("Failed to auto complete level:", error);
  } finally {
    isSubmitting.value = false;
  }
};

// 提交通关
const completeLevel = async () => {
  if (!hasWon.value || isSubmitting.value) return;

  isSubmitting.value = true;

  try {
    const response = await fetch(`http://localhost:8080/api/game/complete`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        levelUuid: props.levelUuid,
        sessionId: sessionId,
        score: score.value,
      }),
    });

    const result = await response.json();

    if (result.success) {
      emit("gameComplete", {
        success: true,
        message: `恭喜！您以 ${score.value} 分的成绩通关了！`,
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

// 游戏更新循环
const update = () => {
  if (!gameRunning.value || gamePaused.value) return;

  dropTimer += 50;
  if (dropTimer >= dropInterval) {
    movePiece(0, 1);
    dropTimer = 0;
  }

  draw();
};

// 移动方块
const movePiece = (dx: number, dy: number) => {
  const newX = currentPiece.x + dx;
  const newY = currentPiece.y + dy;

  if (isValidPosition(currentPiece.shape, newX, newY)) {
    currentPiece.x = newX;
    currentPiece.y = newY;
    return true;
  } else if (dy > 0) {
    // 方块到底了，固定方块
    placePiece();
    clearLines();
    currentPiece = nextPieceData;
    nextPieceData = createPiece();

    // 检查游戏结束
    if (!isValidPosition(currentPiece.shape, currentPiece.x, currentPiece.y)) {
      endGame();
    }
  }
  return false;
};

// 旋转方块
const rotatePiece = () => {
  const currentType = currentPiece.type;
  const rotations = pieces[currentType as keyof typeof pieces];
  const nextRotation = (currentPiece.rotation + 1) % rotations.length;
  const newShape = rotations[nextRotation];

  if (isValidPosition(newShape, currentPiece.x, currentPiece.y)) {
    currentPiece.shape = newShape;
    currentPiece.rotation = nextRotation;
  }
};

// 检查位置是否有效
const isValidPosition = (shape: number[][], x: number, y: number) => {
  for (let row = 0; row < shape.length; row++) {
    for (let col = 0; col < shape[row].length; col++) {
      if (shape[row][col]) {
        const newX = x + col;
        const newY = y + row;

        if (
          newX < 0 ||
          newX >= BOARD_WIDTH ||
          newY >= BOARD_HEIGHT ||
          (newY >= 0 && board[newY][newX])
        ) {
          return false;
        }
      }
    }
  }
  return true;
};

// 放置方块
const placePiece = () => {
  const shape = currentPiece.shape;
  const type = currentPiece.type;

  for (let row = 0; row < shape.length; row++) {
    for (let col = 0; col < shape[row].length; col++) {
      if (shape[row][col]) {
        const x = currentPiece.x + col;
        const y = currentPiece.y + row;
        if (y >= 0) {
          board[y][x] = type;
        }
      }
    }
  }
};

// 清除满行
const clearLines = () => {
  let linesCleared = 0;

  for (let row = BOARD_HEIGHT - 1; row >= 0; row--) {
    if (board[row].every((cell) => cell !== 0)) {
      board.splice(row, 1);
      board.unshift(Array(BOARD_WIDTH).fill(0));
      linesCleared++;
      row++; // 重新检查同一行
    }
  }

  if (linesCleared > 0) {
    score.value += calculateScore(linesCleared);
  }
};

// 计算得分
const calculateScore = (linesCleared: number) => {
  const lineScores = [0, 40, 100, 300, 1200];
  return lineScores[linesCleared] || 0;
};

// 游戏结束
const endGame = () => {
  stopGame();
  gameOver.value = true;
};

// 绘制游戏
const draw = () => {
  drawBoard();
  drawCurrentPiece();
  drawNextPiece();
};

// 绘制游戏板
const drawBoard = () => {
  if (!ctx) return;

  // 清空画布
  ctx.fillStyle = "#1a202c";
  ctx.fillRect(0, 0, gameBoard.value!.width, gameBoard.value!.height);

  // 绘制已放置的方块
  for (let row = 0; row < BOARD_HEIGHT; row++) {
    for (let col = 0; col < BOARD_WIDTH; col++) {
      if (board[row][col]) {
        drawBlock(ctx, col, row, colors[board[row][col]]);
      }
    }
  }

  // 绘制网格线
  ctx.strokeStyle = "#2d3748";
  ctx.lineWidth = 1;
  for (let i = 0; i <= BOARD_WIDTH; i++) {
    ctx.beginPath();
    ctx.moveTo(i * BLOCK_SIZE, 0);
    ctx.lineTo(i * BLOCK_SIZE, gameBoard.value!.height);
    ctx.stroke();
  }
  for (let i = 0; i <= BOARD_HEIGHT; i++) {
    ctx.beginPath();
    ctx.moveTo(0, i * BLOCK_SIZE);
    ctx.lineTo(gameBoard.value!.width, i * BLOCK_SIZE);
    ctx.stroke();
  }
};

// 绘制当前方块
const drawCurrentPiece = () => {
  if (!currentPiece || !ctx) return;

  const shape = currentPiece.shape;
  const color = colors[currentPiece.type];

  for (let row = 0; row < shape.length; row++) {
    for (let col = 0; col < shape[row].length; col++) {
      if (shape[row][col]) {
        const x = currentPiece.x + col;
        const y = currentPiece.y + row;
        if (y >= 0) {
          drawBlock(ctx, x, y, color);
        }
      }
    }
  }
};

// 绘制下一个方块
const drawNextPiece = () => {
  if (!nextPieceData || !nextCtx) return;

  // 清空下一个方块的画布
  nextCtx.fillStyle = "#1a202c";
  nextCtx.fillRect(0, 0, nextPiece.value!.width, nextPiece.value!.height);

  const shape = nextPieceData.shape;
  const color = colors[nextPieceData.type];
  const blockSize = 30; // 增大下一个方块的显示尺寸

  // 计算居中位置
  const offsetX = (nextPiece.value!.width - shape[0].length * blockSize) / 2;
  const offsetY = (nextPiece.value!.height - shape.length * blockSize) / 2;

  for (let row = 0; row < shape.length; row++) {
    for (let col = 0; col < shape[row].length; col++) {
      if (shape[row][col]) {
        const x = offsetX + col * blockSize;
        const y = offsetY + row * blockSize;
        drawBlockAt(nextCtx, x, y, blockSize, color);
      }
    }
  }
};

// 绘制方块
const drawBlock = (
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  color: string
) => {
  const pixelX = x * BLOCK_SIZE;
  const pixelY = y * BLOCK_SIZE;
  drawBlockAt(ctx, pixelX, pixelY, BLOCK_SIZE, color);
};

// 在指定位置绘制方块
const drawBlockAt = (
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  size: number,
  color: string
) => {
  // 绘制方块主体
  ctx.fillStyle = color;
  ctx.fillRect(x, y, size, size);

  // 绘制高光效果
  ctx.fillStyle = "rgba(255, 255, 255, 0.3)";
  ctx.fillRect(x, y, size, 2);
  ctx.fillRect(x, y, 2, size);

  // 绘制阴影效果
  ctx.fillStyle = "rgba(0, 0, 0, 0.3)";
  ctx.fillRect(x, y + size - 2, size, 2);
  ctx.fillRect(x + size - 2, y, 2, size);

  // 绘制边框
  ctx.strokeStyle = "rgba(0, 0, 0, 0.5)";
  ctx.lineWidth = 1;
  ctx.strokeRect(x, y, size, size);
};

// 键盘事件处理
const handleKeyPress = (e: KeyboardEvent) => {
  if (!gameRunning.value || gamePaused.value) return;

  switch (e.code) {
    case "ArrowLeft":
      e.preventDefault();
      movePiece(-1, 0);
      break;
    case "ArrowRight":
      e.preventDefault();
      movePiece(1, 0);
      break;
    case "ArrowDown":
      e.preventDefault();
      movePiece(0, 1);
      break;
    case "Space":
      e.preventDefault();
      rotatePiece();
      break;
    case "KeyP":
      e.preventDefault();
      togglePause();
      break;
  }
};

// 组件挂载时初始化
onMounted(() => {
  if (gameBoard.value) {
    ctx = gameBoard.value.getContext("2d");
  }
  if (nextPiece.value) {
    nextCtx = nextPiece.value.getContext("2d");
  }

  board = createBoard();
  document.addEventListener("keydown", handleKeyPress);
});

// 组件卸载时清理
onUnmounted(() => {
  if (gameLoop) {
    clearInterval(gameLoop);
  }
  document.removeEventListener("keydown", handleKeyPress);
});
</script>
