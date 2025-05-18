<template>
  <div class="chart-container">
    <h2 class="chart-title">月度疫情统计柱状图</h2>

    <!-- 筛选控件 -->
    <div class="filters">
      <label>
        起始月份：
        <input type="month" v-model="startMonth" min="2020-01" max="2025-12" />
      </label>

      <label>
        结束月份：
        <input type="month" v-model="endMonth" min="2020-01" max="2025-12" />
      </label>

      <label>
        省份：
        <select v-model="province">
          <option value="全国">全国</option>
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


          <!-- 你可以继续补充其他省份 -->
        </select>
      </label>

      <label v-if="province !== '全国'">
        城市：
        <input v-model="city" placeholder="请输入城市" class="styled-input" />
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

import { fetchMonthlyOneCitySummary } from '@/apis/apiMonthlyOneCityStats'; // 根据实际路径修改
import { fetchMonthlyWholeChinaSummary } from '@/apis/apiMonthlyWholeChina'; // 新增全国接口辅助函数

const startMonth = ref('2020-01');
const endMonth = ref('2020-06');
const province = ref('全国');
const city = ref('');

const loading = ref(false);
const error = ref(null);
const chartData = ref([]);

// 加载数据
async function loadData() {
  loading.value = true;
  error.value = null;

  try {
    if (province.value === '全国') {
      // 调用全国接口，不需要city参数
      const res = await fetchMonthlyWholeChinaSummary({
        startMonth: startMonth.value,
        endMonth: endMonth.value,
      });

      if (!Array.isArray(res)) {
        error.value = '返回数据格式错误';
        chartData.value = [];
      } else {
        chartData.value = res;
      }
    } else {
      // 省份不是全国时调用城市接口，city可为空字符串
      const res = await fetchMonthlyOneCitySummary({
        startMonth: startMonth.value,
        endMonth: endMonth.value,
        province: province.value,
        city: city.value,
      });

      if (!Array.isArray(res)) {
        error.value = '返回数据格式错误';
        chartData.value = [];
      } else {
        chartData.value = res;
      }
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
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['累计确诊', '累计死亡', '累计治愈'],
      top: 30,
      textStyle: { color: '#fff' }, // 图例字体颜色为白色
    },
    grid: { left: '5%', right: '5%', bottom: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: chartData.value.map(item => item.month),
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
        name: '累计确诊',
        type: 'bar',
        data: chartData.value.map(item => item.totalConfirmed || 0),
        itemStyle: { color: '#5470C6' },
      },
      {
        name: '累计死亡',
        type: 'bar',
        data: chartData.value.map(item => item.totalDeaths || 0),
        itemStyle: { color: '#EE6666' },
      },
      {
        name: '累计治愈',
        type: 'bar',
        data: chartData.value.map(item => item.totalRecovered || 0),
        itemStyle: { color: '#91CC75' },
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

input[type='month'],
input[type='text'],
select {
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
