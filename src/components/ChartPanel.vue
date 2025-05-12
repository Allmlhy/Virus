<template>
  <div class="charts">
    <div ref="deathChart" class="chart-box" />
    <div ref="confirmedChart" class="chart-box" />
    <div ref="recoveryChart" class="chart-box" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'

const deathChart = ref(null)
const confirmedChart = ref(null)
const recoveryChart = ref(null)

onMounted(() => {
  echarts.init(deathChart.value).setOption({
    title: { text: '每日死亡 + 累计' },
    xAxis: { type: 'category', data: ['1月', '2月'] },
    yAxis: {},
    series: [
      { name: '每日死亡', type: 'line', data: [5, 10] },
      { name: '累计死亡', type: 'line', data: [5, 15] }
    ]
  })

  echarts.init(confirmedChart.value).setOption({
    title: { text: '每日确诊 + 累计' },
    xAxis: { type: 'category', data: ['1月', '2月'] },
    yAxis: {},
    series: [
      { name: '每日确诊', type: 'line', data: [20, 30] },
      { name: '累计确诊', type: 'line', data: [20, 50] }
    ]
  })

  echarts.init(recoveryChart.value).setOption({
    title: { text: '接种趋势 + 治愈' },
    xAxis: { type: 'category', data: ['1月', '2月'] },
    yAxis: {},
    series: [
      { name: '接种趋势', type: 'line', data: [100, 300] },
      { name: '累计治愈', type: 'line', data: [10, 25] }
    ]
  })
})
</script>

<style scoped>
.charts {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.chart-box {
  height: 300px;
}
</style>
