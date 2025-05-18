<template>
  <div class="chart-container">
    <h2>某日各省疫情对比柱状图</h2>
    <!-- 自定义输入控件，放置在装图表的容器的左上角 -->
    <div class="custom-controls">
      <div class="control-row">
        <label>Y轴最小值：</label>
        <input type="number" v-model.number="yMin" min="0" />
      </div>
      <div class="control-row">
        <label>Y轴最大值：</label>
        <input type="number" v-model.number="yMax" min="0" />
      </div>
      <div class="control-row">
        <label>省份数量：&nbsp&nbsp&nbsp&nbsp</label>
        <button @click="decrementCount" :disabled="displayCount <= 1">-</button>
        <label>&nbsp&nbsp&nbsp&nbsp</label>
        <button @click="incrementCount" :disabled="displayCount >= chartData.length">+</button>
      </div>
    </div>

    <!-- 筛选控件 -->
    <div class="filters">
      <label>
        年：
        <input type="number" v-model.number="year" min="2020" max="2025" />
      </label>
      <label>
        月：
        <input type="number" v-model.number="month" min="1" max="12" />
      </label>
      <label>
        日：
        <input type="number" v-model.number="day" min="1" max="31" />
      </label>
    </div>

    <!-- 图表展示区域 -->
    <div class="chart-wrapper">
      <!-- 图表展示 -->
      <div class="chart-box" v-if="chartData.length">
        <v-chart :option="chartOptions" style="width: 100%; height: 100%;" />
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

// 日期选择
const year = ref(2020);
const month = ref(1);
const day = ref(20);

// 自定义输入控件
const yMin = ref(0);
const yMax = ref(100);
const displayCount = ref(7); // 初始默认为7

// 状态管理
const loading = ref(false);
const error = ref(null);
const chartData = ref([]);

// 校验日期合法性
function isValidDate(y, m, d) {
  const date = new Date(y, m - 1, d);
  return (
      date.getFullYear() === y &&
      date.getMonth() + 1 === m &&
      date.getDate() === d
  );
}

// 加载数据
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

// 监听日期变化
watch([year, month, day], loadData, { immediate: true });

// 增加省份数量
function incrementCount() {
  if (displayCount.value < chartData.value.length) {
    displayCount.value++;
  }
}

// 减少省份数量
function decrementCount() {
  if (displayCount.value > 1) {
    displayCount.value--;
  }
}

// 图表配置项
const chartOptions = computed(() => {
  if (!chartData.value.length) return {};

  // 根据 displayCount 对数据进行排序并截取前 displayCount 条
  const sortedData = [...chartData.value].sort((a, b) => {
    // 按累计确诊降序排序
    return (b.totalConfirmed || 0) - (a.totalConfirmed || 0);
  });

  const topData = sortedData.slice(0, displayCount.value);
  const totalCount = chartData.value.length;
  const startPercent = 0;
  const endPercent = (displayCount.value / totalCount) * 100;

  return {
    backgroundColor: '#003366',
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['当前确诊', '累计确诊', '累计死亡'],
      top: 30,
      textStyle: { color: '#fff' },
    },
    grid: {
      left: '10%', // 增加左侧空间以容纳自定义控件
      right: '4%',
      bottom: '18%', // 增加底部空间以容纳 dataZoom 和自定义控件
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: topData.map((item) => item.region),
      axisLabel: {
        rotate: 45,
        interval: 0,
        color: '#fff',
      },
    },
    yAxis: {
      type: 'value',
      min: yMin.value,
      max: yMax.value,
      axisLabel: { color: '#fff' },
      splitLine: { lineStyle: { color: '#444' } },
    },

    dataZoom: [
      {
        type: 'slider',
        show: true,
        xAxisIndex: 0,
        start: startPercent,
        end: endPercent,
        zoomLock: true, // 锁定缩放比例
        bottom: 50,
        height: 20,
        textStyle: { color: '#fff' },
      },
      {
        type: 'inside',
        xAxisIndex: 0,
        start: startPercent,
        end: endPercent,
        zoomLock: true, // 锁定缩放比例
        zoomOnMouseWheel: true,
        moveOnMouseWheel: true,
        moveOnMouseMove: true,
        throttle: 100,
        zoomRate: 0.1,
      },
    ],

    series: [
      {
        name: '当前确诊',
        type: 'bar',
        data: topData.map((item) => item.currentConfirmed || 0),
        itemStyle: { color: '#F4A261' },
      },
      {
        name: '累计确诊',
        type: 'bar',
        data: topData.map((item) => item.totalConfirmed || 0),
        itemStyle: { color: '#2A9D8F' },
      },
      {
        name: '累计死亡',
        type: 'bar',
        data: topData.map((item) => item.totalDeaths || 0),
        itemStyle: { color: '#E76F51' },
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
  padding: 20px;
  background: #002244;
  color: white;
  height: 700px; /* 增加高度以容纳自定义控件 */
  box-sizing: border-box;
  position: relative; /* 为绝对定位的自定义控件提供参考 */
}

h2 {
  text-align: center;
  margin-bottom: 12px;
  color: #fff;
}

/* 自定义输入控件样式 */
.custom-controls {
  position: absolute;
  top: 10px;
  left: 10px;
  background-color: rgba(0, 0, 0, 0.6);
  padding: 10px;
  border-radius: 5px;
  z-index: 10;
  width: 200px; /* 设置固定宽度，确保布局稳定 */
}

.control-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.control-row label {
  font-size: 14px;
  margin-right: 5px;
}

.control-row input[type='number'] {
  width: 80px;
  padding: 4px 8px;
  font-size: 14px;
  background: #eef;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.control-row button {
  width: 20px;
  height: 20px;
  padding: 0;
  margin: 0 5px;
  font-size: 16px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.control-row button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.control-row span {
  margin: 0 5px;
  font-size: 14px;
}

.filters {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

label {
  font-weight: 500;
  color: #fff;
}

input[type='number'] {
  margin-left: 6px;
  padding: 4px 8px;
  width: 80px;
  font-size: 14px;
  background: #eef;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.chart-wrapper {
  position: relative;
  width: 100%;
  max-width: 1200px;
  height: 650px; /* 图表高度 */
}

.chart-box {
  width: 100%;
  height: 100%;
}

.status {
  color: #fff;
  margin: 10px;
}

.error {
  color: red;
}
</style>