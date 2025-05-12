<template>
  <nav class="navbar">
    <div class="logo">
      <img src="@/assets/logo.svg" alt="Logo" />
      <span>疫情监测系统</span>
    </div>
    <ul class="nav-links">
      <li>
        <router-link to="/" :class="{ active: isActive('/') }">首页</router-link>
      </li>
      <li>
        <router-link to="/global" :class="{ active: isActive('/global') }">国际</router-link>
      </li>
      <li>
        <router-link to="/domestic" :class="{ active: isActive('/domestic') }">国内</router-link>
      </li>
      <li>
        <router-link to="/search" :class="{ active: isActive('/search') }">检索</router-link>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
const isActive = (path) => route.path === path
</script>

<style scoped>
/* 使 Navbar 固定在顶部 */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  background-color: #f8f9fa;
  padding: 0 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

/* 保证页面主体内容不会被遮挡 */
body {
  padding-top: 60px; /* 给页面顶部加上与导航栏相同的高度 */
}

.logo {
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 18px;
  height: 100%;
}

.logo img {
  width: 30px;
  height: 30px;
  margin-right: 10px;
}

.nav-links {
  display: flex;
  align-items: center;
  height: 100%;
  margin: 0;
  padding: 0;
  list-style: none;
}

.nav-links li {
  margin-left: 20px;
  height: 100%;
  display: flex;
  align-items: center;
}

.nav-links a {
  text-decoration: none;
  color: #333;
  padding: 8px 14px;
  /* 设置四个角的圆角 */
  border-radius: 20px; /* 增大圆角效果 */
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  height: 100%;
  position: relative;
  background-color: transparent;
  box-sizing: border-box; /* 确保 padding 不影响宽高 */
}

.nav-links a:hover {
  background-color: #ddd;
}

.nav-links a.active {
  color: #fff;
}

/* 书签激活样式颜色 */
.nav-links a.active[href="/"] {
  background-color: #2e7d32;
}

.nav-links a.active[href="/global"] {
  background-color: #b71c1c;
}

.nav-links a.active[href="/domestic"] {
  background-color: #0d47a1;
}

.nav-links a.active[href="/search"] {
  background-color: #4a148c;
}

/* 伪元素：下凸圆角，模拟书签标签 */
.nav-links a.active::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 12px;
  height: 12px;
  background-color: inherit;
  border-radius: 50%;
}

/* 防止点击时尺寸变化，保持尺寸一致 */
.nav-links a.active {
  transform: none;
  box-shadow: none;
}
</style>
