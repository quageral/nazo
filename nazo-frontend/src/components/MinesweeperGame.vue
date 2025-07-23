<template>
  <div class="h-full flex items-center justify-center p-4 bg-gradient-to-br from-blue-50 to-indigo-100">
    <div class="max-w-6xl w-full bg-white rounded-xl shadow-xl p-6">
      <h1 class="text-3xl font-bold text-center mb-6 text-gray-800">
        扫雷游戏
      </h1>

      <div class="flex flex-col items-center">
        <!-- 游戏区域 -->
        <div class="flex-1 flex flex-col items-center">
          <div class="board-width bg-gray-300 p-4 rounded-md shadow-lg">
            <!-- 游戏状态栏 -->
            <div class="flex justify-between items-center mb-4 px-2">
              <!-- 剩余地雷计数器 -->
              <div class="w-20 h-12 bg-gray-800 rounded-md shadow-inner flex items-center justify-center">
                <span class="text-2xl font-mono text-red-500 px-2">{{
                  formattedMineCount
                }}</span>
              </div>

              <!-- 表情按钮 -->
              <div @click="restartGame" @dragover="handleDragOver" @dragenter="handleDragEnter"
                @dragleave="handleDragLeave" @drop="handleDrop"
                class="w-12 h-12 bg-gray-200 rounded-md shadow-md flex items-center justify-center cursor-pointer transition-all duration-150 hover:scale-105 active:scale-95"
                :class="{ 'bg-yellow-200 scale-110': isDropZoneActive }">
                <span class="text-2xl">{{ faceEmoji }}</span>
              </div>

              <!-- 计时器 -->
              <div class="w-20 h-12 bg-gray-800 rounded-md shadow-inner flex items-center justify-center">
                <span class="text-2xl font-mono text-red-500 px-2">{{
                  formattedTime
                }}</span>
              </div>
            </div>

            <!-- 游戏面板 -->
            <div class="grid gap-px bg-gray-400 p-2 rounded overflow-auto max-h-120" :style="gridStyle">
              <div v-for="row in ROWS" :key="row" class="contents">
                <div v-for="col in COLS" :key="`${row - 1}-${col - 1}`" :class="getCellClasses(row - 1, col - 1)"
                  @click="handleLeftClick(row - 1, col - 1)" @contextmenu.prevent="handleRightClick(row - 1, col - 1)"
                  @mousedown="handleMouseDown(row - 1, col - 1, $event)" @mouseup="handleMouseUp(row - 1, col - 1)"
                  class="relative">
                  {{ getCellContent(row - 1, col - 1) }}

                  <!-- 可拖拽的地雷图标 - 只在被点击的地雷位置且游戏失败时显示 -->
                  <div
                    v-if="showDraggableMine && draggableMinePosition.row === row - 1 && draggableMinePosition.col === col - 1"
                    class="absolute top-0 left-0 w-full h-full flex items-center justify-center cursor-grab hover:scale-110 transition-transform duration-200 z-10"
                    :class="{ 'cursor-grabbing opacity-50': isDragging }" draggable="true" @dragstart="handleDragStart"
                    @dragend="handleDragEnd">
                    💣
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 操作说明 -->
          <div class="mt-4 text-sm text-gray-600 text-center max-w-2xl">
            <p>左键: 打开格子 | 右键: 标记地雷</p>
            <p class="mt-1">在数字上同时按左右键: 快速打开周围格子</p>
            <p class="mt-1">快捷键: F2或空格 - 重新开始游戏</p>
          </div>
        </div>

        <!-- 游戏信息 -->
        <div class="w-full max-w-4xl bg-gray-50 rounded-md p-5 shadow-md mt-4">
          <div class="grid grid-cols-2 md:grid-cols-5 gap-4 text-center">
            <div class="flex flex-col">
              <span class="text-gray-600 text-sm">难度</span>
              <span class="text-gray-600 font-medium">专家 (30×16, 99雷)</span>
            </div>

            <div class="flex flex-col">
              <span class="text-gray-600 text-sm">游戏状态</span>
              <span :class="getStatusClass()">{{ gameStatus }}</span>
            </div>

            <div class="flex flex-col">
              <span class="text-gray-600 text-sm">本局用时</span>
              <span class="text-gray-600 font-medium">{{ formattedCurrentTime }}</span>
            </div>

            <div class="flex flex-col">
              <span class="text-gray-600 text-sm">已标记地雷</span>
              <span class="text-gray-600 font-medium">{{ flagCount }}/99</span>
            </div>

            <div class="flex flex-col">
              <span class="text-gray-600 text-sm">已打开格子</span>
              <span class="text-gray-600 font-medium">{{ openedCount }}/{{ totalSafeCells }}</span>
            </div>
          </div>

          <div class="flex justify-center mt-4">
            <button @click="restartGame"
              class="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-md transition-colors duration-200 flex items-center justify-center gap-2">
              <span>🔄</span>
              <span>重新开始</span>
            </button>
          </div>

          <!-- 调试面板 (仅开发环境) -->
          <!-- <div
            v-if="isDevelopment"
            class="mt-6 p-4 bg-yellow-50 rounded-md border border-yellow-200"
          >
            <h3 class="text-lg font-semibold text-yellow-800 mb-3">
              🔧 调试面板
            </h3>
            <div class="space-y-3">
              <div class="flex items-center justify-between text-sm">
                <span class="text-yellow-700">游戏状态:</span>
                <span class="font-mono text-yellow-900">{{ gameState }}</span>
              </div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-yellow-700">是否胜利:</span>
                <span class="font-mono text-yellow-900">{{ gameWon }}</span>
              </div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-yellow-700">会话ID:</span>
                <span class="font-mono text-yellow-900 text-xs">{{
                  sessionId
                }}</span>
              </div>
              <button
                @click="debugWin"
                class="w-full px-3 py-2 bg-yellow-600 hover:bg-yellow-700 text-white rounded text-sm"
              >
                调试胜利
              </button>
              <button
                @click="testGameComplete"
                class="w-full px-3 py-2 bg-green-600 hover:bg-green-700 text-white rounded text-sm"
              >
                测试通关检测
              </button>
            </div>
          </div> -->
        </div>
      </div>
    </div>

    <!-- 彩蛋弹窗 -->
    <div v-if="showEasterEgg" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
      @click.self="showEasterEgg = false">
      <div class="bg-white rounded-2xl p-6 max-w-md w-full shadow-2xl mx-4">
        <div class="text-center">
          <div class="text-6xl mb-4">🥚</div>
          <h3 class="text-2xl font-bold text-gray-800 mb-4">恭喜发现彩蛋！请保存彩蛋码</h3>
          <p class="text-gray-600 leading-relaxed mb-6">
            {{ easterEggMessage }}
          </p>
          <button @click="showEasterEgg = false"
            class="bg-gradient-to-r from-red-500 to-orange-600 hover:from-red-600 hover:to-orange-700 text-white px-6 py-3 rounded-xl font-semibold transition-all duration-200 transform hover:scale-105">
            太棒了！
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, reactive } from "vue";
import { startGame as startGameAPI, completeGame, getEasterEgg } from "@/services/api";
import { MINESWEEPER_EASTER_EGG_UUID } from "@/constants/levels";

