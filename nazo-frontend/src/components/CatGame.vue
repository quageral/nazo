<template>
  <div
    class="h-full flex items-center justify-center p-8 bg-gradient-to-br from-orange-50 to-pink-100"
  >
    <div class="max-w-4xl w-full bg-white rounded-xl shadow-xl p-12 m-10">
      <div class="flex flex-col items-center py-12">
        <!-- Game Area -->
        <div class="w-full max-w-3xl">
          <!-- Question Display Area -->
          <div v-if="!gameCompleted" class="mb-16">
            <div class="text-center mb-12">
              <h2 class="text-3xl font-bold text-gray-800 mb-8">🐱 Cat 🐱</h2>
              <p class="text-lg text-gray-600 mb-10">
                题目 {{ currentQuestionIndex + 1 }} / {{ questions.length }}
              </p>
              <div class="w-full bg-gray-200 rounded-full h-2 mt-8">
                <div
                  class="bg-orange-500 h-2 rounded-full transition-all duration-300"
                  :style="{
                    width:
                      ((currentQuestionIndex + 1) / questions.length) * 100 +
                      '%',
                  }"
                ></div>
              </div>
            </div>

            <!-- Current Question -->
            <div
              v-if="currentQuestion"
              class="questionWrapper bg-gray-50 p-12 rounded-lg mt-12"
            >
              <!-- Question with Image Area -->
              <div
                class="question text-xl font-semibold text-gray-800 mb-12 text-center leading-relaxed"
              >
                <p class="mb-6">{{ currentQuestion.question }}</p>
                <!-- Image Display Area -->
                <div class="flex justify-center mb-8">
                  <img
                    :src="currentQuestion.imageUrl"
                    :alt="'猫咪图片 ' + (currentQuestionIndex + 1)"
                    class="max-w-md max-h-80 object-contain rounded-lg shadow-lg"
                    loading="lazy"
                  />
                </div>
              </div>

              <!-- Answer Options -->
              <div class="space-y-6">
                <button
                  v-for="(option, index) in currentQuestion.options"
                  :key="index"
                  @click="selectAnswer(index)"
                  :class="getOptionClasses(index)"
                  class="answer w-full px-8 py-6 rounded-md font-semibold text-lg transition-all duration-200 hover:scale-105 flex justify-between items-center"
                  :disabled="answered"
                >
                  <span>{{ getOptionLabel(index) }}. {{ option.text }}</span>
                  <span
                    v-if="answered"
                    class="text-2xl font-bold"
                    :class="{
                      'text-green-600': index === currentQuestion.correctIndex,
                      'text-red-600': index !== currentQuestion.correctIndex,
                    }"
                  >
                    {{ index === currentQuestion.correctIndex ? "✔" : "❌" }}
                  </span>
                </button>
              </div>
            </div>
          </div>

          <!-- Game Completed - Show Results -->
          <div v-if="gameCompleted" class="text-center">
            <div class="mb-16">
              <h2 class="text-3xl font-bold text-gray-800 mb-10">游戏完成！</h2>
              <div class="text-6xl font-bold mb-10" :class="scoreColor">
                {{ correctAnswers }} / {{ questions.length }}
              </div>
              <p class="text-lg text-gray-600 mb-12">
                您答对了 {{ correctAnswers }} / {{ questions.length }} 题
              </p>
            </div>

            <!-- Result Actions -->
            <div class="space-y-8">
              <div
                v-if="correctAnswers >= 10"
                class="text-green-600 font-bold text-2xl mb-10"
              >
                🎉 恭喜通关！答对了{{ correctAnswers }}题！
              </div>
              <div v-else class="text-orange-600 font-bold text-2xl mb-10">
                😅 需要答对至少10题才能通关（当前答对{{ correctAnswers }}题）
              </div>

              <button
                v-if="correctAnswers < 10"
                @click="restartGame"
                class="px-10 py-6 bg-purple-500 hover:bg-purple-600 text-white rounded-md font-semibold transition-colors text-2xl mt-8"
              >
                重新挑战
              </button>
            </div>
          </div>
        </div>
      </div>
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
        <div class="text-6xl mb-4">🎉</div>
        <h3 class="text-2xl font-bold text-gray-800 mb-4">恭喜发现彩蛋！</h3>
        <p class="text-gray-600 leading-relaxed mb-6">
          {{ easterEggMessage }}
        </p>
        <button
          @click="showEasterEgg = false"
          class="bg-gradient-to-r from-orange-500 to-pink-600 hover:from-orange-600 hover:to-pink-700 text-white px-6 py-3 rounded-xl font-semibold transition-all duration-200 transform hover:scale-105"
        >
          太棒了！
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { onBeforeRouteLeave } from "vue-router";
import { startGame, completeGame } from "@/services/api";
import { CAT_EASTER_EGG_UUID } from "@/constants/levels";

