<template>
  <div>
    <!-- 加载动画遮罩 -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="spinner"></div>
    </div>

    <!-- 页面主体，跳转时隐藏 -->
    <div v-show="!isLoading" class="page-content">
      <nav class="navbar">
        <div class="logo">
          <img src="@/assets/logo.svg" alt="Logo" />
          <span>疫情监测系统</span>
        </div>
        <ul class="nav-links">
          <li>
            <a
                href="/"
                :class="{ active: isActive('/') }"
                @click.prevent="refresh('/')"
            >首页</a>
          </li>
          <li>
            <a
                href="/global"
                :class="{ active: isActive('/global') }"
                @click.prevent="refresh('/global')"
            >国际</a>
          </li>
          <li>
            <a
                href="/domestic"
                :class="{ active: isActive('/domestic') }"
                @click.prevent="refresh('/domestic')"
            >国内</a>
          </li>
          <li>
            <a
                href="/search"
                :class="{ active: isActive('/search') }"
                @click.prevent="refresh('/search')"
            >检索</a>
          </li>
        </ul>
      </nav>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isActive = (path) => route.path === path

const isLoading = ref(false)

const refresh = (path) => {
  isLoading.value = true
  setTimeout(() => {
    window.location.href = path
  }, 300) // 加载动画持续时间
}
</script>

<style scoped>


/* 页面内容隐藏时显示全屏加载层 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.8);
  z-index: 9999;

  display: flex;
  justify-content: center;
  align-items: center;
}

/* 旋转动画的圆圈 */
.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #999;
  border-top-color: #0d47a1; /* 深蓝色 */
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 旋转关键帧 */
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 保持你的原样式不变 */
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

body {
  padding-top: 60px;
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
  border-radius: 20px;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  height: 100%;
  position: relative;
  background-color: transparent;
  box-sizing: border-box;
}

.nav-links a:hover {
  background-color: #ddd;
}

.nav-links a.active {
  color: #fff;
}

.nav-links a.active[href='/'] {
  background-color: #2e7d32;
}

.nav-links a.active[href='/global'] {
  background-color: #b71c1c;
}

.nav-links a.active[href='/domestic'] {
  background-color: #0d47a1;
}

.nav-links a.active[href='/search'] {
  background-color: #4a148c;
}

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

.nav-links a.active {
  transform: none;
  box-shadow: none;
}
</style>
