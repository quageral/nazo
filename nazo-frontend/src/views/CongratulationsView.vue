<template>
    <div class="min-h-screen bg-gradient-to-br from-purple-900 via-blue-900 to-purple-800 relative overflow-hidden">
        <!-- 蛋糕雨效果 -->
        <div class="absolute inset-0 pointer-events-none">
            <div v-for="cake in cakeRain" :key="cake.id" class="absolute animate-bounce" :style="{
                left: cake.x + '%',
                animationDuration: cake.duration + 's',
                animationDelay: cake.delay + 's',
                fontSize: cake.size + 'px',
                top: '-50px'
            }" :class="'cake-fall-' + cake.id">
                {{ cake.emoji }}
            </div>
        </div>

        <!-- 烟花背景效果 -->
        <div class="fireworks-container absolute inset-0 pointer-events-none">
            <div v-for="firework in fireworks" :key="firework.id" class="firework-burst"
                :style="{ left: firework.x + '%', top: firework.y + '%' }">
                <div v-for="particle in firework.particles" :key="particle.id" class="firework-particle" :style="{
                    backgroundColor: particle.color,
                    '--dx': particle.dx + 'px',
                    '--dy': particle.dy + 'px',
                    animationDelay: particle.delay + 's',
                }"></div>
            </div>
        </div>

        <!-- 主要内容区域 -->
        <div class="relative z-10 flex flex-col items-center justify-center min-h-screen px-4 text-center">
            <!-- 庆祝表情 -->
            <div class="text-8xl md:text-9xl mb-8 animate-bounce">🎉</div>

            <!-- 主标题 -->
            <h1
                class="text-6xl md:text-8xl lg:text-9xl font-black text-transparent bg-clip-text bg-gradient-to-r from-yellow-400 via-pink-500 to-purple-600 mb-4 animate-pulse">
                生日快乐！
            </h1>

            <!-- 副标题 -->
            <h2 class="text-4xl md:text-6xl lg:text-7xl font-bold text-white mb-8 animate-pulse"
                style="animation-delay: 0.5s;">
                恭喜通关！
            </h2>

            <!-- 装饰性星星 -->
            <div class="flex justify-center space-x-4 mb-12">
                <span class="text-4xl md:text-6xl animate-spin" style="animation-duration: 3s;">⭐</span>
                <span class="text-4xl md:text-6xl animate-spin"
                    style="animation-duration: 2s; animation-delay: 0.5s;">🌟</span>
                <span class="text-4xl md:text-6xl animate-spin"
                    style="animation-duration: 4s; animation-delay: 1s;">✨</span>
                <span class="text-4xl md:text-6xl animate-spin"
                    style="animation-duration: 2.5s; animation-delay: 1.5s;">💫</span>
            </div>

            <!-- 庆祝消息 -->
            <div class="bg-white/10 backdrop-blur-md rounded-2xl p-8 mb-12 max-w-2xl mx-auto">
                <p class="text-xl md:text-2xl text-white font-medium leading-relaxed">
                    🎂 已成功完成所有关卡！<br>
                    🎈 感谢你的精彩表现！<br>
                    🎁 现在是时候探索隐藏的秘密了...
                </p>
            </div>

            <!-- 彩蛋解密按钮 -->
            <button @click="goToEaster"
                class="group relative px-8 py-4 bg-gradient-to-r from-pink-500 to-purple-600 hover:from-pink-600 hover:to-purple-700 text-white text-xl md:text-2xl font-bold rounded-full shadow-2xl transform hover:scale-105 transition-all duration-300 animate-pulse">
                <span class="relative z-10">🔍 探索彩蛋解密</span>
                <div
                    class="absolute inset-0 bg-gradient-to-r from-yellow-400 to-orange-500 rounded-full opacity-0 group-hover:opacity-20 transition-opacity duration-300">
                </div>
            </button>
        </div>

        <!-- 底部装饰 -->
        <div class="absolute bottom-0 left-0 w-full h-32 bg-gradient-to-t from-black/20 to-transparent"></div>

        <!-- 隐藏消息 -->
        <div class="absolute bottom-4 left-4 text-transparent text-xs opacity-5 pointer-events-none select-none">
            将第24个符号合在一起加上50，就能得到新彩蛋
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 蛋糕雨效果数据
const cakeRain = ref<Array<{
    id: number;
    x: number;
    duration: number;
    delay: number;
    size: number;
    emoji: string;
}>>([]);

// 烟花效果数据
const fireworks = ref<Array<{
    id: number;
    x: number;
    y: number;
    particles: Array<{
        id: number;
        color: string;
        dx: number;
        dy: number;
        delay: number;
    }>;
}>>([]);

// 蛋糕雨的表情符号
const cakeEmojis = ['🍰', '🧁', '🎂', '🍪', '🧁', '🎉', '🎊', '🎈', '🎁', '⭐'];

