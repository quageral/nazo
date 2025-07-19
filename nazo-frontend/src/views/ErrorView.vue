<template>
  <div class="min-h-screen bg-gradient-game-dark flex items-center justify-center p-8">
    <div class="w-full max-w-2xl">
      <div class="game-card text-center">
        <!-- 错误图标 -->
        <div class="text-9xl mb-8 animate-float">🚫</div>
        
        <!-- 错误标题 -->
        <h1 class="text-6xl font-black text-white mb-6 text-shadow-lg">
          404
        </h1>
        <h2 class="text-3xl font-bold text-red-400 mb-8">
          关卡不存在
        </h2>
        
        <!-- 错误描述 -->
        <div class="glass-card p-8 mb-8">
          <p class="text-xl text-gray-300 leading-relaxed">
            🔍 抱歉，您访问的关卡不存在或已失效<br>
            🔗 请检查关卡链接是否正确<br>
            🎮 或者重新登录开始游戏
          </p>
        </div>
        
        <!-- 操作按钮 -->
        <div class="space-y-4">
          <button 
            @click="goToLogin"
            class="w-full game-button bg-gradient-game text-white flex items-center justify-center space-x-3"
          >
            <span class="text-xl">🏠</span>
            <span>返回登录页</span>
          </button>
          <button 
            @click="goBack"
            class="w-full game-button bg-gradient-to-r from-gray-600 to-gray-700 text-white flex items-center justify-center space-x-3"
          >
            <span class="text-xl">↩️</span>
            <span>返回上一页</span>
          </button>
        </div>
        
        <!-- 提示信息 -->
        <div class="mt-10 p-6 bg-blue-500/10 border-2 border-blue-500/30 rounded-xl backdrop-blur-sm">
          <h3 class="text-lg font-semibold text-blue-300 mb-3 flex items-center justify-center space-x-2">
            <span>💡</span>
            <span>提示</span>
          </h3>
          <p class="text-gray-300">
            如果您是通过正确的关卡链接访问的，请尝试重新登录或联系管理员
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goToLogin = () => {
  localStorage.removeItem('nazo_token')
  localStorage.removeItem('nazo_user')
  router.push('/login')
}

const goBack = () => {
  router.go(-1)
}
</script> 