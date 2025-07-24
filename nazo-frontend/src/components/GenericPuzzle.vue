<template>
    <div class="min-h-screen bg-gradient-game-dark flex items-center justify-center p-4 relative">
        <div class="glass-card max-w-2xl w-full">


            <!-- 加载状态 -->
            <div v-if="isLoading" class="text-center text-white">
                <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-white mx-auto mb-4"></div>
                <p>加载谜题中...</p>
            </div>

            <!-- 错误状态 -->
            <div v-else-if="errorMessage" class="text-center text-red-400">
                <p>{{ errorMessage }}</p>
                <button @click="loadPuzzle"
                    class="mt-4 px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-md">
                    重试
                </button>
            </div>

            <!-- 谜题内容 -->
            <div v-else class="space-y-6">
                <!-- 谜面展示区域 -->
                <div class="bg-white/10 backdrop-blur-sm rounded-lg p-6">
                    <!-- 如果是图片URL，则显示图片 -->
                    <div v-if="isImageUrl(puzzleContent)" class="text-center">
                        <img :src="puzzleContent" alt="谜题图片" class="max-w-full h-auto mx-auto rounded-lg shadow-lg"
                            style="max-height: 400px;" />
                    </div>
                    <!-- 对于PUZZLE_1显示纯文本（不解析HTML实体），其他显示HTML内容 -->
                    <div v-else-if="shouldDisplayAsPlainText()" class="text-gray-200 text-lg leading-relaxed">{{
                        puzzleContent }}</div>
                    <!-- 否则显示HTML内容 -->
                    <div v-else class="text-gray-200 text-lg leading-relaxed" v-html="puzzleContent"></div>
                </div>

                <!-- 输入区域 -->
                <div class="space-y-4">
                    <input v-model="userAnswer" type="text"
                        class="w-full px-4 py-3 bg-white/10 backdrop-blur-sm border border-white/20 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-purple-400 focus:ring-1 focus:ring-purple-400"
                        placeholder="" @keyup.enter="submitAnswer" :disabled="isSubmitting" />
                </div>

                <!-- 提交按钮 -->
                <button @click="submitAnswer" :disabled="!userAnswer.trim() || isSubmitting"
                    class="w-full py-4 bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 disabled:from-gray-500 disabled:to-gray-600 text-white font-semibold rounded-lg transition-all duration-200 transform hover:scale-105 disabled:hover:scale-100 disabled:cursor-not-allowed">
                    <span v-if="isSubmitting">提交中...</span>
                    <span v-else>提交</span>
                </button>

                <!-- 错误提示 -->
                <div v-if="submitError" class="text-center text-red-400">
                    <p>{{ submitError }}</p>
                </div>
            </div>
        </div>

        <!-- 彩蛋码 - 右下角透明文本 -->
        <div v-if="easterEggCode"
            class="fixed bottom-4 right-4 text-transparent   text-xs opacity-30 font-mono select-none pointer-events-none">
            {{ easterEggCode }}
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from "vue";
import { getLevel, getPuzzle, completePuzzle } from "@/services/api";
import type { LevelInfo } from "@/services/api";
import {
    PUZZLE_1_UUID,
    PUZZLE_2_UUID,
    PUZZLE_3_UUID,
    PUZZLE_4_UUID,
    PUZZLE_5_UUID
} from "@/constants/levels";

interface Props {
    levelUuid: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
    gameComplete: [
        data: { success: boolean; message: string; nextLevel?: string }
    ];
}>();

// 组件状态
const levelInfo = ref<LevelInfo | null>(null);
const puzzleContent = ref("");
const userAnswer = ref("");
const isLoading = ref(true);
const isSubmitting = ref(false);
const errorMessage = ref("");
const submitError = ref("");