interface Props {
  levelUuid: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  gameComplete: [
    data: { success: boolean; message: string; nextLevel?: string }
  ];
}>();

// Game state
const currentQuestionIndex = ref(0);
const answers = ref<number[]>([]);
const answered = ref(false);
const gameCompleted = ref(false);
const sessionId = ref("");
const isSubmitting = ref(false);

// 彩蛋相关状态
const showEasterEgg = ref(false);
const easterEggMessage = ref("");

// Get username
const username = ref(localStorage.getItem("nazo_user") || "");

// Cat breed quiz questions parsed from HTML
const questions = ref([
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/1.png",
    options: [
      { text: "英短蓝猫", value: 0 },
      { text: "英国短毛猫", value: 0 },
      { text: "美国短毛猫", value: 6 },
    ],
    correctIndex: 2,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/2.png",
    options: [
      { text: "巴厘猫", value: 0 },
      { text: "暹罗猫", value: 6 },
      { text: "布偶猫", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/3.png",
    options: [
      { text: "英国短毛猫", value: 6 },
      { text: "异国短毛猫", value: 0 },
      { text: "美国短毛猫", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/4.png",
    options: [
      { text: "中华田园猫", value: 6 },
      { text: "橘猫", value: 6 },
      { text: "暹罗猫", value: 0 },
    ],
    correctIndex: 0, // Both 中华田园猫 and 橘猫 have value 6, choose first one
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/5.png",
    options: [
      { text: "波斯猫", value: 0 },
      { text: "虎斑猫", value: 0 },
      { text: "异国短毛猫", value: 6 },
    ],
    correctIndex: 2,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/6.png",
    options: [
      { text: "狸花猫", value: 6 },
      { text: "虎斑猫", value: 0 },
      { text: "布偶猫", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/7.png",
    options: [
      { text: "加拿大无毛猫（斯芬克斯猫）", value: 6 },
      { text: "德文卷毛猫", value: 0 },
      { text: "伯曼猫", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/8.png",
    options: [
      { text: "波斯猫", value: 0 },
      { text: "安哥拉猫", value: 0 },
      { text: "布偶猫", value: 6 },
    ],
    correctIndex: 2,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/9.png",
    options: [
      { text: "曼基康矮脚猫", value: 0 },
      { text: "苏格兰折耳猫", value: 6 },
      { text: "虎斑猫", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/10.png",
    options: [
      { text: "临清狮子猫", value: 6 },
      { text: "埃及猫", value: 0 },
      { text: "布偶猫", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/11.png",
    options: [
      { text: "狸花猫", value: 0 },
      { text: "土耳其梵猫", value: 0 },
      { text: "缅因猫", value: 6 },
    ],
    correctIndex: 2,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/12.png",
    options: [
      { text: "狸猫", value: 0 },
      { text: "豹猫", value: 6 },
      { text: "虎斑猫", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/13.png",
    options: [
      { text: "缅因猫", value: 0 },
      { text: "薮猫", value: 7 },
      { text: "豹猫", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/14.png",
    options: [
      { text: "孟买猫", value: 7 },
      { text: "埃及猫", value: 0 },
      { text: "缅因猫", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/15.png",
    options: [
      { text: "暹罗猫", value: 0 },
      { text: "巴厘猫", value: 7 },
      { text: "缅甸猫", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "请问下图中的猫咪属于哪个品种？",
    imageUrl: "https://areal.me/guess-cat-breeds-quiz/files/16.png",
    options: [
      { text: "马恩岛猫", value: 0 },
      { text: "沙特尔猫", value: 0 },
      { text: "阿比西尼亚猫", value: 7 },
    ],
    correctIndex: 2,
  },
]);

// Computed properties
const currentQuestion = computed(() => {
  return questions.value[currentQuestionIndex.value] || null;
});

const correctAnswers = computed(() => {
  return answers.value.reduce((count, answerIndex, questionIndex) => {
    return answerIndex === questions.value[questionIndex].correctIndex
      ? count + 1
      : count;
  }, 0);
});

const scoreColor = computed(() => {
  if (correctAnswers.value >= 10) return "text-green-600";
  if (correctAnswers.value >= 8) return "text-yellow-600";
  return "text-red-600";
});

// Generate option labels (A, B, C, D)
const getOptionLabel = (index: number) => {
  return String.fromCharCode(65 + index); // 65 is ASCII code for 'A'
};

// Handle page leave warning
const handleBeforeUnload = (event: BeforeUnloadEvent) => {
  // Only show warning if user is in the middle of the game (not completed and has answered at least one question)
  if (!gameCompleted.value && answers.value.length > 0) {
    event.preventDefault();
    event.returnValue = ""; // Required for Chrome
    return "您正在答题中，确定要离开页面吗？已答题目的进度将丢失。";
  }
};

// Initialize game
onMounted(async () => {
  await initializeGame();

  // Add beforeunload listener to warn user when leaving during game
  window.addEventListener("beforeunload", handleBeforeUnload);
});

// Cleanup on unmount
onUnmounted(() => {
  window.removeEventListener("beforeunload", handleBeforeUnload);
});

// Vue Router navigation guard
onBeforeRouteLeave((to, from, next) => {
  // Only show warning if user is in the middle of the game (not completed and has answered at least one question)
  if (!gameCompleted.value && answers.value.length > 0) {
    const answer = window.confirm(
      "您正在答题中，确定要离开页面吗？已答题目的进度将丢失。"
    );
    if (answer) {
      next();
    } else {
      next(false);
    }
  } else {
    next();
  }
});

const initializeGame = async () => {
  try {
    if (props.levelUuid && username.value) {
      const response = await startGame(props.levelUuid);
      if (response.success && response.sessionId) {
        sessionId.value = response.sessionId;
        console.log("Game session started with sessionId:", sessionId.value);
      } else {
        console.error("Failed to start game session:", response.message);
      }
    }
  } catch (error) {
    console.error("Failed to initialize game:", error);
    alert("游戏初始化失败，请刷新页面重试");
  }
};

// Game logic
const selectAnswer = (optionIndex: number) => {
  if (answered.value) return;

  answers.value[currentQuestionIndex.value] = optionIndex;
  answered.value = true;

  // Add delay for visual feedback, then proceed
  setTimeout(() => {
    if (currentQuestionIndex.value < questions.value.length - 1) {
      currentQuestionIndex.value++;
      answered.value = false;
    } else {
      // Game completed
      gameCompleted.value = true;
      handleGameComplete();
    }
  }, 1500); // Slightly longer delay to see correct/incorrect feedback
};

const restartGame = () => {
  currentQuestionIndex.value = 0;
  answers.value = [];
  answered.value = false;
  gameCompleted.value = false;
  initializeGame();
};

const getOptionClasses = (optionIndex: number) => {
  const selectedIndex = answers.value[currentQuestionIndex.value];

  if (!answered.value) {
    return "bg-gray-200 hover:bg-gray-300 text-gray-800 border-gray-300";
  }

  // After answering, show correct/incorrect colors
  const isCorrectAnswer = optionIndex === currentQuestion.value?.correctIndex;
  const isSelectedAnswer = optionIndex === selectedIndex;

  if (isCorrectAnswer) {
    // Correct answer - green
    return isSelectedAnswer
      ? "bg-green-500 text-white border-green-500 border-4"
      : "bg-green-100 text-green-800 border-green-300";
  } else if (isSelectedAnswer) {
    // Selected wrong answer - red
    return "bg-red-500 text-white border-red-500 border-4";
  } else {
    // Other wrong answers - gray
    return "bg-gray-200 text-gray-600 border-gray-300";
  }
};

const handleGameComplete = async () => {
  // 检查彩蛋条件：答对所有16题
  if (correctAnswers.value === questions.value.length) {
    // 触发彩蛋
    easterEggMessage.value = CAT_EASTER_EGG_UUID;
    console.log(CAT_EASTER_EGG_UUID);
    showEasterEgg.value = true;
  }

  if (!props.levelUuid || correctAnswers.value < 10) {
    return;
  }

  isSubmitting.value = true;

  try {
    const result = await completeGame(props.levelUuid, sessionId.value, {
      gameWon: true,
    });

    if (result.success) {
      emit("gameComplete", {
        success: true,
        message: `恭喜！您答对了 ${correctAnswers.value} 题，成功过关！`,
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
</script>
