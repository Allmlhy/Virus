<template>
  <div class="domestic">
    <div class="navbar-wrapper">
      <NavBar />
    </div>

    <!-- 标题与描述 -->
    <h2>国内疫情指标</h2>
    <p class="description">这里显示国内疫情的相关数据。</p>

    <!-- 顶部布局：时间选择、地图、表格 -->
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

    <!-- 数据指标 -->
    <div class="data-container">
      <div class="data-box"><h3>国内每日死亡数</h3><p>模拟数据：800</p></div>
      <div class="data-box"><h3>国内每日确诊数</h3><p>模拟数据：4000</p></div>
      <div class="data-box"><h3>国内总接种数量</h3><p>模拟数据：7000</p></div>
      <div class="data-box"><h3>国内每日治愈数</h3><p>模拟数据：2500</p></div>
    </div>

    <!-- 饼图 -->
    <PieChart v-model:queryParams="queryParams" style="margin-top: 40px;" />

    <!-- 柱状图组合 -->
    <div class="charts-container">
      <BarChart :queryParams="queryParams" style="flex: 1;" />
      <MonthlyCityCovidBarChart :queryParams="queryParams" style="flex: 1;" />
    </div>

    <!-- 省份对比图 -->
    <ProvincePK :queryParams="queryParams" style="margin-top: 40px;" />

    <!-- 汇总表 -->
    <TodaySummary />
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'

import NavBar from "@/components/NavBar.vue";
import TimePicker from '@/components/TimePicker/TimePicker.vue';
import ChinaCovidMap from '@/components/Map/ChinaCovidMap.vue';
import ProvinceTable from '@/components/Table/ProvinceTable.vue';
import TodaySummary from '@/components/Table/TodaySummary.vue';
import PieChart from "@/components/PieChart.vue";
import BarChart from "@/components/DailyStatsBarChart.vue";
import MonthlyCityCovidBarChart from "@/components/MonthlyCityCovidBarChart.vue";
import ProvincePK from "@/components/ProvincePK.vue";

import { fetchProvinceStats } from '@/apis/covid.js';

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
  padding: 40px;
}
.domestic {
  background-color: #f5f7fa;
}

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
  box-shadow: 0 1px 6px rgb(0 0 0 / 0.1);
}

.right-panel {
  width: 380px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgb(0 0 0 / 0.1);
}

/* 页面标题与描述 */
h2 {
  text-align: center;
  color: #222;
  font-size: 32px;
  font-weight: bold;
  margin-top: 24px;
}

.description {
  text-align: center;
  color: #555;
  font-size: 16px;
  margin-bottom: 20px;
}

/* 数据指标 */
.data-container {
  display: flex;
  gap: 20px;
  justify-content: space-around;
  margin-top: 20px;
  padding: 0 24px;
}

.data-box {
  background: #f8f8f8;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  width: 220px;
}

/* 图表区域 */
.charts-container {
  display: flex;
  gap: 20px;
  justify-content: space-between;
  margin: 40px 24px 0;
}

.loading {
  text-align: center;
  color: #888;
}
</style>
