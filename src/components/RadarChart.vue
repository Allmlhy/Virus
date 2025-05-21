<template>
  <div class="chart-container">
    <div class="controls">
      <label>
        国家：<span style="font-weight: bold; margin-left: 4px">{{ selectedCountry }}</span>
      </label>

      <label>
        截至日期：
        <input type="date" v-model="selectedDate" />
      </label>
      <button @click="fetchDataAndRenderChart">加载数据</button>
    </div>
    <div class="chart-switch">
      <button @click="showConfirmed" :disabled="currentChart === 'confirmed'">确诊数据</button>
      <button @click="showDeaths" :disabled="currentChart === 'deaths'">死亡数据</button>
    </div>
    <div ref="chartRef" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import * as echarts from 'echarts';
import { useRoute } from 'vue-router'

const route = useRoute()
const selectedCountry = ref(route.params.name || 'China')

const chartRef = ref(null);
const selectedDate = ref('2021-03-15');
let chartInstance = null;
let latestConfirmedData = [];
let latestDeathsData = [];
const currentChart = ref('confirmed'); // 'confirmed' 或 'deaths'
// watch(() => route.params.name, (newName) => {
//   selectedCountry.value = newName || 'China'
//   fetchDataAndRenderChart()
// })

const sharedRadarStyle = {
  radius: 120,
  splitNumber: 4,
  shape: 'circle',
  axisName: {
    formatter: '【{value}】',
    color: '#428BD4'
  },
  splitArea: {
    areaStyle: {
      color: ['#77EADF', '#26C3BE', '#64AFE9', '#428BD4'],
      shadowColor: 'rgba(0, 0, 0, 0.2)',
      shadowBlur: 10
    }
  },
  axisLine: {
    lineStyle: {
      color: 'rgba(211, 253, 250, 0.8)'
    }
  },
  splitLine: {
    lineStyle: {
      color: 'rgba(211, 253, 250, 0.8)'
    }
  }
};

const setConfirmedOption = () => {
  chartInstance.setOption({
    color: ['#67F9D8'],
    title: {
      text: `${selectedCountry.value} 确诊数据雷达图`,
      left: 'center',
      textStyle: { fontSize: 18 }
    },
    tooltip: {
      trigger: 'item',
      formatter: params => {
        const labels = ['确诊方差', '确诊标准差', '确诊中位数', '确诊众数', '确诊平均数'];
        return labels.map((label, i) => `${label}: ${params.value[i]}`).join('<br>');
      }
    },
    radar: {
      ...sharedRadarStyle,
      indicator: [
        { text: '确诊方差', max: 1000000 },
        { text: '确诊标准差', max: 1000 },
        { text: '确诊中位数', max: 50 },
        { text: '确诊众数', max: 50 },
        { text: '确诊平均数', max: 300 }
      ],
      center: ['50%', '50%']
    },
    series: [{
      type: 'radar',
      data: [{
        value: latestConfirmedData,
        name: '确诊数据',
        areaStyle: { color: 'rgba(103, 249, 216, 0.3)' }
      }]
    }]
  });
};

const setDeathsOption = () => {
  chartInstance.setOption({
    color: ['#FF917C'],
    title: {
      text: `${selectedCountry.value} 死亡数据雷达图`,
      left: 'center',
      textStyle: { fontSize: 18 }
    },
    tooltip: {
      trigger: 'item',
      formatter: params => {
        const labels = ['死亡方差', '死亡标准差', '死亡中位数', '死亡众数', '死亡平均数'];
        return labels.map((label, i) => `${label}: ${params.value[i]}`).join('<br>');
      }
    },
    radar: {
      ...sharedRadarStyle,
      indicator: [
        { text: '死亡方差', max: 5000 },
        { text: '死亡标准差', max: 500 },
        { text: '死亡中位数', max: 10 },
        { text: '死亡众数', max: 10 },
        { text: '死亡平均数', max: 100 }
      ],
      center: ['50%', '50%']
    },
    series: [{
      type: 'radar',
      data: [{
        value: latestDeathsData,
        name: '死亡数据',
        areaStyle: { color: 'rgba(255, 145, 124, 0.3)' }
      }]
    }]
  });
};

const fetchDataAndRenderChart = async () => {
  if (!selectedCountry.value || !selectedDate.value) {
    alert('请填写完整的国家和日期信息');
    return;
  }
  const [year, month, day] = selectedDate.value.split('-').map(Number);

  try {
    const response = await fetch(
      `http://localhost:8081/api/stats/country?countryName=${selectedCountry.value}&year=${year}&month=${month}&day=${day}`
    );
    const data = await response.json();

    latestConfirmedData = [
      data.confirmedVariance,
      data.confirmedStdDev,
      data.confirmedMedian,
      data.confirmedMode,
      data.confirmedMean
    ];

    latestDeathsData = [
      data.deathsVariance,
      data.deathsStdDev,
      data.deathsMedian,
      data.deathsMode,
      data.deathsMean
    ];

    if (!chartInstance) {
      chartInstance = echarts.init(chartRef.value);
      window.addEventListener('resize', () => chartInstance.resize());
    }

    if (currentChart.value === 'confirmed') {
      setConfirmedOption();
    } else {
      setDeathsOption();
    }
  } catch (err) {
    console.error('数据加载失败：', err);
    alert('获取数据失败，请检查国家名称和日期是否正确');
  }
};

const showConfirmed = () => {
  if (currentChart.value !== 'confirmed') {
    currentChart.value = 'confirmed';
    setConfirmedOption();
  }
};

const showDeaths = () => {
  if (currentChart.value !== 'deaths') {
    currentChart.value = 'deaths';
    setDeathsOption();
  }
};

onMounted(() => {
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
    window.addEventListener('resize', () => chartInstance.resize());
  }
  fetchDataAndRenderChart();
});
</script>

<style scoped>
.chart-container {
  max-width: 800px;
  height: 100%;
  background: #f9faff;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin: 0 auto; /* 🔥 让容器水平居中 */
}

.controls {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
  align-items: center;
}

.controls input {
  padding: 5px 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
}

.controls button {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  background-color: #428bd4;
  color: #fff;
  font-weight: bold;
  cursor: pointer;
}

.controls button:hover {
  background-color: #306fa3;
}

.chart-switch {
  text-align: center;
}

.chart-switch button {
  margin: 0 10px;
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  background-color: #ddd;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
}

.chart-switch button:disabled {
  background-color: #428bd4;
  color: white;
  cursor: default;
}

.chart-switch button:not(:disabled):hover {
  background-color: #a0c4ff;
}

.chart {
  width: 100%;
  height: 500px;
  min-width: 400px;
  margin: 0 auto;
}
</style>
