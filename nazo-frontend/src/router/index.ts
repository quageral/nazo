import { createRouter, createWebHistory } from "vue-router";
import LoginView from "@/views/LoginView.vue";
import LevelView from "@/views/LevelView.vue";
import ErrorView from "@/views/ErrorView.vue";

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
      component: LevelView,
      props: true,
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

  if (to.name !== "Login" && !isLoggedIn) {
    // 未登录用户重定向到登录页
    next({ name: "Login" });
  } else if (to.name === "Login" && isLoggedIn) {
    // 已登录用户访问登录页时重定向到第一关
    next("/level/tetris-level-1");
  } else {
    next();
  }
});

export default router;
