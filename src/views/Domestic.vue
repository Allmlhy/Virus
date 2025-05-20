<template>
  <div class="domestic">
    <div class="navbar-wrapper">
      <NavBar />
    </div>

    <!-- 用TodaySummary组件替代疫情指标卡片 -->
    <DomesticSummary />

    <!-- 时间选择器、地图、表格 -->
    <div class="top-section layout">
      <div class="left-panel">
        <TimePicker class="time-picker" @time-change="handleTimeChange" />
        <div class="map-container" ref="chartContainer" style="height: 400px;">
          <p v-show="!isMapLoaded" class="loading">地图加载中...</p>
          <ChinaCovidMap v-if="isMapLoaded" :rawData="covidData" />
        </div>
      </div>

      <div class="right-panel">
        <h3>📊 省份疫情数据</h3>
        <ProvinceTable :provinceData="initialData" />
      </div>
    </div>

    <div class="charts-container">
      <BarChart :queryParams="queryParams" style="flex: 1;" />
      <MonthlyCityCovidBarChart :queryParams="queryParams" style="flex: 1;" />
    </div>

    <!-- 饼图区域 + 省份对比图并排展示 -->
    <div class="charts-container">
      <PieChart :queryParams="queryParams" style="flex: 1;"/>
      <ProvincePK :queryParams="queryParams" style="flex: 1;"/>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'

import NavBar from "@/components/NavBar.vue";
import TimePicker from '@/components/TimePicker/TimePicker.vue';
import ChinaCovidMap from '@/components/Map/ChinaCovidMap.vue';
import ProvinceTable from '@/components/Table/ProvinceTable.vue';
import TodaySummary from '@/components/DomesticSummary/DomesticSummary.vue';
import PieChart from "@/components/PieChart.vue";
import BarChart from "@/components/DailyStatsBarChart.vue";
import MonthlyCityCovidBarChart from "@/components/MonthlyCityCovidBarChart.vue";
import ProvincePK from "@/components/ProvincePK.vue";

import { fetchProvinceStats } from '@/apis/covid.js';
import DomesticSummary from "@/components/DomesticSummary/DomesticSummary.vue";

const isMapLoaded = ref(false);

const selectedTime = ref({
  year: 'all',
  month: 'all',
  day: 'all'
});

const covidData = ref(null);
const initialData = ref(null);

const fetchData = async (params = {}) => {
  try {
    const data1 = await fetchProvinceStats(params);
    covidData.value = data1;
    if (!initialData.value) {
      initialData.value = JSON.parse(JSON.stringify(data1));
    }
  } catch (error) {
    console.error('请求疫情数据失败:', error);
  }
};

fetchData();

watch(() => selectedTime.value.day, () => {
  fetchData(selectedTime.value);
});

const handleTimeChange = (time) => {
  selectedTime.value = time;
};

const queryParams = reactive({
  year: 2020,
  month: undefined,
  day: undefined,
  province: "全国",
  city: ""
});

onMounted(() => {
  isMapLoaded.value = true;
});
</script>

<style scoped>
.navbar-wrapper {
  padding: 30px;
}

.domestic {
  background-color: #fff;
}

/* 这部分疫情卡片相关样式已移除，改由TodaySummary组件自己管理 */

/* 顶部结构 */
.top-section.layout {
  display: flex;
  gap: 24px;
  padding: 16px 24px;
  box-sizing: border-box;
}

.left-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgb(0 0 0 / 0.05);
}

.time-picker {
  margin-bottom: 12px;
}

.map-container {
  flex-grow: 1;
  border-radius: 6px;
  overflow: hidden;
}

.right-panel {
  width: 380px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgb(0 0 0 / 0.1);
}

/* 图表区域 */

.charts-container {
  display: flex;
  gap: 20px;
  justify-content: space-between;
  margin: 40px 24px 0;
}
</style>
