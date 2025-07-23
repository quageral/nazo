<template>
  <div class="h-full flex items-center justify-center p-8 bg-gradient-to-br from-green-50 to-blue-100">
    <div class="max-w-4xl w-full bg-white rounded-xl shadow-xl p-12 m-10">
      <div class="flex flex-col items-center py-12">
        <!-- Game Area -->
        <div class="w-full max-w-3xl">
          <!-- Question Display Area -->
          <div v-if="!gameCompleted" class="mb-16">
            <div class="text-center mb-12">
              <h2 class="text-3xl font-bold text-gray-800 mb-8">
                ⛏️ Minecraft ⛏️
              </h2>
              <p class="text-lg text-gray-600 mb-10">
                题目 {{ currentQuestionIndex + 1 }} / {{ questions.length }}
              </p>
              <div class="w-full bg-gray-200 rounded-full h-2 mt-8">
                <div class="bg-green-500 h-2 rounded-full transition-all duration-300" :style="{
                  width:
                    ((currentQuestionIndex + 1) / questions.length) * 100 +
                    '%',
                }"></div>
              </div>
            </div>

            <!-- Current Question -->
            <div v-if="currentQuestion" class="questionWrapper bg-gray-50 p-12 rounded-lg mt-12">
              <div class="question text-xl font-semibold text-gray-800 mb-12 text-center leading-relaxed"
                v-html="currentQuestion.question"></div>

              <!-- Answer Options -->
              <div class="space-y-6">
                <button v-for="(option, index) in currentQuestion.options" :key="index" @click="selectAnswer(index)"
                  :class="getOptionClasses(index)"
                  class="answer w-full px-8 py-6 rounded-md font-semibold text-lg transition-all duration-200 hover:scale-105 flex justify-between items-center"
                  :disabled="answered">
                  <span>{{ getOptionLabel(index) }}. {{ option.text }}</span>
                  <span v-if="answered" class="text-2xl font-bold" :class="{
                    'text-green-600': index === currentQuestion.correctIndex,
                    'text-red-600': index !== currentQuestion.correctIndex,
                  }">
                    {{ index === currentQuestion.correctIndex ? "✓" : "✗" }}
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
                {{ totalScore }} 分
              </div>
              <p class="text-lg text-gray-600 mb-12">
                您答对了 {{ correctAnswers }} / {{ questions.length }} 题
              </p>
            </div>

            <!-- Result Actions -->
            <div class="space-y-8">
              <div v-if="correctAnswers >= 18" class="text-green-600 font-bold text-2xl mb-10">
                🎉 恭喜通关！答对了{{ correctAnswers }}题！
              </div>
              <div v-else class="text-orange-600 font-bold text-2xl mb-10">
                😅 需要答对至少18题才能通关（当前答对{{ correctAnswers }}题）
              </div>

              <button v-if="correctAnswers < 18" @click="restartGame"
                class="px-10 py-6 bg-green-500 hover:bg-green-600 text-white rounded-md font-semibold transition-colors text-2xl mt-8">
                重新挑战
              </button>
            </div>
          </div>
        </div>
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
          class="bg-gradient-to-r from-green-500 to-blue-600 hover:from-green-600 hover:to-blue-700 text-white px-6 py-3 rounded-xl font-semibold transition-all duration-200 transform hover:scale-105">
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
import { MINECRAFT_EASTER_EGG_UUID } from "@/constants/levels";

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
const hasRestarted = ref(false); // 新增：追踪是否已重新开始过游戏

// Get username
const username = ref(localStorage.getItem("nazo_user") || "");