interface Props {
  levelUuid: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  gameComplete: [
    data: { success: boolean; message: string; nextLevel?: string }
  ];
}>();

// 游戏常量
const ROWS = 16;
const COLS = 30;
const MINES = 99;

// 游戏状态
const gameBoard = ref<number[][]>([]);
const mines = ref<Set<string>>(new Set());
const flags = ref<Set<string>>(new Set());
const opened = ref<Set<string>>(new Set());
const gameStarted = ref(false);
const gameOver = ref(false);
const gameWon = ref(false);
const startTime = ref(0);
const currentTime = ref(0);
const timerInterval = ref<number | null>(null);
const sessionId = ref("");
const isSubmitting = ref(false);

// 彩蛋相关状态
const showEasterEgg = ref(false);
const easterEggMessage = ref("");
const showDraggableMine = ref(false);
const draggableMinePosition = ref({ row: -1, col: -1 });
const isDragging = ref(false);
const isDropZoneActive = ref(false);

// 开发环境标识
const isDevelopment = ref(import.meta.env.DEV);

// 拖拽事件处理
const handleDragStart = (event: DragEvent) => {
  isDragging.value = true;
  event.dataTransfer?.setData("text/plain", "mine-icon");
};

const handleDragEnd = () => {
  isDragging.value = false;
};

const handleDragOver = (event: DragEvent) => {
  event.preventDefault();
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
  if (data === "mine-icon") {
    easterEggMessage.value = MINESWEEPER_EASTER_EGG_UUID;
    console.log(MINESWEEPER_EASTER_EGG_UUID);
    showEasterEgg.value = true;
    showDraggableMine.value = false; // 隐藏可拖拽的地雷
  }
};

