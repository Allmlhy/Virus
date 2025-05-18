<template>
  <TimePicker class="time-picker" @time-change="handleTimeChange" />
  <div ref="mapContainer" style="height: 100%; width: 100%" />
</template>

<script setup>
import * as echarts from 'echarts'
import chinaMap from 'echarts/map/json/china.json'  // 使用中国地图
import { onMounted, ref } from 'vue'
import axios from 'axios'
import TimePicker from '@/components/TimePicker/TimePicker.vue'

const mapContainer = ref(null)

const handleTimeChange = (newTime) => {
  // 如果你需要按时间刷新数据可以在这里添加逻辑
}

onMounted(async () => {
  const chart = echarts.init(mapContainer.value)

  // 注册中国地图
  echarts.registerMap('china', chinaMap)

  try {
    const response = await axios.get('http://localhost:8081/api/stats/province')
    const rawData = response.data

    // 格式化为 ECharts 所需的数据格式
    const chartData = Object.entries(rawData).map(([province, stats]) => ({
      name: province,
      value: stats['累计确诊人数'] ?? 0,
    }))

    // 配置图表
    chart.setOption({
      title: {
        text: '中国疫情地图',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: params => {
          const { name, value } = params
          return `${name}：${value ?? 0}人`
        }
      },
      visualMap: {
        min: 0,
        max: 70000,
        left: 'left',
        top: 'bottom',
        text: ['高', '低'],
        inRange: {
          color: ['#e0ffff', '#006edd']
        },
        calculable: true
      },
      series: [{
        name: '累计确诊人数',
        type: 'map',
        map: 'china',
        roam: true,
        label: {
          show: true,
          fontSize: 10
        },
        data: chartData
      }]
    })
  } catch (error) {
    console.error('获取疫情数据失败：', error)
  }
})
</script>
