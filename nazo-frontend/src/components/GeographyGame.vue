<template>
  <div
    class="h-full flex items-center justify-center p-8 bg-gradient-to-br from-blue-50 to-green-100"
  >
    <div class="max-w-4xl w-full bg-white rounded-xl shadow-xl p-12 m-10">
      <div class="flex flex-col items-center py-12">
        <!-- Game Area -->
        <div class="w-full max-w-3xl">
          <!-- Question Display Area -->
          <div v-if="!gameCompleted" class="mb-16">
            <div class="text-center mb-12">
              <h2 class="text-3xl font-bold text-gray-800 mb-8">
                🌍 Geography 🌍
              </h2>
              <p class="text-lg text-gray-600 mb-10">
                题目 {{ currentQuestionIndex + 1 }} / {{ questions.length }}
              </p>
              <div class="w-full bg-gray-200 rounded-full h-2 mt-8">
                <div
                  class="bg-blue-500 h-2 rounded-full transition-all duration-300"
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
              <div
                class="question text-xl font-semibold text-gray-800 mb-12 text-center leading-relaxed"
                v-html="currentQuestion.question"
              ></div>

              <!-- Options -->
              <div class="space-y-6">
                <button
                  v-for="(option, index) in currentQuestion.options"
                  :key="index"
                  @click="selectAnswer(index)"
                  :disabled="answered"
                  :class="getOptionClasses(index)"
                  class="w-full p-6 text-left text-lg rounded-xl border-2 transition-all duration-300 relative"
                >
                  <span class="font-bold mr-4"
                    >{{ getOptionLabel(index) }}.</span
                  >
                  <span>{{ option.text }}</span>

                  <!-- Answer Feedback -->
                  <div
                    v-if="answered"
                    class="absolute right-4 top-1/2 transform -translate-y-1/2 text-2xl"
                  >
                    <span
                      v-if="index === currentQuestion.correctIndex"
                      class="text-green-600"
                      >✓</span
                    >
                    <span
                      v-else-if="index === answers[currentQuestionIndex]"
                      class="text-red-600"
                      >✗</span
                    >
                  </div>
                </button>
              </div>
            </div>
          </div>

          <!-- Game Complete Screen -->
          <div v-else class="text-center">
            <h2 class="text-4xl font-bold text-gray-800 mb-12">
              🌍 Geography完成！ 🌍
            </h2>

            <!-- Score Display -->
            <div class="bg-gray-50 p-12 rounded-lg mb-12">
              <div class="text-6xl mb-8" :class="scoreColor">
                {{ correctAnswers }} / {{ questions.length }}
              </div>
              <div class="text-2xl text-gray-600 mb-8">
                正确率:
                {{ Math.round((correctAnswers / questions.length) * 100) }}%
              </div>
            </div>

            <!-- Result Actions -->
            <div class="space-y-8">
              <div
                v-if="correctAnswers >= 16"
                class="text-green-600 font-bold text-2xl mb-10"
              >
                🎉 恭喜通关！答对了{{ correctAnswers }}题！
              </div>
              <div v-else class="text-orange-600 font-bold text-2xl mb-10">
                😅 需要答对至少16题才能通关（当前答对{{ correctAnswers }}题）
              </div>

              <button
                v-if="correctAnswers < 16"
                @click="restartGame"
                class="px-10 py-6 bg-blue-500 hover:bg-blue-600 text-white rounded-md font-semibold transition-colors text-2xl mt-8"
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
          class="bg-gradient-to-r from-blue-500 to-green-600 hover:from-blue-600 hover:to-green-700 text-white px-6 py-3 rounded-xl font-semibold transition-all duration-200 transform hover:scale-105"
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
import { GEOGRAPHY_EASTER_EGG_UUID } from "@/constants/levels";

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

