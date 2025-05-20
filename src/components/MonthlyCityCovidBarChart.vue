<template>
  <div class="chart-container">
    <h2 class="chart-title">月度疫情趋势折线图</h2>

    <!-- 筛选控件 -->
    <div class="filters">
      <label>
        起始月份：
        <input type="month" v-model="startMonth" min="2020-01" max="2025-12" class="input-box" />
      </label>

      <label>
        结束月份：
        <input type="month" v-model="endMonth" min="2020-01" max="2025-12" class="input-box" />
      </label>

      <label>
        省份：
        <select v-model="province" class="select-box">
            <option value="全国">全国</option>
            <!-- 省份列表同上 -->
            <option value="北京">北京</option>
            <option value="天津">天津</option>
            <option value="上海">上海</option>
            <option value="重庆">重庆</option>
            <option value="河北">河北</option>
            <option value="山西">山西</option>
            <option value="辽宁">辽宁</option>
            <option value="吉林">吉林</option>
            <option value="黑龙江">黑龙江</option>
            <option value="江苏">江苏</option>
            <option value="浙江">浙江</option>
            <option value="安徽">安徽</option>
            <option value="福建">福建</option>
            <option value="江西">江西</option>
            <option value="山东">山东</option>
            <option value="河南">河南</option>
            <option value="湖北">湖北</option>
            <option value="湖南">湖南</option>
            <option value="广东">广东</option>
            <option value="海南">海南</option>
            <option value="四川">四川</option>
            <option value="贵州">贵州</option>
            <option value="云南">云南</option>
            <option value="陕西">陕西</option>
            <option value="甘肃">甘肃</option>
            <option value="青海">青海</option>
            <option value="台湾">台湾</option>
            <option value="内蒙古">内蒙古</option>
            <option value="广西">广西</option>
            <option value="西藏">西藏</option>
            <option value="宁夏">宁夏</option>
            <option value="新疆">新疆</option>
            <option value="香港">香港</option>
            <option value="澳门">澳门</option>
          </select>
      </label>

      <label v-if="province !== '全国'">
        城市：
        <input v-model="city" placeholder="请输入城市" class="input-box" />
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

import { fetchMonthlyOneCitySummary } from '@/apis/apiMonthlyOneCityStats';
import { fetchMonthlyWholeChinaSummary } from '@/apis/apiMonthlyWholeChina';

const startMonth = ref('2020-01');
const endMonth = ref('2020-06');
const province = ref('全国');
const city = ref('');

const loading = ref(false);
const error = ref(null);
const chartData = ref([]);

async function loadData() {
  loading.value = true;
  error.value = null;

  try {
    let res;
    if (province.value === '全国') {
      res = await fetchMonthlyWholeChinaSummary({
        startMonth: startMonth.value,
        endMonth: endMonth.value,
      });
    } else {
      res = await fetchMonthlyOneCitySummary({
        startMonth: startMonth.value,
        endMonth: endMonth.value,
        province: province.value,
        city: city.value,
      });
    }

    if (!Array.isArray(res)) {
      error.value = '返回数据格式错误';
      chartData.value = [];
    } else {
      chartData.value = res;
    }
  } catch (err) {
    error.value = '数据加载失败';
    chartData.value = [];
    console.error(err);
  } finally {
    loading.value = false;
  }
}

watch([startMonth, endMonth, province, city], loadData, { immediate: true });

const chartOptions = computed(() => {
  if (!chartData.value.length) return {};

  return {
    backgroundColor: '#fff',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(50,50,50,0.7)',
      textStyle: { color: '#fff' },
    },
    legend: {
      data: ['累计确诊', '累计死亡', '累计治愈'],
      top: 30,
      textStyle: { color: '#333' },
    },
    grid: { left: '5%', right: '5%', bottom: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: chartData.value.map(item => item.month),
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
        lineStyle: { color: '#eee' },
      },
    },
    series: [
      {
        name: '累计确诊',
        type: 'line',
        data: chartData.value.map(item => item.totalConfirmed || 0),
        smooth: true,
        lineStyle: { color: '#8884d8' },
        itemStyle: { color: '#8884d8' },
        showSymbol: false,
      },
      {
        name: '累计死亡',
        type: 'line',
        data: chartData.value.map(item => item.totalDeaths || 0),
        smooth: true,
        lineStyle: { color: '#ff4d4f' },
        itemStyle: { color: '#ff4d4f' },
        showSymbol: false,
      },
      {
        name: '累计治愈',
        type: 'line',
        data: chartData.value.map(item => item.totalRecovered || 0),
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
        },
      },
    ],
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
