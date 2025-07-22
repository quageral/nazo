<template>
    <div class="h-full flex items-center justify-center p-8 bg-gradient-to-br from-blue-50 to-indigo-100">
        <div class="max-w-4xl w-full bg-white rounded-xl shadow-xl p-12 m-10">
            <div class="flex flex-col items-center py-12">

                <!-- Game Area -->
                <div class="w-full max-w-3xl">
                    <!-- Question Display Area -->
                    <div v-if="!gameCompleted" class="mb-16">
                        <div class="text-center mb-12">
                            <h2 class="text-2xl font-bold text-gray-800 mb-8"></h2>
                            <p class="text-lg text-gray-600 mb-10">题目 {{ currentQuestionIndex + 1 }} / {{
                                questions.length }}
                            </p>
                            <div class="w-full bg-gray-200 rounded-full h-2 mt-8">
                                <div class="bg-blue-500 h-2 rounded-full transition-all duration-300"
                                    :style="{ width: ((currentQuestionIndex + 1) / questions.length) * 100 + '%' }">
                                </div>
                            </div>
                        </div>

                        <!-- Current Question -->
                        <div v-if="currentQuestion" class="questionWrapper bg-gray-50 p-12 rounded-lg mt-12">
                            <div class="question text-xl font-semibold text-gray-800 mb-12 text-center leading-relaxed"
                                v-html="currentQuestion.question"></div>

                            <!-- Answer Options -->
                            <div class="space-y-6">
                                <button v-for="(option, index) in currentQuestion.options" :key="index"
                                    @click="selectAnswer(index)" :class="getOptionClasses(index)"
                                    class="answer w-full px-8 py-6 rounded-md font-semibold text-lg transition-all duration-200 hover:scale-105"
                                    :disabled="answered">
                                    {{ option.text }}
                                </button>
                            </div>

                            <!-- Remove the Next Button section -->
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
                            <div v-if="totalScore >= 80" class="text-green-600 font-bold text-2xl mb-10">
                                🎉 恭喜通关！
                            </div>
                            <div v-else class="text-orange-600 font-bold text-2xl mb-10">
                                😅 需要达到80分才能通关
                            </div>

                            <button v-if="totalScore < 80" @click="restartGame"
                                class="px-10 py-6 bg-purple-500 hover:bg-purple-600 text-white rounded-md font-semibold transition-colors text-2xl mt-8">
                                重新挑战
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { startGame, completeGame } from "@/services/api";

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

// Get username
const username = ref(localStorage.getItem("nazo_user") || "");