// 烟花颜色数组
const fireworkColors = [
    '#ff6b6b', '#4ecdc4', '#45b7d1', '#96ceb4', '#feca57',
    '#ff9ff3', '#54a0ff', '#5f27cd', '#00d2d3', '#ff9f43',
    '#10ac84', '#ee5a6f', '#c44569', '#f8b500', '#7bed9f'
];

// 生成蛋糕雨
const generateCakeRain = () => {
    const cakes = [];
    for (let i = 0; i < 50; i++) {
        cakes.push({
            id: i,
            x: Math.random() * 100,
            duration: 2 + Math.random() * 4, // 2-6秒
            delay: Math.random() * 10, // 0-10秒延迟
            size: 24 + Math.random() * 16, // 24-40px
            emoji: cakeEmojis[Math.floor(Math.random() * cakeEmojis.length)]
        });
    }
    cakeRain.value = cakes;
};

// 生成烟花效果
const generateFireworks = () => {
    const newFireworks = [];
    for (let i = 0; i < 20; i++) {
        const firework = {
            id: Date.now() + i,
            x: 10 + Math.random() * 80, // 10% 到 90% 的位置
            y: 10 + Math.random() * 60, // 10% 到 70% 的位置
            particles: [] as Array<{
                id: number;
                color: string;
                dx: number;
                dy: number;
                delay: number;
            }>
        };

        // 为每个烟花生成粒子
        for (let j = 0; j < 25; j++) {
            const angle = (j / 25) * Math.PI * 2;
            const velocity = 60 + Math.random() * 100;
            firework.particles.push({
                id: j,
                color: fireworkColors[Math.floor(Math.random() * fireworkColors.length)],
                dx: Math.cos(angle) * velocity,
                dy: Math.sin(angle) * velocity,
                delay: i * 0.2 + Math.random() * 0.6
            });
        }

        newFireworks.push(firework);
    }
    fireworks.value = newFireworks;

    // 8秒后清除烟花
    setTimeout(() => {
        fireworks.value = [];
    }, 8000);
};

// 前往彩蛋解密页面
const goToEaster = () => {
    router.push('/easter');
};

// 组件挂载时启动动效
onMounted(() => {
    generateCakeRain();
    generateFireworks();

    // 每隔15秒重新生成烟花
    setInterval(generateFireworks, 15000);
});
</script>

<style scoped>
/* 蛋糕雨动画 */
@keyframes cakeFall {
    0% {
        transform: translateY(-100vh) rotate(0deg);
        opacity: 1;
    }

    100% {
        transform: translateY(100vh) rotate(360deg);
        opacity: 0;
    }
}

.cake-fall-0,
.cake-fall-1,
.cake-fall-2,
.cake-fall-3,
.cake-fall-4,
.cake-fall-5,
.cake-fall-6,
.cake-fall-7,
.cake-fall-8,
.cake-fall-9,
.cake-fall-10,
.cake-fall-11,
.cake-fall-12,
.cake-fall-13,
.cake-fall-14,
.cake-fall-15,
.cake-fall-16,
.cake-fall-17,
.cake-fall-18,
.cake-fall-19,
.cake-fall-20,
.cake-fall-21,
.cake-fall-22,
.cake-fall-23,
.cake-fall-24,
.cake-fall-25,
.cake-fall-26,
.cake-fall-27,
.cake-fall-28,
.cake-fall-29,
.cake-fall-30,
.cake-fall-31,
.cake-fall-32,
.cake-fall-33,
.cake-fall-34,
.cake-fall-35,
.cake-fall-36,
.cake-fall-37,
.cake-fall-38,
.cake-fall-39,
.cake-fall-40,
.cake-fall-41,
.cake-fall-42,
.cake-fall-43,
.cake-fall-44,
.cake-fall-45,
.cake-fall-46,
.cake-fall-47,
.cake-fall-48,
.cake-fall-49 {
    animation: cakeFall linear infinite;
}

/* 烟花动画 */
.firework-particle {
    position: absolute;
    width: 4px;
    height: 4px;
    border-radius: 50%;
    animation: fireworkExplosion 2s ease-out forwards;
}

@keyframes fireworkExplosion {
    0% {
        transform: translate(0, 0);
        opacity: 1;
    }

    100% {
        transform: translate(var(--dx), var(--dy));
        opacity: 0;
    }
}

/* 文字渐变动画 */
@keyframes gradientShift {

    0%,
    100% {
        background-position: 0% 50%;
    }

    50% {
        background-position: 100% 50%;
    }
}

/* 增强的脉冲效果 */
@keyframes enhancedPulse {

    0%,
    100% {
        transform: scale(1);
        opacity: 1;
    }

    50% {
        transform: scale(1.05);
        opacity: 0.8;
    }
}

.animate-pulse {
    animation: enhancedPulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
</style>