// 彩蛋码映射
const easterEggCodes = {
    [PUZZLE_1_UUID]: "Y2hpY2tlbi1lYXN0ZXItZWda",
    [PUZZLE_2_UUID]: "dHJvbGxmYWNlLWVhc3Rlci1ld2c",
    [PUZZLE_3_UUID]: "ZHVjay1lYXN0ZXItZWdnw2vc",
    [PUZZLE_4_UUID]: "bGlmZS1lYXN0ZXItZWdnMTIf",
    [PUZZLE_5_UUID]: "YnJhaW5mdWNrLWVhc3Rlci1la2dhc2Q"
};

// 计算当前谜题的彩蛋码
const easterEggCode = computed(() => {
    return easterEggCodes[props.levelUuid as keyof typeof easterEggCodes] || null;
});

// 加载关卡信息
const loadLevelInfo = async () => {
    try {
        const response = await getLevel(props.levelUuid);
        if (response.success) {
            levelInfo.value = response.level;
        } else {
            errorMessage.value = "无法加载关卡信息";
        }
    } catch (error) {
        console.error("Failed to load level info:", error);
        errorMessage.value = "网络错误，请检查连接";
    }
};

// 加载谜题内容
const loadPuzzle = async () => {
    try {
        isLoading.value = true;
        errorMessage.value = "";

        // 并行加载关卡信息和谜题内容
        await Promise.all([
            loadLevelInfo(),
            loadPuzzleContent()
        ]);
    } catch (error) {
        console.error("Failed to load puzzle:", error);
        errorMessage.value = "加载失败，请重试";
    } finally {
        isLoading.value = false;
    }
};

// 加载谜题内容
const loadPuzzleContent = async () => {
    try {
        const response = await getPuzzle(props.levelUuid);
        if (response.success) {
            puzzleContent.value = response.puzzle || "暂无谜面内容";
        } else {
            errorMessage.value = "无法加载谜题内容";
        }
    } catch (error) {
        console.error("Failed to load puzzle content:", error);
        errorMessage.value = "网络错误，请检查连接";
    }
};

// 检查是否为图片URL
const isImageUrl = (url: string): boolean => {
    if (!url) return false;
    // 检查是否以http/https开头且包含图片文件扩展名或者是已知的图片托管服务
    const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg'];
    const lowerUrl = url.toLowerCase();

    // 检查是否包含图片扩展名
    const hasImageExtension = imageExtensions.some(ext => lowerUrl.includes(ext));

    // 检查是否是HTTP/HTTPS URL
    // const isHttpUrl = lowerUrl.startsWith('http://') || lowerUrl.startsWith('https://');

    return hasImageExtension;
};

// 检查是否应该显示为纯文本（不解析HTML实体）
const shouldDisplayAsPlainText = (): boolean => {
    return props.levelUuid === PUZZLE_1_UUID;
};

// 提交答案
const submitAnswer = async () => {
    if (!userAnswer.value.trim() || isSubmitting.value) return;

    isSubmitting.value = true;
    submitError.value = "";

    try {
        const response = await completePuzzle(props.levelUuid, userAnswer.value.trim());

        if (response.success) {
            // 通关成功
            emit("gameComplete", {
                success: true,
                message: response.message || "恭喜通关！",
                nextLevel: response.nextLevel,
            });
        } else {
            // 答案错误
            submitError.value = response.message || "答案错误，请重试";
            // 清空输入框，允许重新输入
            userAnswer.value = "";
        }
    } catch (error) {
        console.error("Failed to submit answer:", error);
        submitError.value = "网络错误，请重试";
    } finally {
        isSubmitting.value = false;
    }
};

// 组件挂载时加载谜题
onMounted(() => {
    loadPuzzle();
});

// 监听 levelUuid 变化，重新加载谜题
watch(
    () => props.levelUuid,
    () => {
        // 重置组件状态
        userAnswer.value = "";
        submitError.value = "";
        // 重新加载谜题
        loadPuzzle();
    },
    { immediate: false } // 不需要立即执行，因为 onMounted 已经执行了初始加载
);
</script>

<style scoped>
/* 使用现有的全局样式类 */
</style>