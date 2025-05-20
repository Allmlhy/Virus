<template>
  <div class="chart-container">
    <h2 class="chart-title">疫情每日趋势折线图</h2>

    <!-- 筛选控件 -->
    <div class="filters">
      <label>
        年份：
        <select v-model="year" class="select-box">
          <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
        </select>
      </label>

      <label>
        月份：
        <select v-model="month" class="select-box">
          <option value="">全部</option>
          <option v-for="m in months" :key="m" :value="m">{{ m }}</option>
        </select>
      </label>

      <label>
        省份：
        <select v-model="province" class="select-box">
          <option value="全国">全国</option>
          <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
        </select>
      </label>

      <label>
        城市：
        <input v-model="city" placeholder="请输入城市名" class="input-box" />
      </label>
    </div>

    <!-- 显示状态 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="!chartData.length" class="no-data">暂无数据</div>
    <v-chart v-else :option="chartOptions" class="chart-box" />
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import VChart from 'vue-echarts';
import { fetchDailyStats } from '@/apis/apiDailyStats';
import { fetchDailyStatsWholeChina } from '@/apis/apiDailyStatsWholeChina';

const year = ref(2020);
const month = ref(1);
const province = ref('上海');
const city = ref('上海');

const years = Array.from({ length: 5 }, (_, i) => 2020 + i);
const months = Array.from({ length: 12 }, (_, i) => i + 1);
const provinces = [
  "北京", "天津", "上海", "重庆",
  "河北", "山西", "辽宁", "吉林", "黑龙江",
  "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南",
  "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南",
  "陕西", "甘肃", "青海", "台湾",
  "内蒙古", "广西", "西藏", "宁夏", "新疆",
  "香港", "澳门"
];

const loading = ref(false);
const error = ref(null);
const chartData = ref([]);

async function loadData() {
  loading.value = true;
  error.value = null;

  try {
    let res;
    if (province.value === '全国') {
      res = await fetchDailyStatsWholeChina({
        year: year.value,
        month: month.value || undefined,
        province: province.value,
        city: city.value,
      });
    } else {
      res = await fetchDailyStats({
        year: year.value,
        month: month.value || undefined,
        province: province.value,
        city: city.value,
      });
    }

    if (!Array.isArray(res)) {
      chartData.value = [];
    } else {
      chartData.value = res;
    }
  } catch (err) {
    error.value = '数据加载失败';
  } finally {
    loading.value = false;
  }
}

watch([year, month, province, city], loadData, { immediate: true });

const chartOptions = computed(() => {
  if (!chartData.value.length) return {};

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(50,50,50,0.7)',
      textStyle: {
        color: '#fff'
      }
    },
    legend: {
      data: ['新增确诊数', '新增死亡数', '新增治愈数'],
      top: 30,
      textStyle: { color: '#333' },
    },
    grid: { left: '5%', right: '5%', bottom: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: chartData.value.map(item => `${item.day}日`),
      axisLabel: {
        rotate: 45,
        interval: 0,
        color: '#333',
      },
      axisPointer: { type: 'shadow' },
      boundaryGap: false,
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#333' },
      axisPointer: { type: 'shadow' },
      splitLine: {
        lineStyle: {
          color: '#eee'
        }
      }
    },
    series: [
      {
        name: '新增确诊数',
        type: 'line',
        data: chartData.value.map(item => item.newConfirmed),
        smooth: true,
        lineStyle: { color: '#8884d8' },
        itemStyle: { color: '#8884d8' },
        showSymbol: false,
      },
      {
        name: '新增死亡数',
        type: 'line',
        data: chartData.value.map(item => item.newDeaths),
        smooth: true,
        lineStyle: { color: '#ff4d4f' },
        itemStyle: { color: '#ff4d4f' },
        showSymbol: false,
      },
      {
        name: '新增治愈数',
        type: 'line',
        data: chartData.value.map(item => item.newRecovered),
        smooth: true,
        lineStyle: { color: '#82ca9d' },
        itemStyle: { color: '#82ca9d' },
        showSymbol: false,
      },
    ],
    dataZoom: [
      {
        type: 'inside',
        xAxisIndex: [0],
        start: 0,
        end: 100,
      },
      {
        type: 'slider',
        xAxisIndex: [0],
        start: 0,
        end: 100,
        handleSize: '100%',
        handleStyle: {
          color: '#aaa',
        }
      }
    ],
    backgroundColor: '#fff',
  };
});
</script>

<style scoped>
.chart-container {
  background-color: #fff;
  border-radius: 12px;
  padding: 12px;
  margin: 10px 0 10px 20px;
  width: 35%;
  min-width: 300px;
  color: #333;
  box-sizing: border-box;
}

.chart-title {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 10px;
  justify-content: center;
}

label {
  font-weight: bold;
  color: #333;
  font-size: 16px;
  display: flex;
  align-items: center;
}

.select-box,
.input-box {
  appearance: none;
  background-color: #fff;
  border: 1.5px solid #4a90e2;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
  color: #1a3f72;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  box-sizing: border-box;
  outline: none;
  cursor: text;
  margin-left: 6px;
}

.select-box {
  padding-right: 32px;
  cursor: pointer;
  background-image: url("data:image/svg+xml,%3Csvg fill='none' height='10' viewBox='0 0 24 24' width='10' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M7 10l5 5 5-5' stroke='%234a90e2' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 12px 12px;
}

.select-box:hover,
.input-box:hover {
  border-color: #3a7bd5;
  box-shadow: 0 0 6px rgba(58, 123, 213, 0.5);
}

.select-box:focus,
.input-box:focus {
  border-color: #255aab;
  box-shadow: 0 0 8px rgba(37, 90, 171, 0.7);
  outline: none;
}

.input-box {
  width: 140px;
}

input::placeholder {
  color: #8faeea;
}

input:focus::placeholder {
  color: #3a7bd5;
}

.loading,
.error,
.no-data {
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  margin-top: 20px;
}

.loading {
  color: #333;
}

.error {
  color: #ff4d4f;
}

.no-data {
  color: #888;
}

.chart-box {
  width: 100%;
  height: 400px;
  border: none;
  border-radius: 8px;
}
</style>
