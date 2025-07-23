<template>
  <div class="h-full flex items-center justify-center p-4 bg-gradient-to-br from-green-50 to-emerald-100">
    <div class="max-w-4xl w-full bg-white rounded-xl shadow-xl p-6 m-5">

      <div class="flex flex-col items-center mt-2 mb-2">

        <!-- 游戏网格 5x6 -->
        <div class="mb-8">
          <div class="grid grid-rows-6 gap-2 p-4 bg-gray-50 rounded-md">
            <div v-for="row in 6" :key="row" class="flex gap-2">
              <div v-for="col in 5" :key="`${row}-${col}`" :class="getCellClasses(row - 1, col - 1)"
                class="w-12 h-12 border-2 rounded-md flex items-center justify-center font-bold text-lg uppercase transition-all duration-300">
                {{ getCellContent(row - 1, col - 1) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 当前输入框 -->
        <div class="mb-6 w-full max-w-md">
          <div class="flex gap-2 justify-center">
            <input v-model="currentGuess" @keyup.enter="submitGuess" @input="handleInput"
              :disabled="gameState !== 'playing'" placeholder="输入5个字母" maxlength="5"
              class="text-gray-400 flex-1 px-4 py-3 border-2 border-gray-300 rounded-md text-center text-lg font-semibold uppercase focus:border-blue-500 focus:outline-none disabled:bg-gray-100" />
            <button @click="submitGuess" :disabled="!canSubmitGuess"
              class="px-6 py-3 bg-green-500 hover:bg-green-600 disabled:bg-gray-400 text-white-400 rounded-md font-semibold transition-colors">
              提交
            </button>
          </div>
        </div>

        <!-- 虚拟键盘 -->
        <div class="w-full max-w-2xl">
          <div class="space-y-2">
            <!-- 第一行 -->
            <div class="flex justify-center gap-1 mt-2 mb-2">
              <button v-for="letter in keyboardRows[0]" :key="letter" @click="addLetter(letter)"
                :class="getKeyboardKeyClasses(letter)"
                class="w-10 h-12 rounded font-semibold text-sm transition-all duration-200 hover:scale-105">
                {{ letter }}
              </button>
            </div>
            <!-- 第二行 -->
            <div class="flex justify-center gap-1 mt-2 mb-2">
              <button v-for="letter in keyboardRows[1]" :key="letter" @click="addLetter(letter)"
                :class="getKeyboardKeyClasses(letter)"
                class="w-10 h-12 rounded font-semibold text-sm transition-all duration-200 hover:scale-105 mt-2 mb-2">
                {{ letter }}
              </button>
            </div>
            <!-- 第三行 -->
            <div class="flex justify-center gap-1 mt-2 mb-2">
              <button @click="deleteLetter"
                class="w-16 h-12 bg-gray-300 hover:bg-gray-400 text-gray-700 rounded font-semibold text-xs transition-all duration-200 hover:scale-105">
                删除
              </button>
              <button v-for="letter in keyboardRows[2]" :key="letter" @click="addLetter(letter)"
                :class="getKeyboardKeyClasses(letter)"
                class="w-10 h-12 rounded font-semibold text-sm transition-all duration-200 hover:scale-105">
                {{ letter }}
              </button>
              <button @click="submitGuess" :disabled="!canSubmitGuess"
                class="w-16 h-12 bg-blue-500 hover:bg-blue-600 disabled:bg-gray-400 text-white rounded font-semibold text-xs transition-all duration-200 hover:scale-105">
                回车
              </button>
            </div>
          </div>
        </div>

        <!-- 游戏状态提示 -->
        <div class="mt-6 text-center">
          <div v-if="gameState === 'won'" class="text-green-600 font-bold text-2xl">
            🎉 恭喜！你猜对了单词：{{ answer }}
          </div>
          <div v-else-if="gameState === 'lost'" class="text-red-600 font-bold text-2xl">
            😢 游戏结束！正确答案是：{{ answer }}
          </div>
          <!-- 提示信息 -->
          <div v-if="messageText" class="mt-2 text-orange-600 font-semibold text-2xl">
            {{ messageText }}
          </div>
        </div>

        <!-- 重新开始按钮 -->
        <div class="mt-6 space-y-3">
          <button v-if="gameState !== 'playing'" @click="restartGame"
            class="px-8 py-3 bg-purple-500 hover:bg-purple-600 text-white rounded-md font-semibold transition-colors">
            重新开始
          </button>
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
            class="bg-gradient-to-r from-green-500 to-emerald-600 hover:from-green-600 hover:to-emerald-700 text-white px-6 py-3 rounded-xl font-semibold transition-all duration-200 transform hover:scale-105">
            太棒了！
          </button>
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
  getEasterEgg,
} from "@/services/api";
import { WORDLE_EASTER_EGG_UUID } from "@/constants/levels";

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
const wordleSessionId = ref(""); // Wordle游戏的sessionId
const levelSessionId = ref(""); // 关卡系统的sessionId
const currentGuess = ref("");
const guessHistory = ref<string[]>([]);
const guessResults = ref<string[]>([]); // 存储每次猜测的结果（+ x -）
const currentGuessCount = ref(0);
const answer = ref("");
const isSubmitting = ref(false);
const messageText = ref(""); // 用于显示提示信息

// 彩蛋相关状态
const showEasterEgg = ref(false);
const easterEggMessage = ref("");

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
      if (levelResponse.success && levelResponse.sessionId) {
        levelSessionId.value = levelResponse.sessionId;
        console.log(
          "Level session started with sessionId:",
          levelSessionId.value
        );
      } else {
        console.error("Failed to start level session:", levelResponse.message);
      }
    }

    // 然后启动Wordle游戏
    const response = await startWordle(username.value);
    if (response.success && response.sessionId) {
      wordleSessionId.value = response.sessionId;
      gameState.value = "playing";

      // 重置游戏状态
      currentGuess.value = "";
      guessHistory.value = [];
      guessResults.value = [];
      currentGuessCount.value = 0;
      answer.value = "";
      letterStates.value = {};

      console.log("Wordle game started with sessionId:", wordleSessionId.value);
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

  // 检查彩蛋触发词
  if (currentGuess.value.toLowerCase() === "ginow") {
    await triggerEasterEgg();
    return;
  }

  isSubmitting.value = true;
  messageText.value = ""; // 清空之前的提示信息

  try {
    const response = await guessWordle(
      wordleSessionId.value,
      currentGuess.value
    );

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
      // 处理猜测失败的情况
      if (
        response.message === "猜测的单词必须为5个字母" ||
        response.message === "单词不在词库中" ||
        response.message?.includes("词库")
      ) {
        // 单词无效，显示提示但不消耗猜测次数
        messageText.value = response.message || "单词不在词库中";
        // 3秒后清除提示信息
        setTimeout(() => {
          messageText.value = "";
        }, 3000);
      } else {
        // 其他错误用弹窗显示
        alert("猜测失败：" + (response.message || "未知错误"));
      }
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

  isSubmitting.value = true;

  try {
    const result = await completeGame(props.levelUuid, levelSessionId.value, {
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
    } else {
      alert("通关验证失败，请重试：" + (result.message || "未知错误"));
    }
  } catch (error) {
    console.error("Failed to complete game:", error);
    alert("网络错误，请重试");
  } finally {
    isSubmitting.value = false;
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

// 触发彩蛋
const triggerEasterEgg = async () => {
  easterEggMessage.value = WORDLE_EASTER_EGG_UUID;
  console.log(WORDLE_EASTER_EGG_UUID);
  showEasterEgg.value = true;
  currentGuess.value = ""; // 清空输入
};
</script>