// Minecraft trivia questions extracted from HTML
const questions = ref([
  {
    question: "挖掘石头和砖块的最佳工具是什么？",
    options: [
      { text: "铲子", value: 0 },
      { text: "镐", value: 4 },
      { text: "斧头", value: 0 },
      { text: "剑", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "制作一个熔炉总共需要多少个圆石块？",
    options: [
      { text: "6 个圆石", value: 0 },
      { text: "9 个圆石", value: 0 },
      { text: "10 个圆石", value: 0 },
      { text: "8 个圆石", value: 4 },
    ],
    correctIndex: 3,
  },
  {
    question: "最弱的 Minecraft 盔甲是用什么制作的？",
    options: [
      { text: "铁", value: 0 },
      { text: "锁链甲", value: 0 },
      { text: "皮革", value: 4 },
      { text: "金", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "Minecraft 创始人的昵称是什么？",
    options: [
      { text: "Zizot", value: 0 },
      { text: "Apple", value: 0 },
      { text: "Dinnerbone", value: 0 },
      { text: "Notch", value: 4 },
    ],
    correctIndex: 3,
  },
  {
    question: "Minecraft 最初发布时的原始名称是什么？",
    options: [
      { text: "Mine-Crafter", value: 0 },
      { text: "Cave Game", value: 4 },
      { text: "Procedural Game", value: 0 },
      { text: "Minicraft", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "以下哪种动物可以与马和骡子一起用鞍骑乘？",
    options: [
      { text: "猪", value: 4 },
      { text: "蜜蜂", value: 0 },
      { text: "牛", value: 0 },
      { text: "山羊", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "与铁制盔甲和工具/武器相比，黄金盔甲和工具/武器有什么缺点？",
    options: [
      { text: "它们太闪亮了", value: 0 },
      { text: "它们的耐用性比铁低，更容易损坏", value: 4 },
      {
        text: "与其他盔甲相比，它们能给你带来更多的箭和抛射物伤害。",
        value: 0,
      },
      { text: "没有缺点，它们是完全一样的", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "哪种材料能制造出最坚固的盔甲或武器？",
    options: [
      { text: "铁", value: 0 },
      { text: "下界合金", value: 4 },
      { text: "钻石", value: 0 },
      { text: "绿宝石", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "当爬行者Creeper被闪电击中时会发生什么？",
    options: [
      { text: "它会带电，爆炸威力更大", value: 4 },
      { text: "它会带电，并在爆炸时用闪电击中你", value: 0 },
      { text: "它会立即爆炸", value: 0 },
      { text: "它会立即死亡", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "以下哪一项物品不存在于原版 Minecraft 中？",
    options: [
      { text: "金胡萝卜", value: 0 },
      { text: "雪桶", value: 0 },
      { text: "银锭", value: 4 },
      { text: "钟", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "末影龙被击败后会掉落什么？",
    options: [
      { text: "经验和龙蛋", value: 4 },
      { text: "经验、一组钻石和末影珍珠", value: 0 },
      { text: "经验和鞘翅", value: 0 },
      { text: "火药和铁块", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "哪种方块挖掘时间最长？",
    options: [
      { text: "远古残骸", value: 0 },
      { text: "钻石矿", value: 0 },
      { text: "黑曜石", value: 4 },
      { text: "钻石块", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "Minecraft 的白天和黑夜加起来的现实时间有多长？",
    options: [
      { text: "20 分钟", value: 4 },
      { text: "4 小时", value: 0 },
      { text: "12 小时", value: 0 },
      { text: "5 分钟", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "Minecraft 开发者工作室叫什么名字？",
    options: [
      { text: "Electronic Arts", value: 0 },
      { text: "Mojang", value: 4 },
      { text: "Activision", value: 0 },
      { text: "Bethesda Softworks", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "如果流动的水接触到熔岩源会发生什么？",
    options: [
      { text: "会生成黑曜石", value: 4 },
      { text: "水消失", value: 0 },
      { text: "会生成圆石", value: 0 },
      { text: "什么也不会发生", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "苹果是怎么找到的？",
    options: [
      { text: "从被破坏的树叶中掉落", value: 4 },
      { text: "从杀死猪和骷髅中获得", value: 0 },
      { text: "它们在草方块上随机出现", value: 0 },
      { text: "作为完成成就的奖励", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "以下哪一种只在末地维度中发现？",
    options: [
      { text: "钻石", value: 0 },
      { text: "下界合金", value: 0 },
      { text: "金苹果", value: 0 },
      { text: "鞘翅", value: 4 },
    ],
    correctIndex: 3,
  },
  {
    question: "以下哪一种只在下界维度中发现？",
    options: [
      { text: "远古残骸", value: 4 },
      { text: "红蘑菇", value: 0 },
      { text: "普通骷髅", value: 0 },
      { text: "金矿石", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "当你在下界维度放置一张床，然后右键点击它试图睡觉时会发生什么？",
    options: [
      { text: "它会爆炸", value: 4 },
      { text: "它不会被放置下来", value: 0 },
      { text: "它会消失", value: 0 },
      { text: "什么也不会发生，它像往常一样工作", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "Minecraft 正式发布 1.0 版本是在哪一年？",
    options: [
      { text: "2011", value: 4 },
      { text: "2012", value: 0 },
      { text: "2016", value: 0 },
      { text: "2017", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "如果你在泥土上使用锄头，水源周围有多少个泥土块会被浇灌？",
    options: [
      { text: "所有方向 4 个方块", value: 3 },
      { text: "所有方向 2 个方块", value: 0 },
      { text: "只有横向 3 个方块", value: 0 },
      { text: "所有方向 1 个方块", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "如果你在极限 Minecraft 世界中死亡会发生什么？",
    options: [
      { text: "你会重生，失去所有物品", value: 0 },
      { text: "你的世界会被锁定，你无法在同一个世界继续游戏", value: 3 },
      { text: "你的 Minecraft 账户的高级状态被撤销", value: 0 },
      { text: "你会重生，所有物品都会保留", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "截至 1.18 版本，你可以向下挖掘到的 Minecraft 最低深度是多少？",
    options: [
      { text: "12", value: 0 },
      { text: "0", value: 0 },
      { text: "-64", value: 3 },
      { text: "48", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "你需要用什么作为粗制药水的成分？",
    options: [
      { text: "下界疣", value: 3 },
      { text: "萤石粉", value: 0 },
      { text: "鱼", value: 0 },
      { text: "闪烁的西瓜片", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "除了物品本身和经验之外，附魔物品所需的材料是什么？",
    options: [
      { text: "下界疣", value: 0 },
      { text: "金锭", value: 0 },
      { text: "青金石", value: 3 },
      { text: "铁锭", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "以下哪一项不能从附魔台中附魔？",
    options: [
      { text: "剑", value: 0 },
      { text: "斧头", value: 0 },
      { text: "书", value: 0 },
      { text: "望远镜", value: 1 },
    ],
    correctIndex: 3,
  },
  {
    question: "如果你穿戴上带有绑定诅咒附魔的物品会发生什么？",
    options: [
      { text: "你会将你的力量绑定到它上面", value: 0 },
      { text: "你无法将其脱下，直到它损坏或直到你死亡", value: 1 },
      { text: "如果你靠近村庄，你会导致村庄受到攻击", value: 0 },
      { text: "它可以作为额外的保护值来防御生物和玩家", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "如果通过剑使用火焰附加附魔，会发生什么？",
    options: [
      { text: "你会在攻击时点燃你的对手", value: 1 },
      { text: "你可以在 5 秒内免受火焰攻击", value: 0 },
      { text: "如果玩家或生物着火，你可以扑灭他们", value: 0 },
      { text: "它会在使用时融化你周围的雪", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question: "忠诚三叉戟附魔会导致什么？",
    options: [
      { text: "你只能将三叉戟用作近战武器，但它更强大", value: 0 },
      { text: "三叉戟在投掷后会返回", value: 1 },
      { text: "三叉戟造成更多伤害", value: 0 },
      { text: "忠诚不能应用于三叉戟", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "经验修补附魔对物品有什么作用？",
    options: [
      { text: "它会使它在使用时立即损坏", value: 0 },
      { text: "它会用获得的经验球修复该特定物品", value: 1 },
      { text: "如果用在武器上，它会对生物造成更多伤害", value: 0 },
      { text: "它会增加矿石掉落率", value: 0 },
    ],
    correctIndex: 1,
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
  if (correctAnswers.value >= 18) return "text-green-600";
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
  hasRestarted.value = true; // 标记已重新开始过游戏
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
  // 检查彩蛋条件：如果用户重新开始过但仍未达到18题，则触发彩蛋
  if (hasRestarted.value && correctAnswers.value < 18) {
    easterEggMessage.value = MINECRAFT_EASTER_EGG_UUID;
    console.log(MINECRAFT_EASTER_EGG_UUID);
    showEasterEgg.value = true;
  }

  // 检查满分彩蛋条件（保留原有逻辑）
  if (correctAnswers.value === questions.value.length) {
    easterEggMessage.value = MINECRAFT_EASTER_EGG_UUID;
    console.log(MINECRAFT_EASTER_EGG_UUID);
    showEasterEgg.value = true;
  }

  if (!props.levelUuid || correctAnswers.value < 18) {
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
