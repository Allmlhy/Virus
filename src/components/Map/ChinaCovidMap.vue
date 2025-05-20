<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts';
import chinaMap from '@/assets/map/china.json';

const props = defineProps({
  rawData: {
    type: Object,
    default: null
  }
});

const chartContainer = ref(null);
let chart = null;

const initChart = () => {
  if (!chartContainer.value) return;

  if (chart) {
    chart.dispose();
  }

  chart = echarts.init(chartContainer.value);
  echarts.registerMap('china', chinaMap);
};

const renderChart = (data) => {
  if (!chart || !data) return;

  const chartData = Object.entries(data).map(([province, stats]) => ({
    name: province,
    value: stats['累计确诊人数'] ?? 0,
    details: {
      累计确诊人数: stats['累计确诊人数'] ?? 0,
      累计死亡人数: stats['累计死亡人数'] ?? 0,
      累计治愈人数: stats['累计治愈人数'] ?? 0,
      累计境外输入人数: stats['累计境外输入人数'] ?? 0,
      新增确诊人数: stats['新增确诊人数'] ?? 0,
      新增死亡人数: stats['新增死亡人数'] ?? 0,
      新增治愈人数: stats['新增治愈人数'] ?? 0,
      新增疑似病例数: stats['新增疑似病例数'] ?? 0,
    }
  }));

  const values = chartData.map(item => item.value);
  const maxValue = Math.min(Math.max(...values), 10000);
  const minValue = Math.max(Math.min(...values), 0);

  const option = {
    backgroundColor: '#ffffff',
    title: {
      text: '全国疫情分布图',
      left: 'center',
      textStyle: {
        color: '#2c3e50',
        fontSize: 20,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#ccc',
      borderWidth: 1,
      textStyle: {
        color: '#333',
        fontSize: 13
      },
      formatter: params => {
        if (!params.data) return `${params.name}: 无数据`;
        const d = params.data.details;
        return `
          <strong>${params.name}</strong><br/>
          累计确诊人数: ${d.累计确诊人数}<br/>
          累计死亡人数: ${d.累计死亡人数}<br/>
          累计治愈人数: ${d.累计治愈人数}<br/>
          累计境外输入人数: ${d.累计境外输入人数}<br/>
          新增确诊人数: ${d.新增确诊人数}<br/>
          新增死亡人数: ${d.新增死亡人数}<br/>
          新增治愈人数: ${d.新增治愈人数}<br/>
          新增疑似病例数: ${d.新增疑似病例数}
        `;
      }
    },
    visualMap: {
      min: minValue,
      max: maxValue,
      left: 'left',
      bottom: '10%',
      text: ['高', '低'],
      calculable: true,
      inRange: {
        color: ['#d0f0f9', '#a0d8ef', '#70b7df', '#4088c0', '#2b3f77']  // 医学冷色调渐变
      },
      textStyle: {
        color: '#333'
      }
    },
    series: [{
      name: '累计确诊人数',
      type: 'map',
      map: 'china',
      roam: true,
      label: {
        show: true,
        fontSize: 10,
        color: '#444'
      },
      data: chartData,
      emphasis: {
        label: {
          show: true,
          color: '#fff',
          fontWeight: 'bold'
        },
        itemStyle: {
          areaColor: '#ff9e7a'
        }
      },
      itemStyle: {
        borderColor: '#ccc',
        borderWidth: 1,
        areaColor: '#eef6fc'
      }
    }]
  };

  chart.setOption(option);
};

onMounted(() => {
  initChart();
  renderChart(props.rawData);
});

watch(() => props.rawData, (newData) => {
  renderChart(newData);
});

onBeforeUnmount(() => {
  if (chart) {
    chart.dispose();
  }
});
</script>

<template>
  <div class="map-container" ref="chartContainer" style="height: 600px;">
    <p v-if="!props.rawData">地图加载中...</p>
  </div>
</template>

<style scoped>
.map-container {
  margin-top: 25px;
  background: linear-gradient(135deg, #f6fafd 0%, #dfefff 100%);
}
</style>
