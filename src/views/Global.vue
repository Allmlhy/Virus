<template>
  <div class="international">
    <NavBar />
    <div class="container">
      <h2>国际疫情指标</h2>
      <DateAndStats />

      <!-- 集成按钮和视图的容器 -->
      <div class="view-container">
        <!-- 切换按钮 -->
        <div class="toggle-buttons">
          <button :class="{ active: viewMode === 'map' }" @click="viewMode = 'map'">地图视图</button>
          <button :class="{ active: viewMode === 'globe' }" @click="viewMode = 'globe'">地球仪视图</button>
        </div>

        <!-- 视图内容 -->
        <div class="main-content">
          <div class="map-globe-area">
            <div v-if="viewMode === 'map'" class="map-container">
              <AMapCountryMap @countryClick="handleCountryClick" />
            </div>
            <div v-else class="globe-container">
              <GlobePopulation />
            </div>
          </div>
          <div class="chart-area">
            <BarChart />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import NavBar from "@/components/NavBar.vue";
import AMapCountryMap from "@/components/AMapCountryMap_TopBar.vue";
import DateAndStats from "@/components/DateAndStats.vue";
import BarChart from "@/components/BarChart.vue";
import GlobePopulation from "@/components/GlobePopulation.vue";

const router = useRouter();
const viewMode = ref('map'); // 默认显示地图

function handleCountryClick(countryName) {
  console.log("点击国家:", countryName);
  const countryNameForPath = encodeURIComponent(countryName.replace(/\s+/g, ""));
  router.push(`/country/${countryNameForPath}`).catch(() => { });
}
</script>

<style scoped>
/* 主要容器样式 */
.view-container {
  background: var(--color-background);
  border-radius: 16px;
  padding: 24px;
  margin-top: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

/* 主内容布局 */
.main-content {
  display: flex;
  gap: 24px;
  height: 75vh;
  min-height: 600px;
}

/* 左侧容器（地图/地球仪） */
.map-globe-area {
  flex: 3;
  /* 左侧占比3份 */
  display: flex;
  flex-direction: column;
  background: var(--color-background-soft);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 右侧图表容器 */
.chart-area {
  flex: 1;
  /* 右侧占比1份 */
  background: var(--color-background-soft);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 地图/地球仪容器 */
.map-container,
.globe-container {
  flex: 1;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  background: #ffffff;
}

/* 按钮样式优化 */
.toggle-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.toggle-buttons button {
  padding: 10px 28px;
  border: 2px solid var(--color-primary);
  border-radius: 30px;
  background: transparent;
  color: var(--color-primary);
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.toggle-buttons button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.15);
}

.toggle-buttons button.active {
  background: var(--color-primary);
  color: white;
  box-shadow: 0 4px 16px rgba(var(--color-primary-rgb), 0.3);
}

/* 图表容器内样式 */
.chart-area ::v-deep .chart-card {
  height: 100%;
  background: transparent;
  box-shadow: none;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
    height: auto;
    min-height: unset;
  }

  .map-globe-area,
  .chart-area {
    height: 60vh;
    min-height: 400px;
  }

  .toggle-buttons {
    flex-wrap: wrap;
    justify-content: center;
  }

  .toggle-buttons button {
    flex: 1;
    min-width: 120px;
    text-align: center;
  }
}

/* 全局微调 */
h2 {
  color: var(--color-heading);
  font-size: 2.2rem;
  margin-bottom: 1.5rem;
  text-align: center;
  position: relative;
  padding-bottom: 0.5rem;
}

h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: var(--color-primary);
  border-radius: 2px;
}
</style>