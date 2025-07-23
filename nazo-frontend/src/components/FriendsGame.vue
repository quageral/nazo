<template>
  <div class="h-full flex items-center justify-center p-8 bg-gradient-to-br from-orange-50 to-pink-100">
    <div class="max-w-4xl w-full bg-white rounded-xl shadow-xl p-12 m-10">
      <div class="flex flex-col items-center py-12">
        <!-- Game Area -->
        <div class="w-full max-w-3xl">
          <!-- Question Display Area -->
          <div v-if="!gameCompleted" class="mb-16">
            <div class="text-center mb-12">
              <h2 v-if="!gameCompleted || !isEditingTitle" class="text-3xl font-bold text-gray-800 mb-8">
                🏠 {{ titleText }} 🏠
              </h2>
              <p class="text-lg text-gray-600 mb-10">
                题目 {{ currentQuestionIndex + 1 }} / {{ questions.length }}
              </p>
              <div class="w-full bg-gray-200 rounded-full h-2 mt-8">
                <div class="bg-orange-500 h-2 rounded-full transition-all duration-300" :style="{
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
              <!-- 可编辑的标题（彩蛋功能） -->
              <div class="text-center mb-8">
                <div class="text-3xl font-bold text-gray-800">
                  🏠 <input v-model="titleText" @input="handleTitleInput"
                    class="bg-transparent border-transparent outline-none text-center w-auto min-w-0 px-0 py-0 m-0"
                    style="color: inherit; font-family: inherit; font-size: inherit; font-weight: inherit; line-height: inherit; letter-spacing: inherit; text-align: center; background: transparent !important; border: none !important; outline: none !important; box-shadow: none !important;"
                    :style="{ width: titleText.length * 0.6 + 'em' }" /> 🏠
                </div>
              </div>
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
              <div v-if="correctAnswers >= 25" class="text-green-600 font-bold text-2xl mb-10">
                🎉 恭喜通关！答对了{{ correctAnswers }}题！
              </div>
              <div v-else class="text-orange-600 font-bold text-2xl mb-10">
                😅 需要答对至少25题才能通关（当前答对{{ correctAnswers }}题）
              </div>

              <button v-if="correctAnswers < 25" @click="restartGame"
                class="px-10 py-6 bg-purple-500 hover:bg-purple-600 text-white rounded-md font-semibold transition-colors text-2xl mt-8">
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
          class="bg-gradient-to-r from-orange-500 to-pink-600 hover:from-orange-600 hover:to-pink-700 text-white px-6 py-3 rounded-xl font-semibold transition-all duration-200 transform hover:scale-105">
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
import { FRIENDS_EASTER_EGG_UUID } from "@/constants/levels";

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

// 新增：标题编辑彩蛋相关状态
const titleText = ref("Friends");
const isEditingTitle = ref(false);

// Get username
const username = ref(localStorage.getItem("nazo_user") || "");

