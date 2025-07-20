<template>
  <div
    class="min-h-screen bg-gradient-to-br from-indigo-500 via-purple-600 to-purple-800 flex items-center justify-center p-4"
  >
    <div
      class="bg-white/95 rounded-2xl p-6 shadow-2xl backdrop-blur-lg max-w-6xl w-full"
    >
      <h1 class="text-4xl font-bold text-center text-gray-700 mb-6 shadow-sm">
        俄罗斯方块
      </h1>

      <div class="flex flex-col lg:flex-row gap-8 items-center justify-center">
        <!-- 游戏区域 -->
        <div class="relative flex justify-center">
          <canvas
            ref="gameBoard"
            width="300"
            height="600"
            class="border-4 border-gray-600 rounded-lg bg-gray-900 shadow-inner"
          ></canvas>

          <!-- 游戏结束弹窗 -->
          <div
            v-if="gameOver"
            class="absolute inset-0 flex items-center justify-center bg-white/95 rounded-lg backdrop-blur-sm"
          >
            <div class="text-center p-8 bg-white rounded-xl shadow-lg">
              <h2 class="text-2xl font-bold text-red-600 mb-4">游戏结束</h2>
              <p class="text-lg text-gray-600 mb-6">最终得分: {{ score }}</p>
              <button
                @click="restartGame"
                class="px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transform hover:-translate-y-1 transition-all duration-300 font-bold uppercase tracking-wide shadow-lg hover:shadow-blue-500/40"
              >
                重新开始
              </button>
            </div>
          </div>
        </div>

        <!-- 信息面板 -->
        <div class="min-w-[200px] space-y-4">
          <!-- 得分 -->
          <div class="bg-white/80 p-4 rounded-lg shadow-md">
            <h3
              class="text-sm font-bold text-gray-600 mb-2 uppercase tracking-wide"
            >
              得分
            </h3>
            <div class="text-2xl font-bold text-gray-800 text-center">
              {{ score }}
            </div>
          </div>

          <!-- 等级 -->
          <div class="bg-white/80 p-4 rounded-lg shadow-md">
            <h3
              class="text-sm font-bold text-gray-600 mb-2 uppercase tracking-wide"
            >
              等级
            </h3>
            <div class="text-2xl font-bold text-gray-800 text-center">
              {{ level }}
            </div>
          </div>

          <!-- 消除行数 -->
          <div class="bg-white/80 p-4 rounded-lg shadow-md">
            <h3
              class="text-sm font-bold text-gray-600 mb-2 uppercase tracking-wide"
            >
              消除行数
            </h3>
            <div class="text-2xl font-bold text-gray-800 text-center">
              {{ lines }}
            </div>
          </div>

          <!-- 下一个方块 -->
          <div class="bg-white/80 p-4 rounded-lg shadow-md">
            <h3
              class="text-sm font-bold text-gray-600 mb-2 uppercase tracking-wide text-center"
            >
              下一个
            </h3>
            <div class="flex justify-center">
              <canvas
                ref="nextPiece"
                width="120"
                height="120"
                class="border-2 border-gray-300 rounded bg-gray-900"
              ></canvas>
            </div>
          </div>

          <!-- 操作说明 -->
          <div class="bg-white/80 p-4 rounded-lg shadow-md">
            <h3
              class="text-sm font-bold text-gray-600 mb-2 uppercase tracking-wide"
            >
              操作说明
            </h3>
            <div class="space-y-1 text-sm text-gray-600">
              <p>← → 移动</p>
              <p>↓ 快速下降</p>
              <p>空格 旋转</p>
              <p>P 暂停</p>
            </div>
          </div>

          <!-- 控制按钮 -->
          <button
            v-if="!gameRunning"
            @click="startGame"
            class="w-full py-3 bg-green-500 text-white rounded-lg hover:bg-green-600 transform hover:-translate-y-1 transition-all duration-300 font-bold uppercase tracking-wide shadow-lg hover:shadow-green-500/40"
          >
            开始游戏
          </button>

          <button
            v-if="gameRunning"
            @click="togglePause"
            class="w-full py-3 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transform hover:-translate-y-1 transition-all duration-300 font-bold uppercase tracking-wide shadow-lg hover:shadow-orange-500/40"
          >
            {{ gamePaused ? "继续" : "暂停" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";

// 模板引用
const gameBoard = ref<HTMLCanvasElement | null>(null);
const nextPiece = ref<HTMLCanvasElement | null>(null);

// 游戏状态
const score = ref(0);
const level = ref(1);
const lines = ref(0);
const gameRunning = ref(false);
const gamePaused = ref(false);
const gameOver = ref(false);

// 游戏配置
const BOARD_WIDTH = 10;
const BOARD_HEIGHT = 20;
const BLOCK_SIZE = 30;

// 游戏变量
let ctx: CanvasRenderingContext2D | null = null;
let nextCtx: CanvasRenderingContext2D | null = null;
let board: number[][] = [];
let currentPiece: any = null;
let nextPieceData: any = null;
let gameLoop: number | null = null;
let dropTimer = 0;
let dropInterval = 1000;

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
const startGame = () => {
  gameRunning.value = true;
  gamePaused.value = false;
  gameOver.value = false;
  board = createBoard();
  score.value = 0;
  level.value = 1;
  lines.value = 0;
  dropInterval = 1000;

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
    lines.value += linesCleared;
    score.value += calculateScore(linesCleared);
    level.value = Math.floor(lines.value / 10) + 1;
    dropInterval = Math.max(100, 1000 - (level.value - 1) * 100);
  }
};

// 计算得分
const calculateScore = (linesCleared: number) => {
  const lineScores = [0, 40, 100, 300, 1200];
  return lineScores[linesCleared] * level.value;
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
  const blockSize = 20;

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
