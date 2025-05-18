<template>
  <div class="chart-container">
    <h2 class="chart-title">疫情每日趋势柱状图</h2>

    <!-- 筛选控件 -->
    <div class="filters">
      <label>
        年份：
        <select v-model="year">
          <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
        </select>
      </label>

      <label>
        月份：
        <select v-model="month">
          <option value="">全部</option>
          <option v-for="m in months" :key="m" :value="m">{{ m }}</option>
        </select>
      </label>

      <label>
        省份：
        <select v-model="province">
          <option value="全国">全国</option>
          <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
        </select>
      </label>

      <label>
        城市：
        <input v-model="city" placeholder="请输入城市名" class="styled-input" />
      </label>
    </div>

    <!-- 显示状态 -->
    <div v-if="loading">加载中...</div>
    <div v-else-if="error" style="color: red">{{ error }}</div>
    <div v-else-if="!chartData.length">暂无数据</div>
    <v-chart v-else :option="chartOptions" style="width: 100%; height: 400px;" />
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import VChart from 'vue-echarts';
import { fetchDailyStats } from '@/apis/apiDailyStats';
import { fetchDailyStatsWholeChina } from '@/apis/apiDailyStatsWholeChina'; // 新增导入

// 筛选参数，设置默认值为2020年1月，省份和城市均为上海
const year = ref(2020);
const month = ref(1);
const province = ref('上海');
const city = ref('上海');

// 年份和月份选择项
const years = Array.from({ length: 5 }, (_, i) => 2020 + i);
const months = Array.from({ length: 12 }, (_, i) => i + 1);

// 示例省份列表
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

// 加载数据函数
async function loadData() {
  console.log('开始加载数据，参数：', {
    year: year.value,
    month: month.value,
    province: province.value,
    city: city.value,
  });

  loading.value = true;
  error.value = null;

  try {
    let res;

    // 根据省份选择不同的API
    if (province.value === '全国') {
      res = await fetchDailyStatsWholeChina({
        year: year.value,
        month: month.value || undefined, // 为空时传 undefined
        province: province.value,
        city: city.value,
      });
    } else {
      res = await fetchDailyStats({
        year: year.value,
        month: month.value || undefined, // 为空时传 undefined
        province: province.value,
        city: city.value,
      });
    }

    console.log('接口返回数据：', res);

    if (!Array.isArray(res)) {
      console.warn('返回数据格式异常，预期数组，实际为：', res);
      chartData.value = [];
    } else {
      chartData.value = res;
    }
  } catch (err) {
    error.value = '数据加载失败';
    console.error('加载数据出错：', err);
  } finally {
    loading.value = false;
    console.log('数据加载完成');
  }
}

// 筛选条件变化时自动加载数据
watch([year, month, province, city], loadData, { immediate: true });

// 图表配置项
const chartOptions = computed(() => {
  if (!chartData.value.length) return {};

  return {
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['新增确诊数', '新增死亡数', '新增治愈数'],
      top: 30,
      textStyle: { color: '#fff' }, // 图例字体颜色为白色
    },
    grid: { left: '5%', right: '5%', bottom: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: chartData.value.map(item => `${item.day}日`), // 修改这里，确保day是数字
      axisLabel: {
        rotate: 45,
        interval: 0,
        color: '#fff' // 横坐标字体颜色为白色
      },
      axisPointer: { type: 'shadow' }, // 横坐标支持拖动
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#fff' }, // 纵坐标字体颜色为白色
      axisPointer: { type: 'shadow' }, // 纵坐标支持拖动
    },
    series: [
      {
        name: '新增确诊数',
        type: 'bar',
        data: chartData.value.map(item => item.newConfirmed),
        itemStyle: { color: '#8884d8' },
      },
      {
        name: '新增死亡数',
        type: 'bar',
        data: chartData.value.map(item => item.newDeaths),
        itemStyle: { color: '#ff4d4f' },
      },
      {
        name: '新增治愈数',
        type: 'bar',
        data: chartData.value.map(item => item.newRecovered),
        itemStyle: { color: '#82ca9d' },
      },
    ],
    dataZoom: [
      { // 添加内部滚动条，支持横坐标范围拖动
        type: 'inside',
        xAxisIndex: [0],
        start: 0,  // 默认显示前5个月
        end: 100,
      },
      { // 添加滚动条，支持拖动查看其他月份
        type: 'slider',
        xAxisIndex: [0],
        start: 0,  // 默认显示前5个月
        end: 100,
        handleSize: '100%', // 滚动条的宽度
      }
    ],
  };
});
</script>

<style scoped>
h2.chart-title {
  text-align: center; /* 标题居中 */
  margin-bottom: 12px;
  color: #fff;
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

select, input {
  margin-left: 6px;
  padding: 4px 8px;
  font-size: 14px;
  background: #eef;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.styled-input {
  margin-left: 6px;
  padding: 4px 8px;
  font-size: 14px;
  background: #eef;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.chart-container {
  padding: 20px;
  background: #002244;
  color: white;
  width: 50%; /* 容器宽度为100% */
  box-sizing: border-box;
  margin: 0 auto;
}

v-chart {
  width: 100%;
  height: 400px;
}
</style>