// Friends trivia questions extracted from HTML
const questions = ref([
  {
    question:
      "The main characters in Friends are Rachel, Monica, Phoebe, Joey, _____, and Ross.",
    options: [
      { text: "Sheldon", value: 0 },
      { text: "Jake", value: 0 },
      { text: "Chandler", value: 3 },
      { text: "Homer", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "Friends is predominantly set in which major American city?",
    options: [
      { text: "Washington", value: 0 },
      { text: "Los Angeles", value: 0 },
      { text: "New York", value: 3 },
      { text: "Las Vegas", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question:
      "Which title was NOT considered for the show before producers settled on Friends?",
    options: [
      { text: "Friends Like Us", value: 0 },
      { text: "Six of Us", value: 3 },
      { text: "Across the Hall", value: 0 },
      { text: "Once Upon a Time in the West Village", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "Which of these songs is NOT performed by Phoebe on the show?",
    options: [
      { text: "Two of Them Kissed Last Night", value: 0 },
      { text: "Grandpa", value: 3 },
      { text: "Sticky Shoes", value: 0 },
      { text: "Little Black Curly Hair", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "What is Chandler's middle name?",
    options: [
      { text: "Bing", value: 0 },
      { text: "Eustace", value: 0 },
      { text: "Muriel", value: 3 },
      { text: "Francis", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question:
      'Which famous guest actor portrays Joey\'s co-star in the World War I period movie entitled "Over There"?',
    options: [
      { text: "Sean Penn", value: 0 },
      { text: "Ted Danson", value: 0 },
      { text: "Gary Oldman", value: 3 },
      { text: "Bruce Willis", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "Who does Ross sleep with when he and Rachel are 'on a break'?",
    options: [
      { text: "Julie", value: 0 },
      { text: "Emily", value: 0 },
      { text: "Chloe", value: 3 },
      { text: "Janice", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "Who gives Phoebe away at her wedding to Mike?",
    options: [
      { text: "Frank Jr.", value: 0 },
      { text: "Ross", value: 0 },
      { text: "Joey", value: 0 },
      { text: "Chandler", value: 3 },
    ],
    correctIndex: 3,
  },
  {
    question:
      "Why was Joey's character, Dr. Drake Ramoray, killed off in Days of Our Lives?",
    options: [
      {
        text: "Joey annoyed the director by forgetting his lines too often",
        value: 0,
      },
      {
        text: "He insulted the writers by claiming he wrote his own lines",
        value: 3,
      },
      { text: "The writers ran out of storylines for the character", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "How did Dr. Drake Ramoray die in Days of Our Lives?",
    options: [
      { text: "He was murdered by his ex-lover's husband", value: 0 },
      { text: "He fell down an elevator shaft at the hospital", value: 3 },
      { text: "He contracted a rare and deadly tropical disease", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "How was Dr. Drake Ramoray revived in Days of Our Lives?",
    options: [
      {
        text: "He came back as a secret evil twin intent on revenge",
        value: 0,
      },
      {
        text: "He woke up from amnesia and realized he had been living a double life",
        value: 0,
      },
      { text: "He underwent a dangerous brain transplant operation", value: 3 },
    ],
    correctIndex: 2,
  },
  {
    question: 'Which Friends character said: "See? He\'s her lobster!"?',
    options: [
      { text: "Rachel", value: 0 },
      { text: "Joey", value: 0 },
      { text: "Phoebe", value: 3 },
      { text: "Janice", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question:
      'Can you guess how many times Joey says his famous catchphrase, "How you doin\'?" over 10 seasons?',
    options: [
      { text: "19", value: 3 },
      { text: "25", value: 0 },
      { text: "42", value: 0 },
      { text: "Over 50 times!", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question:
      "Monica was upset to learn that her parents spent her wedding fund because they believed she would never get married. What did they spend it on?",
    options: [
      { text: "A yacht", value: 0 },
      { text: "A beach house", value: 3 },
      { text: "A holiday in Florida", value: 0 },
      { text: "A Porsche", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question:
      "Rachel dated some memorable (and not-so-memorable) characters over the course of 10 seasons. Who was NOT one of her hook-ups?",
    options: [
      { text: "Paul Stevens", value: 0 },
      { text: "Paul the Wine Guy", value: 3 },
      { text: "Mark Robinson", value: 0 },
      { text: "Joey Tribbiani", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "What is the name of Rachel's cat?",
    options: [
      { text: "Smelly Cat", value: 0 },
      { text: "Mr. Mistoffelees", value: 0 },
      { text: "Mrs. Whiskerson", value: 3 },
      { text: "Mr. Meowserson", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question:
      "Over the course of 236 episodes, the characters drink a total of 1,154 cups of coffee. Can you guess who was the heaviest drinker?",
    options: [
      { text: "Joey", value: 0 },
      { text: "Rachel", value: 0 },
      { text: "Monica", value: 0 },
      { text: "Phoebe", value: 3 },
    ],
    correctIndex: 3,
  },
  {
    question: "How many sisters does Joey have?",
    options: [
      { text: "5", value: 0 },
      { text: "6", value: 0 },
      { text: "7", value: 3 },
      { text: "8", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "Which of Joey's sisters does Chandler hook up with?",
    options: [
      { text: "Mary Therese", value: 0 },
      { text: "Mary Angela", value: 3 },
      { text: "Tina", value: 0 },
      { text: "Gina", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "Which famous guest actor did Rachel hook up with?",
    options: [
      { text: "Brad Pitt", value: 0 },
      { text: "Bruce Willis", value: 3 },
      { text: "Billy Crystal", value: 0 },
      { text: "Tom Selleck", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question:
      'When Heckles dies, he leaves his possessions to "the noisy girls upstairs". Which item causes a disagreement between Monica and Rachel?',
    options: [
      { text: "A giant magnifying glass on a stand", value: 0 },
      { text: "My Big Book of Grievances", value: 0 },
      { text: "A lamp made from seashells", value: 3 },
      { text: "The girlie clock", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "What is Chandler's father's drag queen name?",
    options: [
      { text: "Misty Meaner", value: 0 },
      { text: "Allison Chains", value: 0 },
      { text: "Helena Handbasket", value: 3 },
      { text: "Paige Turner", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "What is Janice's first surname?",
    options: [
      { text: "Litman", value: 0 },
      { text: "Liebowitz", value: 0 },
      { text: "Goralnik", value: 0 },
      { text: "Hosenstein", value: 3 },
    ],
    correctIndex: 3,
  },
  {
    question:
      "Where did David the Scientist Guy move to, causing him to split up with Phoebe?",
    options: [
      { text: "Barbados", value: 0 },
      { text: "Tulsa", value: 0 },
      { text: "Minsk", value: 3 },
      { text: "Paris", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "Which famous actress did NOT audition for the role of Rachel?",
    options: [
      { text: "Téa Leoni", value: 0 },
      { text: "Jane Krakowski", value: 0 },
      { text: "Ellen DeGeneres", value: 3 },
      { text: "Denise Richards", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question:
      'In the 2-part alternate reality episode, "The One That Could Have Been", what was Phoebe\'s job?',
    options: [
      { text: "Anesthesiologist", value: 0 },
      { text: "Software Engineer", value: 0 },
      { text: "Stockbroker", value: 3 },
      { text: "Veterinarian", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question:
      "Gunther appeared in 185 episodes of Friends, but he didn't have a speaking role until halfway through the second season. What was his first line?",
    options: [
      { text: "Rachel!", value: 0 },
      { text: "I dropped a cup", value: 0 },
      { text: "Yeah", value: 3 },
      { text: "I thought you were Chandler", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question:
      'Which character said: "Oh, damn the jellyfish. Damn all the jellyfish!"?',
    options: [
      { text: "Phoebe", value: 0 },
      { text: "Chandler", value: 0 },
      { text: "Rachel", value: 0 },
      { text: "Monica", value: 3 },
    ],
    correctIndex: 3,
  },
  {
    question: "How many episodes of Friends did David Schwimmer direct?",
    options: [
      { text: "5", value: 0 },
      { text: "10", value: 3 },
      { text: "15", value: 0 },
      { text: "20", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "What is the name of Ross and Chandler's college band?",
    options: [
      { text: "Can You Dig It", value: 0 },
      { text: "Way! No Way!", value: 3 },
      { text: "Far Out Man", value: 0 },
      { text: "Right On, Man!", value: 0 },
    ],
    correctIndex: 1,
  },
  {
    question: "Joey is offered the role of butt double for which famous actor?",
    options: [
      { text: "Robert De Niro", value: 0 },
      { text: "Andy Garcia", value: 0 },
      { text: "Al Pacino", value: 2 },
      { text: "Jack Nicholson", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "Which of these was NOT one of Joey's acting gigs?",
    options: [
      { text: "Corpse in Law & Order", value: 0 },
      { text: "Extra in Jean-Claude van Damme film", value: 0 },
      { text: "Stage role in MacBeth", value: 0 },
      { text: "Holden McGroin in Shutter Speed", value: 2 },
    ],
    correctIndex: 3,
  },
  {
    question: "Who is Chandler's TV guide addressed to?",
    options: [
      { text: "Mrs. Chandeler Bing", value: 0 },
      { text: "Mr. Chan Tribbiani", value: 0 },
      { text: "Mrs. Chanandler Bong", value: 2 },
      { text: "Mr. Chandlerbing Tribbiani", value: 0 },
    ],
    correctIndex: 2,
  },
  {
    question: "Who was the only Friend NOT nominated for an Emmy Award?",
    options: [
      { text: "Monica", value: 2 },
      { text: "Rachel", value: 0 },
      { text: "Joey", value: 0 },
      { text: "Phoebe", value: 0 },
    ],
    correctIndex: 0,
  },
  {
    question:
      "When the final Friends episode aired in 2004, the actors were allowed to take home a memento from the set. What did they choose?",
    options: [
      { text: "Their favorite coffee cups from Central Perk", value: 0 },
      {
        text: "A prop from Monica's apartment that had personal meaning to each of them",
        value: 0,
      },
      { text: "A piece of the sidewalk from outside Central Perk", value: 2 },
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
  if (correctAnswers.value >= 25) return "text-green-600";
  if (correctAnswers.value >= 20) return "text-yellow-600";
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
  }, 500);
};

const restartGame = () => {
  currentQuestionIndex.value = 0;
  answers.value = [];
  answered.value = false;
  gameCompleted.value = false;
  titleText.value = "Friends"; // 重置标题
  isEditingTitle.value = false; // 重置编辑状态
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

// 标题编辑相关函数
const handleTitleInput = (event: Event) => {
  const target = event.target as HTMLInputElement;
  titleText.value = target.value;

  // 检查是否输入了"Family"或者“Framily”
  if (titleText.value.toLowerCase() === "family" || titleText.value.toLowerCase() === "framily") {
    easterEggMessage.value = FRIENDS_EASTER_EGG_UUID;
    console.log(FRIENDS_EASTER_EGG_UUID);
    showEasterEgg.value = true;
  }
};

const handleGameComplete = async () => {
  // 游戏完成后启用标题编辑功能
  isEditingTitle.value = true;

  // 检查彩蛋条件：答对所有35题
  if (correctAnswers.value === questions.value.length) {
    // 触发彩蛋
    easterEggMessage.value = FRIENDS_EASTER_EGG_UUID;
    console.log(FRIENDS_EASTER_EGG_UUID);
    showEasterEgg.value = true;
  }

  if (!props.levelUuid || correctAnswers.value < 25) {
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
