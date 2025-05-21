<template>
  <div class="international">
    <NavBar />
    <div class="container">
      <h2>国际疫情指标</h2>
      <DateAndStats />
      <!-- 使用一个容器包裹地图和柱形图 -->
      <div class="map-chart-wrapper">
        <div class="map-container">
          <AMapCountryMap @countryClick="handleCountryClick" />
        </div>
        <div class="bar-chart-container">
          <BarChart />
        </div>
      </div>
    </div>
    <div class="globe-container">
      <GlobePopulation />
    </div>
    <GlobalCovidStatsPie />
    <RadarChart />
  </div>


</template>

<script setup>
import NavBar from "@/components/NavBar.vue";
import AMapCountryMap from "@/components/AMapCountryMap_TopBar.vue";
import DateAndStats from "@/components/DateAndStats.vue";
import BarChart from "@/components/BarChart.vue";
import { useRouter } from "vue-router";
import GlobePopulation from "@/components/GlobePopulation.vue";
import GlobalCovidStatsPie from "@/components/GlobalCovidStatsPie.vue";
import RadarChart from '@/components/RadarChart.vue';
const router = useRouter();

// 地图点击事件处理
function handleCountryClick(countryName) {
  console.log("点击国家:", countryName);
  const countryNameForPath = encodeURIComponent(countryName.replace(/\s+/g, ""));

  // 用路由跳转，假设路由规则 /country/:name
  router.push(`/country/${countryNameForPath}`).catch(() => {});
}
</script>

<style scoped>
.globe-container {
  padding: 32px;
  margin: 40px auto;
  max-width: 1000px;
  background: #f0f4f8;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}
.container {
  margin: 20px;
}

h2 {
  text-align: center;
}

.map-chart-wrapper {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  width: 100%;
  gap: 20px; /* 增加地图和图表之间的间距 */
  border-radius: 10px; /* 圆角效果 */
  border: 1px solid var(--color-border); /* 边框 */
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); /* 阴影效果 */
  background-color: var(--color-background-soft); /* 背景颜色 */
  padding: 20px;
  height: 1200px;
}

.map-container {
  flex: 1; /* 地图占据可用空间 */
  height: 100%; /* 固定地图高度 */
}

.data-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.data-box {
  background-color: #fff;
  border: 1px solid #ddd;
  padding: 10px;
  text-align: center;
  border-radius: 8px;
}

.data-box h3 {
  margin: 0;
  color: #333;
}

.data-box p {
  margin: 10px 0 0;
  color: #555;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .map-chart-wrapper {
    flex-direction: column; /* 小屏设备下地图和图表堆叠显示 */
    gap: 10px; /* 更小的间距 */
  }

  .map-container {
    flex: 0 0 100%; /* 地图占满100%宽度 */
  }

  .bar-chart-container {
    flex: 0 0 100%; /* 图表占满100%宽度 */
    height: 300px; /* 调整图表高度 */
  }
}
</style>