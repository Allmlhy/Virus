<template>
  <div class="domestic">
    <div class="navbar-wrapper">
      <NavBar />
    </div>

    <!-- 标题与卡片整体绑定并水平居中 -->
    <div class="header-summary-wrapper">
      <h2 class="summary-title">今日国内疫情指标速递</h2>
      <div class="summary-container">
        <div class="summary-box">
          <p class="compare">较昨日 <span class="up">+30</span></p>
          <p class="number red">125073</p>
          <p class="label">累计确诊</p>
        </div>
        <div class="summary-box">
          <p class="compare">较昨日 <span class="up">+25</span></p>
          <p class="number orange">9303</p>
          <p class="label">现存疑似</p>
        </div>
        <div class="summary-box">
          <p class="compare">较昨日 <span class="same">0</span></p>
          <p class="number darkblue">800</p>
          <p class="label">累计死亡数</p>
        </div>
        <div class="summary-box">
          <p class="compare">较昨日 <span class="up">+20</span></p>
          <p class="number green">2500</p>
          <p class="label">累计治愈数</p>
        </div>
      </div>
    </div>

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

<!--    &lt;!&ndash; 汇总表 &ndash;&gt;-->
<!--    <TodaySummary />-->
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
  padding: 30px;
}

.domestic {
  background-color: #fff;
}

.header-summary-wrapper {
  display: flex;
  justify-content: center;      /* 整体水平居中 */
  align-items: center;          /* 垂直居中（对齐卡片高度） */
  padding: 20px 24px 0;
  gap: 24px;                    /* 标题与卡片间距 */
  flex-wrap: nowrap;            /* 不换行，保持一排 */
}

.summary-title {
  font-size: 24px;
  font-weight: bold;
  color: #222;
  margin: 0;
  white-space: nowrap;
  /* 去掉 padding-top，改为垂直居中 */
  line-height: 1;               /* 减少行高，方便对齐 */
  display: flex;
  align-items: center;          /* 文字垂直居中 */
}


.summary-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.summary-box {
  width: 130px;
  padding: 8px 6px;
  text-align: center;
  background: rgba(250, 250, 250, 0.92);
  border: 1px solid #ddd;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(2px);
  transition: box-shadow 0.3s ease;
}

.compare {
  font-size: 12px;
  color: #888;
  margin-bottom: 4px;
}

.compare .up {
  color: #c9302c;
}

.compare .down {
  color: #5bc0de;
}

.compare .same {
  color: #999;
}

.number {
  font-size: 18px;
  font-weight: bold;
  margin: 2px 0;
}

.label {
  font-size: 13px;
  color: #333;
}

.red { color: #c9302c; }
.orange { color: #f0ad4e; }
.blue { color: #5bc0de; }
.darkblue { color: #337ab7; }
.green { color: #5cb85c; }

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