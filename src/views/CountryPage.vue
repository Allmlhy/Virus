<template>
  <div class="page-container">
    <!-- 替换标题为国家卡片组件 -->
    <CountryCard :code="countryCode" />

    <CountryStats :country="countryName" />

    <!-- 图表容器 -->
    <div class="charts-container">
      <div class="chart-card">
        <global-covid-stats-pie :country="countryName" />
      </div>
      <div class="chart-card">
        <RadarChart :country="countryName" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import CountryStats from '@/components/CountryStats.vue'
import GlobalCovidStatsPie from "@/components/GlobalCovidStatsPie.vue"
import RadarChart from '@/components/RadarChart.vue'
import CountryCard from '@/components/CountryCard/CountryCard.vue'

const route = useRoute()
const countryName = route.params.name

const codeMap = {
  china: 'CN',
  america: 'US',
  // 继续补充映射
}

const countryCode = computed(() => {
  return codeMap[countryName?.toLowerCase()]
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  font-family: "Segoe UI", "Microsoft YaHei", sans-serif;
  background-color: #f7f9fb;
}

/* 原有 title 已去除 */
.charts-container {
  display: flex;
  height: 80%;
  justify-content: space-between;
  gap: 24px;
  margin-top: 20px;
}

.chart-card {
  flex: 1;
  max-width: 50%;
  background: white;
  height: 700px;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
  padding: 20px;
  box-sizing: border-box;
  transition: transform 0.3s ease;
}

.chart-card:hover {
  transform: translateY(-5px);
}

.chart-card :deep(canvas),
.chart-card :deep(svg) {
  width: 100% !important;
  height: auto !important;
}
</style>
