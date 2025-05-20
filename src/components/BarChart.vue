<template>
  <div class="bar-chart-container">
    <h2>全球疫情前N个国家柱状图</h2>

    <!-- 前几个国家数量选择和是否显示累计确诊与累计死亡的复选框放一行 -->
    <div class="top-countries-selector">
      <label for="topCountriesCount">显示前N个国家 (5-20):</label>
      <input
          type="number"
          v-model="topCountriesCount"
          id="topCountriesCount"
          min="5"
          max="20"
          step="1"
          @change="updateChartData"
      >
      <!-- 是否显示累计确诊与累计死亡的复选框 -->
      <div class="checkboxes">
        <label>
          <input type="checkbox" v-model="showConfirmed" @change="updateChartData">
          显示累计确诊
        </label>
        <label>
          <input type="checkbox" v-model="showDeaths" @change="updateChartData">
          显示累计死亡
        </label>
      </div>
    </div>

    <!-- 柱状图 -->
    <div class="bar-chart">
      <Bar
          v-if="!loading && chartData.labels && chartData.labels.length > 0"
          :data="chartData"
          :options="chartOptions"
          :key="chartKey"
      />
      <div v-if="loading" class="loading">数据加载中...</div>
      <div v-if="!loading && chartData.labels && chartData.labels.length === 0" class="no-data">暂无数据</div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { Bar } from 'vue-chartjs';
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale
} from 'chart.js';
import { fetchGlobalStatsTopBottom } from '@/apis/globalStats.js';

// 注册chart.js组件
ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale);

// 接收来自父组件的日期参数
const props = defineProps({
  selectedYear: {
    type: Number,
    required: true,
    default: 2021  // 确保有默认值
  },
  selectedMonth: {
    type: Number,
    required: true,
    default: 1  // 确保有默认值
  },
  selectedDay: {
    type: Number,
    required: true,
    default: 1  // 确保有默认值
  }
});

// 组件内部状态
const topCountriesCount = ref(10);
const showConfirmed = ref(true);
const showDeaths = ref(true);
const loading = ref(false);
const chartKey = ref(0);

// 确保显示国家数量在5-20之间
const validatedCount = computed(() => {
  return Math.min(Math.max(topCountriesCount.value, 5), 20);
});

// 图表数据
const chartData = ref({
  labels: [],
  datasets: [
    {
      label: '累计确诊',
      data: [],
      backgroundColor: '#36A2EB',
      borderColor: '#2980B9',
      borderWidth: 1
    },
    {
      label: '累计死亡',
      data: [],
      backgroundColor: '#FF6384',
      borderColor: '#E74C3C',
      borderWidth: 1
    }
  ]
});

// 图表配置
const chartOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  indexAxis: 'y',  // 修改这里，设置为横向柱状图
  plugins: {
    title: {
      display: true,
      text: '全球疫情数据',
      font: {
        size: 16
      }
    },
    tooltip: {
      callbacks: {
        label: function(context) {
          return `${context.dataset.label}: ${context.raw.toLocaleString()}`;
        }
      }
    },
    legend: {
      position: 'top'
    }
  },
  scales: {
    x: {
      beginAtZero: true,
      ticks: {
        stepSize: 2000000,
        callback: function(value) {
          return value.toLocaleString();
        }
      }
    },
    y: {
      grid: {
        display: false
      }
    }
  }
});

// 获取数据
const updateChartData = async () => {
  loading.value = true;

  try {
    const params = {
      year: props.selectedYear,
      month: props.selectedMonth,
      day: props.selectedDay,
      limit: validatedCount.value  // 确保传递了正确的 limit 参数
    };

    const response = await fetchGlobalStatsTopBottom(params);
    console.log('API原始数据:', response); // 调试用

    // 转换数据格式并限制返回的前N个国家
    const countries = response.top20 || response; // 兼容不同API响应结构
    const limitedCountries = countries.slice(0, validatedCount.value); // 根据 `validatedCount.value` 限制国家数量

    chartData.value = {
      labels: limitedCountries.map(item => item.countryName),
      datasets: [
        {
          label: '累计确诊',
          data: showConfirmed.value ? limitedCountries.map(item => item.totalConfirmed) : [],
          backgroundColor: '#36A2EB',
          borderColor: '#2980B9',
          borderWidth: 1
        },
        {
          label: '累计死亡',
          data: showDeaths.value ? limitedCountries.map(item => item.totalDeaths) : [],
          backgroundColor: '#FF6384',
          borderColor: '#E74C3C',
          borderWidth: 1
        }
      ]
    };

    console.log('转换后的图表数据:', chartData.value); // 调试用
  } catch (error) {
    console.error('数据加载失败:', error);
    chartData.value = {
      labels: [],
      datasets: []
    };
  } finally {
    loading.value = false;
    chartKey.value++; // 强制图表重新渲染
  }
};

// 监听父组件传递的日期数据变化，更新图表数据
watch(
    () => [
      props.selectedYear,
      props.selectedMonth,
      props.selectedDay,
      validatedCount.value,
      showConfirmed.value,
      showDeaths.value
    ],
    () => {
      updateChartData();
      chartKey.value++; // 确保图表重渲染
    },
    { immediate: true }
);
</script>

<style scoped>
.bar-chart-container {
  margin-top: -30px;
  width: 100%;
  width: 500px;
  max-width: 1000px;
  margin: 20px auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #2C3E50;
  margin-bottom: 20px;
}

.top-countries-selector {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.checkboxes {
  display: flex;
  gap: 10px;
  margin-left: 20px;
}

label {
  font-weight: 500;
  color: #34495E;
}

input[type="number"], input[type="checkbox"] {
  padding: 4px 8px; /* 减少内边距 */
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

input[type="number"] {
  width: 60px;
}

input[type="checkbox"] {
  margin-right: 5px;
}

.bar-chart {
  width: 100%;
  height: 500px;
  margin-top: 20px;
  position: relative;
}

.loading, .no-data {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 18px;
  color: #999;
}

.no-data {
  color: #e74c3c;
}
</style>
