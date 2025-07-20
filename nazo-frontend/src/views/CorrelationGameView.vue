<template>
  <CorrelationGame
    level-uuid="correlation-level-2"
    @game-complete="handleGameComplete"
  />
</template>

<script setup lang="ts">
import CorrelationGame from "@/components/CorrelationGame.vue";
import { useRouter } from "vue-router";

const router = useRouter();

// 处理游戏完成事件
const handleGameComplete = (data: {
  success: boolean;
  message: string;
  nextLevel?: string;
}) => {
  if (data.success && data.nextLevel) {
    // 跳转到下一关
    router.push(`/level/${data.nextLevel}`);
  } else {
    // 如果没有下一关或失败，可以跳转到登录页或其他页面
    console.log(data.message);
  }
};
</script>
