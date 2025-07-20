<template>
  <div
    class="h-full flex items-center justify-center p-4 bg-gradient-to-br from-green-50 to-emerald-100"
  >
    <div class="max-w-4xl w-full bg-white rounded-xl shadow-xl p-6">
      <h1 class="text-3xl font-bold text-center mb-6 text-gray-800">
        Wordle 猜词游戏
      </h1>

      <div class="flex flex-col items-center">
        <!-- 游戏状态信息 -->
        <div class="mb-6 text-center">
          <div class="flex justify-center items-center gap-6 mb-4">
            <div class="bg-blue-100 px-4 py-2 rounded-lg">
              <span class="text-sm text-blue-700">剩余次数:</span>
              <span class="text-lg font-bold text-blue-900 ml-1">{{
                remainingGuesses
              }}</span>
            </div>
            <div class="bg-purple-100 px-4 py-2 rounded-lg">
              <span class="text-sm text-purple-700">当前回合:</span>
              <span class="text-lg font-bold text-purple-900 ml-1"
                >{{ currentGuessCount }}/6</span
              >
            </div>
          </div>
        </div>

        <!-- 游戏网格 5x6 -->
        <div class="mb-8">
          <div class="grid grid-rows-6 gap-2 p-4 bg-gray-50 rounded-lg">
            <div v-for="row in 6" :key="row" class="flex gap-2">
              <div
                v-for="col in 5"
                :key="`${row}-${col}`"
                :class="getCellClasses(row - 1, col - 1)"
                class="w-12 h-12 border-2 rounded-md flex items-center justify-center font-bold text-lg uppercase transition-all duration-300"
              >
                {{ getCellContent(row - 1, col - 1) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 当前输入框 -->
        <div class="mb-6 w-full max-w-md">
          <div class="flex gap-2 justify-center mb-4">
            <input
              v-model="currentGuess"
              @keyup.enter="submitGuess"
              @input="handleInput"
              :disabled="gameState !== 'playing'"
              placeholder="输入5个字母"
              maxlength="5"
              class="flex-1 px-4 py-3 border-2 border-gray-300 rounded-lg text-center text-lg font-semibold uppercase focus:border-blue-500 focus:outline-none disabled:bg-gray-100"
            />
            <button
              @click="submitGuess"
              :disabled="!canSubmitGuess"
              class="px-6 py-3 bg-green-500 hover:bg-green-600 disabled:bg-gray-400 text-white rounded-lg font-semibold transition-colors"
            >
              提交
            </button>
          </div>
        </div>

        <!-- 虚拟键盘 -->
        <div class="w-full max-w-2xl">
          <div class="space-y-2">
            <!-- 第一行 -->
            <div class="flex justify-center gap-1">
              <button
                v-for="letter in keyboardRows[0]"
                :key="letter"
                @click="addLetter(letter)"
                :class="getKeyboardKeyClasses(letter)"
                class="w-10 h-12 rounded font-semibold text-sm transition-all duration-200 hover:scale-105"
              >
                {{ letter }}
              </button>
            </div>
            <!-- 第二行 -->
            <div class="flex justify-center gap-1">
              <button
                v-for="letter in keyboardRows[1]"
                :key="letter"
                @click="addLetter(letter)"
                :class="getKeyboardKeyClasses(letter)"
                class="w-10 h-12 rounded font-semibold text-sm transition-all duration-200 hover:scale-105"
              >
                {{ letter }}
              </button>
            </div>
            <!-- 第三行 -->
            <div class="flex justify-center gap-1">
              <button
                @click="deleteLetter"
                class="w-16 h-12 bg-gray-300 hover:bg-gray-400 text-gray-700 rounded font-semibold text-xs transition-all duration-200 hover:scale-105"
              >
                删除
              </button>
              <button
                v-for="letter in keyboardRows[2]"
                :key="letter"
                @click="addLetter(letter)"
                :class="getKeyboardKeyClasses(letter)"
                class="w-10 h-12 rounded font-semibold text-sm transition-all duration-200 hover:scale-105"
              >
                {{ letter }}
              </button>
              <button
                @click="submitGuess"
                :disabled="!canSubmitGuess"
                class="w-16 h-12 bg-blue-500 hover:bg-blue-600 disabled:bg-gray-400 text-white rounded font-semibold text-xs transition-all duration-200 hover:scale-105"
              >
                回车
              </button>
            </div>
          </div>
        </div>

        <!-- 游戏状态提示 -->
        <div class="mt-6 text-center">
          <div
            v-if="gameState === 'won'"
            class="text-green-600 font-bold text-xl"
          >
            🎉 恭喜！你猜对了单词：{{ answer }}
          </div>
          <div
            v-else-if="gameState === 'lost'"
            class="text-red-600 font-bold text-xl"
          >
            😢 游戏结束！正确答案是：{{ answer }}
          </div>
          <div v-else-if="gameState === 'playing'" class="text-gray-600">
            猜一个5字母的英文单词
          </div>
        </div>

        <!-- 重新开始按钮 -->
        <div class="mt-6 space-y-3">
          <button
            v-if="gameState !== 'playing'"
            @click="restartGame"
            class="px-8 py-3 bg-purple-500 hover:bg-purple-600 text-white rounded-lg font-semibold transition-colors"
          >
            重新开始
          </button>
        </div>

        <!-- 调试面板 (仅开发环境) -->
        <div
          v-if="isDevelopment"
          class="mt-6 p-4 bg-yellow-50 rounded-lg border border-yellow-200 w-full max-w-md"
        >
          <h3 class="text-lg font-semibold text-yellow-800 mb-3">
            🔧 调试面板
          </h3>
          <div class="space-y-2">
            <div class="flex items-center justify-between text-sm">
              <span class="text-yellow-700">游戏状态:</span>
              <span class="font-mono text-yellow-900">{{ gameState }}</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-yellow-700">会话ID:</span>
              <span class="font-mono text-yellow-900 text-xs">{{
                sessionId
              }}</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-yellow-700">答案:</span>
              <span class="font-mono text-yellow-900">{{ answer }}</span>
            </div>
            <button
              @click="debugWin"
              class="w-full px-3 py-2 bg-yellow-600 hover:bg-yellow-700 text-white rounded text-sm"
            >
              调试胜利
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import {
  startWordle,
  guessWordle,
  startGame,
  completeGame,
} from "@/services/api";

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
const gameState = ref<"waiting" | "playing" | "won" | "lost">("waiting");
const sessionId = ref("");
const currentGuess = ref("");
const guessHistory = ref<string[]>([]);
const guessResults = ref<string[]>([]); // 存储每次猜测的结果（+ x -）
const currentGuessCount = ref(0);
const answer = ref("");
const isSubmitting = ref(false);

// 开发环境标识
const isDevelopment = ref(import.meta.env.DEV);

// 获取用户信息
const username = ref(localStorage.getItem("nazo_user") || "");

// 键盘布局
const keyboardRows = [
  ["Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"],
  ["A", "S", "D", "F", "G", "H", "J", "K", "L"],
  ["Z", "X", "C", "V", "B", "N", "M"],
];

// 字母状态跟踪（用于键盘颜色）
const letterStates = ref<
  Record<string, "correct" | "present" | "absent" | "unused">
>({});

// 计算属性
const remainingGuesses = computed(() => 6 - currentGuessCount.value);

const canSubmitGuess = computed(() => {
  return (
    gameState.value === "playing" &&
    currentGuess.value.length === 5 &&
    /^[A-Za-z]{5}$/.test(currentGuess.value) &&
    !isSubmitting.value
  );
});

// 初始化游戏
onMounted(async () => {
  await initializeGame();
});

// 初始化游戏
const initializeGame = async () => {
  try {
    // 首先初始化游戏会话（用于关卡系统）
    if (props.levelUuid && username.value) {
      const levelResponse = await startGame(props.levelUuid);
      if (!levelResponse.success) {
        console.error("Failed to start level session:", levelResponse.message);
      }
    }

    // 然后启动Wordle游戏
    const response = await startWordle(username.value);
    if (response.success && response.sessionId) {
      sessionId.value = response.sessionId;
      gameState.value = "playing";

      // 重置游戏状态
      currentGuess.value = "";
      guessHistory.value = [];
      guessResults.value = [];
      currentGuessCount.value = 0;
      answer.value = "";
      letterStates.value = {};

      console.log("Wordle game started with sessionId:", sessionId.value);
    } else {
      console.error("Failed to start Wordle game:", response.message);
      alert("启动游戏失败：" + response.message);
    }
  } catch (error) {
    console.error("Failed to initialize game:", error);
    alert("游戏初始化失败，请刷新页面重试");
  }
};

// 处理输入
const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement;
  // 只允许字母输入，并转换为大写
  target.value = target.value.replace(/[^A-Za-z]/g, "").toUpperCase();
  currentGuess.value = target.value;
};

// 添加字母（键盘点击）
const addLetter = (letter: string) => {
  if (gameState.value === "playing" && currentGuess.value.length < 5) {
    currentGuess.value += letter;
  }
};

// 删除字母
const deleteLetter = () => {
  if (currentGuess.value.length > 0) {
    currentGuess.value = currentGuess.value.slice(0, -1);
  }
};

// 提交猜测
const submitGuess = async () => {
  if (!canSubmitGuess.value) return;

  isSubmitting.value = true;

  try {
    const response = await guessWordle(sessionId.value, currentGuess.value);

    if (response.success && response.result) {
      // 保存猜测历史和结果
      guessHistory.value.push(currentGuess.value);
      guessResults.value.push(response.result);
      currentGuessCount.value =
        response.guessCount || currentGuessCount.value + 1;

      // 更新字母状态
      updateLetterStates(currentGuess.value, response.result);

      // 检查游戏状态
      if (response.result === "+++++") {
        // 猜对了
        gameState.value = "won";
        answer.value = response.answer || currentGuess.value;
        await handleGameComplete(true);
      } else if (currentGuessCount.value >= 6) {
        // 用完了次数
        gameState.value = "lost";
        answer.value = response.answer || "";
      }

      // 清空当前猜测
      currentGuess.value = "";
    } else {
      alert("猜测失败：" + (response.message || "未知错误"));
    }
  } catch (error) {
    console.error("Guess submission failed:", error);
    alert("提交猜测失败，请重试");
  } finally {
    isSubmitting.value = false;
  }
};

// 更新字母状态（用于键盘颜色）
const updateLetterStates = (guess: string, result: string) => {
  for (let i = 0; i < 5; i++) {
    const letter = guess[i];
    const resultChar = result[i];

    if (resultChar === "+") {
      letterStates.value[letter] = "correct";
    } else if (resultChar === "x" && letterStates.value[letter] !== "correct") {
      letterStates.value[letter] = "present";
    } else if (resultChar === "-" && !letterStates.value[letter]) {
      letterStates.value[letter] = "absent";
    }
  }
};

// 处理游戏完成
const handleGameComplete = async (won: boolean) => {
  if (!won || !props.levelUuid) return;

  try {
    const result = await completeGame(props.levelUuid, sessionId.value, {
      gameWon: won,
      guessCount: currentGuessCount.value,
      answer: answer.value,
    });

    if (result.success) {
      emit("gameComplete", {
        success: true,
        message: `恭喜！您用 ${currentGuessCount.value} 次猜测完成了Wordle游戏！`,
        nextLevel: result.nextLevel,
      });
    }
  } catch (error) {
    console.error("Failed to complete game:", error);
  }
};

// 重新开始游戏
const restartGame = async () => {
  gameState.value = "waiting";
  await initializeGame();
};

// 获取网格单元格样式
const getCellClasses = (row: number, col: number) => {
  if (row < guessHistory.value.length) {
    // 已提交的猜测
    const result = guessResults.value[row];
    const resultChar = result[col];

    if (resultChar === "+") {
      return "border-green-500 bg-green-500 text-white";
    } else if (resultChar === "x") {
      return "border-yellow-500 bg-yellow-500 text-white";
    } else if (resultChar === "-") {
      return "border-gray-500 bg-gray-500 text-white";
    }
  } else if (
    row === guessHistory.value.length &&
    currentGuess.value.length > col
  ) {
    // 当前正在输入的行
    return "border-blue-500 bg-blue-50 text-gray-800";
  }

  return "border-gray-300 bg-white text-gray-800";
};

// 获取网格单元格内容
const getCellContent = (row: number, col: number) => {
  if (row < guessHistory.value.length) {
    return guessHistory.value[row][col] || "";
  } else if (row === guessHistory.value.length) {
    return currentGuess.value[col] || "";
  }
  return "";
};

// 获取键盘按键样式
const getKeyboardKeyClasses = (letter: string) => {
  const state = letterStates.value[letter];

  switch (state) {
    case "correct":
      return "bg-green-500 text-white border-green-500";
    case "present":
      return "bg-yellow-500 text-white border-yellow-500";
    case "absent":
      return "bg-gray-500 text-white border-gray-500";
    default:
      return "bg-gray-200 hover:bg-gray-300 text-gray-800 border-gray-300";
  }
};

// 调试函数
const debugWin = () => {
  if (isDevelopment.value) {
    gameState.value = "won";
    answer.value = "DEBUG";
    handleGameComplete(true);
  }
};
</script>