// 双键操作状态
const mouseDownButtons = ref(new Set<number>());
const isDoubleButtonOperation = ref(false);

// 计算属性
const totalSafeCells = computed(() => ROWS * COLS - MINES);
const openedCount = computed(() => opened.value.size);
const flagCount = computed(() => flags.value.size);
const remainingMines = computed(() => MINES - flagCount.value);

const formattedMineCount = computed(() => {
  return remainingMines.value.toString().padStart(3, "0");
});

const formattedTime = computed(() => {
  const displayTime = Math.min(999, Math.floor(currentTime.value / 1000));
  return displayTime.toString().padStart(3, "0");
});

const formattedCurrentTime = computed(() => {
  const seconds = Math.floor(currentTime.value / 1000);
  return `${seconds}秒`;
});

const faceEmoji = computed(() => {
  if (gameWon.value) return "😎";
  if (gameOver.value) return "😵";
  return "🙂";
});

const gameStatus = computed(() => {
  if (gameWon.value) return "游戏胜利！";
  if (gameOver.value) return "游戏失败！";
  if (gameStarted.value) return "游戏进行中";
  return "准备开始";
});

const gameState = computed(() => {
  if (gameWon.value) return "WON";
  if (gameOver.value) return "LOST";
  if (gameStarted.value) return "PLAYING";
  return "READY";
});

const gridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${COLS}, 1fr)`,
}));

// 初始化游戏会话
const initializeGameSession = async () => {
  try {
    const result = await startGameAPI(props.levelUuid);
    if (result.success && result.sessionId) {
      sessionId.value = result.sessionId;
      console.log("Game session initialized with sessionId:", sessionId.value);
    } else {
      console.error("Failed to initialize game session:", result.message);
      throw new Error(result.message);
    }
  } catch (error) {
    console.error("Failed to initialize game session:", error);
  }
};

// 初始化游戏
const initGame = () => {
  // 重置游戏状态
  gameBoard.value = Array(ROWS)
    .fill(null)
    .map(() => Array(COLS).fill(0));
  mines.value.clear();
  flags.value.clear();
  opened.value.clear();
  gameStarted.value = false;
  gameOver.value = false;
  gameWon.value = false;
  currentTime.value = 0;

  // 停止计时器
  if (timerInterval.value) {
    clearInterval(timerInterval.value);
    timerInterval.value = null;
  }
};

// 开始游戏（第一次点击后）
const startGame = (firstRow: number, firstCol: number) => {
  // 生成地雷位置，确保第一次点击的位置不是地雷
  generateMines(firstRow, firstCol);

  // 计算每个格子周围的地雷数
  calculateNumbers();

  // 打开第一次点击的格子
  openCell(firstRow, firstCol);

  // 开始计时
  gameStarted.value = true;
  startTime.value = Date.now();
  timerInterval.value = setInterval(updateTimer, 100);
};

// 生成地雷位置
const generateMines = (firstRow: number, firstCol: number) => {
  // 创建一个安全区域（第一次点击的格子及其周围8个格子）
  const safeZone = new Set<string>();
  for (
    let r = Math.max(0, firstRow - 1);
    r <= Math.min(ROWS - 1, firstRow + 1);
    r++
  ) {
    for (
      let c = Math.max(0, firstCol - 1);
      c <= Math.min(COLS - 1, firstCol + 1);
      c++
    ) {
      safeZone.add(`${r}-${c}`);
    }
  }

  // 随机放置地雷
  let minesPlaced = 0;
  while (minesPlaced < MINES) {
    const row = Math.floor(Math.random() * ROWS);
    const col = Math.floor(Math.random() * COLS);
    const position = `${row}-${col}`;

    if (!mines.value.has(position) && !safeZone.has(position)) {
      mines.value.add(position);
      minesPlaced++;
    }
  }
};

// 计算每个格子周围的地雷数
const calculateNumbers = () => {
  for (let row = 0; row < ROWS; row++) {
    for (let col = 0; col < COLS; col++) {
      if (mines.value.has(`${row}-${col}`)) {
        gameBoard.value[row][col] = -1; // -1 表示地雷
        continue;
      }

      // 计算周围的地雷数
      let count = 0;
      for (
        let r = Math.max(0, row - 1);
        r <= Math.min(ROWS - 1, row + 1);
        r++
      ) {
        for (
          let c = Math.max(0, col - 1);
          c <= Math.min(COLS - 1, col + 1);
          c++
        ) {
          if (r === row && c === col) continue;
          if (mines.value.has(`${r}-${c}`)) {
            count++;
          }
        }
      }

      gameBoard.value[row][col] = count;
    }
  }
};

// 处理鼠标按下
const handleMouseDown = (row: number, col: number, event: MouseEvent) => {
  if (gameOver.value || gameWon.value) return;

  mouseDownButtons.value.add(event.button);

  // 检查是否同时按下左右键 (0: 左键, 2: 右键)
  if (mouseDownButtons.value.has(0) && mouseDownButtons.value.has(2)) {
    isDoubleButtonOperation.value = true;
    event.preventDefault();
    handleDoubleClick(row, col);
  }
};

// 处理鼠标释放
const handleMouseUp = (row: number, col: number) => {
  mouseDownButtons.value.clear();
  isDoubleButtonOperation.value = false;
};

// 处理双键快速打开
const handleDoubleClick = (row: number, col: number) => {
  const position = `${row}-${col}`;

  // 只有已打开的格子且有数字才能进行双键操作
  if (!opened.value.has(position) || gameBoard.value[row][col] <= 0) {
    return;
  }

  const targetNumber = gameBoard.value[row][col];

  // 计算周围的旗帜数量
  let flagCountAround = 0;
  for (let r = Math.max(0, row - 1); r <= Math.min(ROWS - 1, row + 1); r++) {
    for (let c = Math.max(0, col - 1); c <= Math.min(COLS - 1, col + 1); c++) {
      if (r === row && c === col) continue;
      if (flags.value.has(`${r}-${c}`)) {
        flagCountAround++;
      }
    }
  }

  // 只有当周围旗帜数量等于该数字时才能快速打开
  if (flagCountAround === targetNumber) {
    for (let r = Math.max(0, row - 1); r <= Math.min(ROWS - 1, row + 1); r++) {
      for (
        let c = Math.max(0, col - 1);
        c <= Math.min(COLS - 1, col + 1);
        c++
      ) {
        if (r === row && c === col) continue;
        if (!flags.value.has(`${r}-${c}`) && !opened.value.has(`${r}-${c}`)) {
          openCell(r, c);
        }
      }
    }
  }
};

// 处理左键点击
const handleLeftClick = (row: number, col: number) => {
  if (
    gameOver.value ||
    gameWon.value ||
    flags.value.has(`${row}-${col}`) ||
    isDoubleButtonOperation.value
  )
    return;

  if (!gameStarted.value) {
    startGame(row, col);
  } else {
    openCell(row, col);
  }
};

// 处理右键点击
const handleRightClick = (row: number, col: number) => {
  if (
    gameOver.value ||
    gameWon.value ||
    opened.value.has(`${row}-${col}`) ||
    isDoubleButtonOperation.value
  )
    return;

  toggleFlag(row, col);
};

// 切换标记
const toggleFlag = (row: number, col: number) => {
  const position = `${row}-${col}`;

  if (opened.value.has(position)) return;

  if (flags.value.has(position)) {
    flags.value.delete(position);
  } else if (flags.value.size < MINES) {
    flags.value.add(position);
  }

  checkWin();
};

// 打开格子
const openCell = (row: number, col: number) => {
  const position = `${row}-${col}`;

  if (
    opened.value.has(position) ||
    flags.value.has(position) ||
    gameOver.value ||
    gameWon.value
  )
    return;

  opened.value.add(position);
  const value = gameBoard.value[row][col];

  if (value === -1) {
    // 踩到地雷，显示可拖拽的地雷图标
    showDraggableMine.value = true;
    draggableMinePosition.value = { row, col };
    // 游戏结束
    endGame(false);
    return;
  }

  if (value === 0) {
    // 自动打开周围的格子
    for (let r = Math.max(0, row - 1); r <= Math.min(ROWS - 1, row + 1); r++) {
      for (
        let c = Math.max(0, col - 1);
        c <= Math.min(COLS - 1, col + 1);
        c++
      ) {
        if (r === row && c === col) continue;
        openCell(r, c);
      }
    }
  }

  checkWin();
};

// 检查游戏胜利条件
const checkWin = () => {
  if (opened.value.size === totalSafeCells.value) {
    endGame(true);
  }
};

// 结束游戏
const endGame = (isWin: boolean) => {
  gameOver.value = !isWin;
  gameWon.value = isWin;

  // 停止计时器
  if (timerInterval.value) {
    clearInterval(timerInterval.value);
    timerInterval.value = null;
  }

  if (isWin) {
    // 自动标记所有地雷
    mines.value.forEach((position) => {
      if (!flags.value.has(position)) {
        flags.value.add(position);
      }
    });

    // 自动提交通关
    autoCompleteLevel();
  }
};

// 自动提交通关
const autoCompleteLevel = async () => {
  if (isSubmitting.value) return;

  isSubmitting.value = true;

  try {
    const result = await completeGame(props.levelUuid, sessionId.value, {
      gameWon: gameWon.value,
    });

    if (result.success) {
      emit("gameComplete", {
        success: true,
        message: "恭喜！您成功完成了扫雷游戏！",
        nextLevel: result.nextLevel,
      });
    }
  } catch (error) {
    console.error("Failed to auto complete level:", error);
  } finally {
    isSubmitting.value = false;
  }
};

// 重新开始游戏
const restartGame = async () => {
  initGame();
  // 隐藏彩蛋相关元素
  showDraggableMine.value = false;
  showEasterEgg.value = false;
  await initializeGameSession();
};

// 更新计时器
const updateTimer = () => {
  currentTime.value = Date.now() - startTime.value;
};

// 获取格子类名
const getCellClasses = (row: number, col: number) => {
  const position = `${row}-${col}`;
  const classes = [
    "w-6 h-6 border border-gray-400 flex items-center justify-center text-xs font-bold cursor-pointer select-none",
  ];

  if (opened.value.has(position)) {
    classes.push("bg-gray-100");
    if (gameBoard.value[row][col] === -1) {
      classes.push("bg-red-300"); // 踩到的地雷
    } else {
      // 为数字添加不同的颜色
      const value = gameBoard.value[row][col];
      if (value === 1) classes.push("text-blue-600");
      else if (value === 2) classes.push("text-green-600");
      else if (value === 3) classes.push("text-red-600");
      else if (value === 4) classes.push("text-purple-600");
      else if (value === 5) classes.push("text-yellow-600");
      else if (value === 6) classes.push("text-pink-600");
      else if (value === 7) classes.push("text-black");
      else if (value === 8) classes.push("text-gray-800");
      else classes.push("text-gray-800"); // 默认深色
    }
  } else {
    classes.push("bg-gray-300 hover:bg-gray-200 text-gray-800");
    if (flags.value.has(position)) {
      classes.push("bg-orange-200");
    }
  }

  return classes;
};

// 获取格子内容
const getCellContent = (row: number, col: number) => {
  const position = `${row}-${col}`;

  if (flags.value.has(position)) {
    return "🚩";
  }

  if (!opened.value.has(position)) {
    return "";
  }

  const value = gameBoard.value[row][col];
  if (value === -1) {
    return "💣";
  } else if (value > 0) {
    return value.toString();
  }

  return "";
};

// 获取状态类名
const getStatusClass = () => {
  if (gameWon.value) return "font-medium text-green-600";
  if (gameOver.value) return "font-medium text-red-500";
  if (gameStarted.value) return "font-medium text-blue-600";
  return "font-medium text-gray-600";
};

// 调试函数
const debugWin = () => {
  if (isDevelopment.value) {
    // 直接设置为胜利状态
    opened.value.clear();
    for (let row = 0; row < ROWS; row++) {
      for (let col = 0; col < COLS; col++) {
        if (!mines.value.has(`${row}-${col}`)) {
          opened.value.add(`${row}-${col}`);
        }
      }
    }
    endGame(true);
  }
};

const testGameComplete = () => {
  if (isDevelopment.value) {
    console.log(`游戏状态: ${gameState.value}`);
    console.log(`是否胜利: ${gameWon.value}`);
    console.log(`会话ID: ${sessionId.value}`);
    if (gameWon.value) {
      console.log("满足通关条件！");
    } else {
      console.log("未满足通关条件");
    }
  }
};

// 键盘快捷键
const handleKeydown = (e: KeyboardEvent) => {
  if (e.code === "F2" || e.code === "Space") {
    e.preventDefault();
    restartGame();
  }
};

// 生命周期
onMounted(async () => {
  initGame();
  await initializeGameSession();
  document.addEventListener("keydown", handleKeydown);
});

onUnmounted(() => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value);
  }
  document.removeEventListener("keydown", handleKeydown);
});
</script>
