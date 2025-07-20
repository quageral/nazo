import { createRouter, createWebHistory } from "vue-router";
import LoginView from "@/views/LoginView.vue";
import LevelView from "@/views/LevelView.vue";
import ErrorView from "@/views/ErrorView.vue";
import CorrelationGameView from "@/views/CorrelationGameView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      redirect: "/login",
    },
    {
      path: "/login",
      name: "Login",
      component: LoginView,
    },
    {
      path: "/level/:uuid",
      name: "Level",
      component: () => import("@/views/LevelView.vue"),
      props: true,
    },
    {
      path: "/correlation-game",
      name: "CorrelationGame",
      component: CorrelationGameView,
    },
    {
      path: "/error",
      name: "Error",
      component: ErrorView,
    },
    {
      path: "/:pathMatch(.*)*",
      redirect: "/error",
    },
  ],
});

// 路由守卫 - 检查登录状态
router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem("nazo_token");

  console.log("路由守卫执行:", {
    to: to.path,
    from: from.path,
    isLoggedIn: !!isLoggedIn,
  });

  if (to.name !== "Login" && !isLoggedIn) {
    // 未登录用户重定向到登录页
    console.log("未登录用户重定向到登录页");
    next({ name: "Login" });
  } else if (to.name === "Login" && isLoggedIn) {
    // fixme: 已登录用户访问登录页时重定向到第一关
    console.log("已登录用户重定向到第一关");
    next("/level/tetris-level-1");
  } else {
    console.log("正常通过路由守卫");
    next();
  }
});

export default router;
