<template>
  <div class="chart-container">
    <h2>某日各省疫情对比柱状图</h2>

    <!-- 筛选控件 -->
    <div class="filters">
      <label>
        年：<input type="number" v-model.number="year" min="2020" max="2025" />
      </label>
      <label>
        月：<input type="number" v-model.number="month" min="1" max="12" />
      </label>
      <label>
        日：<input type="number" v-model.number="day" min="1" max="31" />
      </label>
    </div>

    <!-- 图表区域 -->
    <div class="chart-wrapper">
      <div class="chart-box" v-if="chartData.length">
        <v-chart :option="chartOptions" style="width: 100%; height: 100%;" />
      </div>
    </div>

    <!-- 控制项：已居中显示在图表下方 -->
    <div class="custom-controls inline-controls">
      <div class="control-row">
        <label>Y轴最小：</label>
        <input type="number" v-model.number="yMin" min="0" />
      </div>
      <div class="control-row">
        <label>Y轴最大：</label>
        <input type="number" v-model.number="yMax" min="0" />
      </div>
      <div class="control-row">
        <label>省份数量：</label>
        <button @click="decrementCount" :disabled="displayCount <= 1">-</button>
        <button @click="incrementCount" :disabled="displayCount >= chartData.length">+</button>
      </div>
    </div>

    <!-- 状态提示 -->
    <div v-if="loading" class="status">加载中...</div>
    <div v-else-if="error" class="status error">{{ error }}</div>
    <div v-else-if="!chartData.length" class="status">暂无数据</div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import VChart from 'vue-echarts';
import { fetchProvincePK } from '@/apis/province_pk';

const year = ref(2020);
const month = ref(1);
const day = ref(20);
const yMin = ref(0);
const yMax = ref(100);
const displayCount = ref(7);
const loading = ref(false);
const error = ref(null);
const chartData = ref([]);

function isValidDate(y, m, d) {
  const date = new Date(y, m - 1, d);
  return date.getFullYear() === y && date.getMonth() + 1 === m && date.getDate() === d;
}

async function loadData() {
  if (!isValidDate(year.value, month.value, day.value)) {
    error.value = '请输入合法日期';
    chartData.value = [];
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const res = await fetchProvincePK({
      year: year.value,
      month: month.value,
      day: day.value,
      regionLevel: 'province',
    });

    if (!Array.isArray(res)) {
      error.value = '返回数据格式错误';
      chartData.value = [];
    } else {
      chartData.value = res;
    }
  } catch (err) {
    error.value = '数据加载失败，请检查网络或接口';
    chartData.value = [];
    console.error('数据请求失败:', err);
  } finally {
    loading.value = false;
  }
}

watch([year, month, day], loadData, { immediate: true });

function incrementCount() {
  if (displayCount.value < chartData.value.length) displayCount.value++;
}

function decrementCount() {
  if (displayCount.value > 1) displayCount.value--;
}

const chartOptions = computed(() => {
  if (!chartData.value.length) return {};

  const sortedData = [...chartData.value].sort((a, b) => (b.totalConfirmed || 0) - (a.totalConfirmed || 0));
  const topData = sortedData.slice(0, displayCount.value);
  const totalCount = chartData.value.length;
  const startPercent = 0;
  const endPercent = (displayCount.value / totalCount) * 100;

  return {
    backgroundColor: '#fff',
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['当前确诊', '累计确诊', '累计死亡'],
      top: 30,
      textStyle: { color: '#333' },
    },
    grid: {
      left: '10%',
      right: '4%',
      bottom: '18%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: topData.map(item => item.region),
      axisLabel: {
        rotate: 45,
        interval: 0,
        color: '#333',
      },
    },
    yAxis: {
      type: 'value',
      min: yMin.value,
      max: yMax.value,
      axisLabel: {color: '#333'},
      splitLine: {lineStyle: {color: '#ccc'}},
    },
    dataZoom: [
      {
        type: 'slider',
        show: true,
        xAxisIndex: 0,
        start: startPercent,
        end: endPercent,
        zoomLock: true,
        bottom: 50,
        height: 20,
        textStyle: {color: '#333'},
      },
      {
        type: 'inside',
        xAxisIndex: 0,
        start: startPercent,
        end: endPercent,
        zoomLock: true,
        throttle: 100,
        zoomRate: 0.1,
      },
    ],
    series: [
      {
        name: '当前确诊',
        type: 'bar',
        data: topData.map(item => item.currentConfirmed || 0),
        itemStyle: {color: '#F4A261'},
      },
      {
        name: '累计确诊',
        type: 'bar',
        data: topData.map(item => item.totalConfirmed || 0),
        itemStyle: {color: '#2A9D8F'},
      },
      {
        name: '累计死亡',
        type: 'bar',
        data: topData.map(item => item.totalDeaths || 0),
        itemStyle: {color: '#E76F51'},
      },
    ],
  };
});
</script>

<style scoped>
.chart-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  color: #333;
  height: 700px;
  box-sizing: border-box;
  border-radius: 12px;
  width: 60%;
  min-width: 300px;
}

h2 {
  text-align: center;
  margin-bottom: 15px;
  color: #333;
}

.filters {
  margin-bottom: 10px;
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

input[type='number'] {
  margin-left: 6px;
  padding: 8px 12px;
  width: 80px;
  font-size: 14px;
  background: #fff;
  border: 1.5px solid #4a90e2;
  border-radius: 8px;
  color: #1a3f72;
  outline: none;
}

.chart-wrapper {
  position: relative;
  width: 100%;
  height: 620px;
  background-color: #f9f9f9;
}

.chart-box {
  width: 100%;
  height: 100%;
  background-color: #fff;
  border-radius: 8px;
}

/* ✅ 控件区域统一样式 */
.inline-controls {
  display: flex;
  gap: 24px;
  justify-content: center;
  align-items: center;
  margin-top: 16px;
  flex-wrap: wrap;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 10px 16px;
  border-radius: 8px;
  width: auto;
}

.control-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.control-row label {
  font-size: 14px;
  color: #333;
}

.control-row input[type='number'] {
  width: 80px;
  padding: 4px 8px;
  font-size: 14px;
  background: #fff;
  border: 1.5px solid #4a90e2;
  border-radius: 8px;
  color: #1a3f72;
  outline: none;
}

.control-row button {
  width: 24px;
  height: 24px;
  font-size: 16px;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.control-row button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.control-row button:hover:not(:disabled) {
  background-color: #3a7bd5;
}

.status {
  color: #333;
  margin: 10px;
  font-weight: 600;
}

.error {
  color: #ff4d4f;
}
</style>
