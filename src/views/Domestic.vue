<script setup>
import { ref, watch } from 'vue'
import NavBar from "@/components/NavBar.vue";
import TimePicker from '@/components/TimePicker/TimePicker.vue';
import ChinaCovidMap from '@/components/Map/ChinaCovidMap.vue';
import ProvinceTable from '@/components/Table/ProvinceTable.vue';  // 新增表格组件
import { fetchProvinceStats } from '@/apis/covid.js';
import TodaySummary from '@/components/Table/TodaySummary.vue';
const selectedTime = ref({
  year: 'all',
  month: 'all',
  day: 'all'
});

const covidData = ref(null);
const initialData = ref(null);  // 新增固定数据

const fetchData = async (params = {}) => {
  try {
    const data1 = await fetchProvinceStats(params);

    covidData.value = data1;
    if (!initialData.value) {
      // 只赋值一次
      initialData.value = JSON.parse(JSON.stringify(data1));
    }
    console.log("请求数据1：", data1)
  } catch (error) {
    console.error('请求疫情数据失败:', error);
  }
};

fetchData();

watch(() => selectedTime.value.day, (newDay) => {
  fetchData(selectedTime.value);
});

const handleTimeChange = (time) => {
  selectedTime.value = time;
};
</script>

<template>
  <div class="domestic">
    <NavBar />
    <h2>国内疫情指标</h2>
    <p>这里显示国内疫情的相关数据。</p>
    <div class="container layout">
      <div class="left-panel">

        <TimePicker class="time-picker" @time-change="handleTimeChange" />
        <ChinaCovidMap :rawData="covidData" />

      </div>
      <div class="right-panel">
        <h3>📊 省份疫情数据</h3>
        <ProvinceTable :provinceData="initialData" />
      </div>
    </div>
    <TodaySummary />

  </div>
</template>

<style scoped>
.container.layout {
  display: flex;
  gap: 24px;
  height: calc(100vh - 60px);
  padding: 16px 24px;
  box-sizing: border-box;
  background-color: #f9fafb;
  /* 轻微灰白背景，避免纯白过刺眼 */
}

.left-panel {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-sizing: border-box;
  box-shadow: 0 2px 8px rgb(0 0 0 / 0.05);
  overflow: hidden;
}

.time-picker {
  margin-bottom: 12px;
}

.left-panel>ChinaCovidMap {
  flex-grow: 1;
  /* 地图撑满剩余高度 */
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgb(0 0 0 / 0.1);
}


.right-panel {
  /* 去掉 overflow-y */
  width: 380px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-sizing: border-box;
  box-shadow: 0 2px 12px rgb(0 0 0 / 0.1);
  /* 不加 overflow */
  overflow: hidden;
}

.right-panel::-webkit-scrollbar {
  width: 8px;
}

.right-panel::-webkit-scrollbar-track {
  background: transparent;
}

.right-panel::-webkit-scrollbar-thumb {
  background-color: #94a3b8;
  border-radius: 4px;
  border: 2px solid transparent;
  background-clip: content-box;
}

h2 {
  margin: 16px 24px 8px;
  font-weight: 700;
  font-size: 28px;
  color: #222;
}

p {
  margin: 0 24px 24px;
  color: #555;
  font-size: 16px;
  line-height: 1.5;
  user-select: none;
}
</style>