// Questions data based on the HTML source
const questions = ref([
    {
        question: '1, 2, <b style="color:red">?</b>, 4, 5',
        options: [
            { text: '2', value: 0 },
            { text: '3', value: 10 },
            { text: '1', value: 0 },
            { text: '4', value: 0 }
        ],
        correctIndex: 1
    },
    {
        question: '1, 3, <b style="color:red">?</b>, 7, 9',
        options: [
            { text: '5', value: 10 },
            { text: '3', value: 0 },
            { text: '1', value: 0 },
            { text: '13', value: 0 }
        ],
        correctIndex: 0
    },
    {
        question: '1, 10, 3, 8, <b style="color:red">?</b>, 6, 7, 4, 9, 2',
        options: [
            { text: '8', value: 0 },
            { text: '9', value: 0 },
            { text: '5', value: 10 },
            { text: '11', value: 0 }
        ],
        correctIndex: 2
    },
    {
        question: '1, 3, 9, 27, <b style="color:red">?</b>, 243',
        options: [
            { text: '36', value: 0 },
            { text: '48', value: 0 },
            { text: '81', value: 10 },
            { text: '54', value: 0 }
        ],
        correctIndex: 2
    },
    {
        question: '2, 3, 5, 7, 11, 13, 17, 19, <b style="color:red">?</b>, 29',
        options: [
            { text: '23', value: 10 },
            { text: '22', value: 0 },
            { text: '25', value: 0 },
            { text: '30', value: 0 }
        ],
        correctIndex: 0
    },
    {
        question: '916, 015, <b style="color:red">?</b>, 213, 312, 411',
        options: [
            { text: '123', value: 0 },
            { text: '332', value: 0 },
            { text: '113', value: 0 },
            { text: '114', value: 10 }
        ],
        correctIndex: 3
    },
    {
        question: '23, 57, 1113, 1719, 2329, <b style="color:red">?</b>, 4143, 4753',
        options: [
            { text: '3137', value: 10 },
            { text: '4122', value: 0 },
            { text: '51232', value: 0 },
            { text: '12523', value: 0 }
        ],
        correctIndex: 0
    },
    {
        question: '<p>1, 5, 16, 17, 18.</p><p>2, 6, 17, 18, <b style="color:red">?</b>.</p>',
        options: [
            { text: '31', value: 0 },
            { text: '22', value: 0 },
            { text: '19', value: 10 },
            { text: '5', value: 0 }
        ],
        correctIndex: 2
    },
    {
        question: '1 - 2 - 5 - 7 - 8<br>1 - 2 - 5 - 7 - 1<br>8 - 7 - <b style="color:red">?</b> - 8 - 2<br>7 - 5 - 2 - 1 - 5<br>5 - 2 - 1 - 8 - 7',
        options: [
            { text: '1', value: 0 },
            { text: '2', value: 0 },
            { text: '5', value: 0 },
            { text: '8', value: 5 },
            { text: '7', value: 0 }
        ],
        correctIndex: 3
    },
    {
        question: '6 - 2 - 7 - 6 - 6 - 2<br>2 - 0 - 7 - 0 - <b style="color:red">?</b> - 0<br>0 - 2 - 0 - 7 - 0 - 2<br>2 - 6 - 6 - 7 - 2 - 6<br>7 - 6 - 6 - 2 - 7 - 6<br>7 - 0 - 2 - 0 - 7 - 0',
        options: [
            { text: '2', value: 5 },
            { text: '0', value: 0 },
            { text: '6', value: 0 },
            { text: '7', value: 0 }
        ],
        correctIndex: 0
    },
    {
        question: '3.14, 4.28, 5.46, 6.82, 7.64, <b style="color:red">?</b>?',
        options: [
            { text: '8.99', value: 0 },
            { text: '8.28', value: 5 },
            { text: '8.38', value: 0 },
            { text: '8.00', value: 0 }
        ],
        correctIndex: 1
    },
    {
        question: '8110423, 259974, 9814143, 5512166, 113322, <b style="color:red">?</b>',
        options: [
            { text: '334455', value: 0 },
            { text: '7911132', value: 5 },
            { text: '9841233', value: 0 },
            { text: '872236', value: 0 }
        ],
        correctIndex: 1
    }
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
        return answerIndex === questions.value[questionIndex].correctIndex ? count + 1 : count;
    }, 0);
});

const scoreColor = computed(() => {
    if (totalScore.value >= 80) return 'text-green-600';
    if (totalScore.value >= 60) return 'text-yellow-600';
    return 'text-red-600';
});

// Initialize game
onMounted(async () => {
    await initializeGame();
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
    initializeGame();
};

const getOptionClasses = (optionIndex: number) => {
    const selectedIndex = answers.value[currentQuestionIndex.value];

    if (!answered.value) {
        return 'bg-gray-200 hover:bg-gray-300 text-gray-800 border-gray-300';
    }

    if (optionIndex === selectedIndex) {
        return 'bg-blue-500 text-white border-blue-500 border-4'; // ans-on style
    }

    return 'bg-gray-200 text-gray-600 border-gray-300';
};

const handleGameComplete = async () => {
    if (!props.levelUuid || totalScore.value < 80) {
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
                message: `恭喜！您获得了 ${totalScore.value} 分，成功过关！`,
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