// Geography quiz questions parsed from HTML
const questions = ref([
  {
    question: "2014年冬奥会举办地索契的冬季均温约为",
    options: [
      { text: "-15℃－-5℃", value: 0 },
      { text: "-5℃－5℃", value: 0 },
      { text: "5℃－10℃", value: 5 },
    ],
    correctIndex: 2,
  },
  {
    question: "下列选项中人口密度最大的国家是",
    options: [
      { text: "中国", value: 0 },
      { text: "日本", value: 0 },
      { text: "印度", value: 0 },
      { text: "孟加拉国", value: 5 },
    ],
    correctIndex: 3,
  },
  {
    question: "世界上最大的内陆国是",
    options: [
      { text: "蒙古", value: 0 },
      { text: "哈萨克斯坦", value: 5 },
      { text: "尼泊尔", value: 0 },
      { text: "吉尔吉斯斯坦", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "战斗民族俄罗斯的麻(mu)麻(qin)河伏尔加河最终汇入的是",
    options: [
      { text: "北冰洋", value: 0 },
      { text: "大西洋", value: 0 },
      { text: "贝加尔湖", value: 0 },
      { text: "里海", value: 5 },
    ],
    correctIndex: 3,
  },
  {
    question: "因为有了伏尔加河所以只能屈居欧洲第二的多瑙河最终又汇入",
    options: [
      { text: "黑海", value: 5 },
      { text: "北冰洋", value: 0 },
      { text: "大西洋", value: 0 },
      { text: "地中海", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "肯德基爷爷来自美国的：",
    options: [
      { text: "田纳西州", value: 0 },
      { text: "肯塔基州", value: 5 },
      { text: "肯德基州", value: 0 },
      { text: "密苏里州", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "肯德基爷爷相爱相杀的好基友麦当劳蜀黍来自",
    options: [
      { text: "美国", value: 0 },
      { text: "拉脱维亚", value: 5 },
      { text: "西班牙", value: 0 },
      { text: "立陶宛", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "物美价廉又好吃的红薯产自",
    options: [
      { text: "美洲", value: 5 },
      { text: "非洲", value: 0 },
      { text: "东亚地区", value: 0 },
      { text: "中亚地区", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "世界公认农业科技最为发达的国家是",
    options: [
      { text: "以色列", value: 5 },
      { text: "美国", value: 0 },
      { text: "澳大利亚", value: 0 },
      { text: "日本", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "从中国大陆到挪威，不坐船最少要经过几个国家",
    options: [
      { text: "10", value: 0 },
      { text: "7", value: 0 },
      { text: "4", value: 0 },
      { text: "1", value: 5 },
    ],
    correctIndex: 3,
  },
  {
    question: "以下国家距离南极洲最近的是",
    options: [
      { text: "新西兰", value: 0 },
      { text: "南非", value: 0 },
      { text: "智利", value: 5 },
      { text: "阿根廷", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "《托斯卡纳艳阳下》一书中的托斯卡纳位于",
    options: [
      { text: "摩纳哥", value: 0 },
      { text: "希腊", value: 0 },
      { text: "西班牙", value: 0 },
      { text: "意大利", value: 5 },
    ],
    correctIndex: 3,
  },
  {
    question: "在中亚地区众多的“斯坦”国家中，哪个斯坦不与中国接壤",
    options: [
      { text: "乌兹别克斯坦", value: 5 },
      { text: "塔吉克斯坦", value: 0 },
      { text: "吉尔吉斯斯坦", value: 0 },
      { text: "哈萨克斯坦", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "世界上平均海拔最高的国家是",
    options: [
      { text: "莱索托", value: 5 },
      { text: "南非", value: 0 },
      { text: "智利", value: 0 },
      { text: "摩纳哥", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "从行政区划分来讲，景德镇是一个什么？",
    options: [
      { text: "县", value: 0 },
      { text: "市", value: 5 },
      { text: "镇", value: 0 },
      { text: "乡", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "北京市关于地心的对称点位于",
    options: [
      { text: "南太平洋", value: 0 },
      { text: "东非大裂谷", value: 0 },
      { text: "南印度洋", value: 0 },
      { text: "阿根廷潘帕斯草原", value: 5 },
    ],
    correctIndex: 3,
  },
  {
    question: "国中国梵蒂冈的国土面积约等于几个天安门广场的大小？",
    options: [
      { text: "一个", value: 5 },
      { text: "两个", value: 0 },
      { text: "三个", value: 0 },
      { text: "五个", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "亚马孙河干流发源于",
    options: [
      { text: "秘鲁", value: 5 },
      { text: "巴西西北部", value: 0 },
      { text: "哥伦比亚", value: 0 },
      { text: "厄瓜多尔", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "世界上最年轻的海是",
    options: [
      { text: "黑海", value: 0 },
      { text: "里海", value: 0 },
      { text: "红海", value: 5 },
      { text: "波罗的海", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "以下哪个国家拥有最多的时区",
    options: [
      { text: "俄罗斯", value: 0 },
      { text: "美国", value: 0 },
      { text: "法国", value: 5 },
      { text: "挪威", value: 0 },
    ],
    correctIndex: 2,
  },
]);

// Computed properties
const currentQuestion = computed(() => {
  return questions.value[currentQuestionIndex.value] || null;
});

const totalScore = computed(() => {
  return answers.value.reduce((sum, answerIndex, questionIndex) => {
    if (answerIndex === questions.value[questionIndex].correctIndex) {
      return sum + questions.value[questionIndex].options[answerIndex].value;
    }
    return sum;
  }, 0);
});

const correctAnswers = computed(() => {
  return answers.value.reduce((count, answerIndex, questionIndex) => {
    return answerIndex === questions.value[questionIndex].correctIndex
      ? count + 1
      : count;
  }, 0);
});

const scoreColor = computed(() => {
  if (correctAnswers.value >= 16) return "text-green-600";
  if (correctAnswers.value >= 12) return "text-yellow-600";
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
  }, 1500);
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
  // 检查彩蛋条件：答对所有20题
  if (correctAnswers.value === questions.value.length) {
    // 触发彩蛋
    easterEggMessage.value = GEOGRAPHY_EASTER_EGG_UUID;
    console.log(GEOGRAPHY_EASTER_EGG_UUID);
    showEasterEgg.value = true;
  }

  if (!props.levelUuid || correctAnswers.value < 16) {
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
