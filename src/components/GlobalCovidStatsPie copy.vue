<template>
  <div class="container">
    <div class="form-row">
      <label>
        日期：
        <input type="date" v-model="date" />
      </label>
      <button @click="fetchStats">查询</button>
    </div>

    <div v-if="error" class="error-msg">
      {{ error }}
    </div>

    <div v-if="stats" class="stats-card">
      <p><strong>国家:</strong> {{ stats.country }}</p>
      <p><strong>日期:</strong> {{ stats.date }}</p>
      <p><strong>累计确诊:</strong> {{ stats.confirmed }}</p>
      <p><strong>累计死亡:</strong> {{ stats.deaths }}</p>
      <p><strong>估计康复:</strong> {{ stats.estimatedRecovered }}</p>
    </div>

    <div ref="chart" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'

const route = useRoute()
const countryName = ref(route.params.name || 'China')
const date = ref('2021-03-15')

const stats = ref(null)
const error = ref('')
const chart = ref(null)
let myChart = null

watch(() => route.params.name, (newVal) => {
  countryName.value = newVal
  fetchStats()
})

watch(date, () => {
  fetchStats()
})

async function fetchStats() {
  error.value = ''
  stats.value = null

  if (!countryName.value || !date.value) {
    error.value = '请填写国家和日期'
    return
  }

  try {
    const res = await fetch(`http://localhost:8081/api/stats/rates?countryName=${encodeURIComponent(countryName.value)}&date=${date.value}`)
    if (!res.ok) throw new Error('接口请求失败')
    const data = await res.json()

    if (data.message) {
      error.value = data.message
      return
    }

    stats.value = data
    updateChart(data)
  } catch (e) {
    error.value = e.message || '请求异常'
  }
}

function updateChart(data) {
  if (!chart.value) return

  if (!myChart) {
    myChart = echarts.init(chart.value)
  } else {
    // 关键点：每次更新前清除旧图表内容
    myChart.clear()
  }

  myChart.setOption({
    title: {
      text: `疫情数据 (${data.country} - ${data.date})`,
      left: 'center',
      textStyle: { fontWeight: 'bold', fontSize: 18 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {d}%'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '疫情比例',
        type: 'pie',
        radius: '50%',
        data: [
          { value: parseFloat((parseFloat(data.deathRate) * 100).toFixed(2)), name: '死亡率' },
          { value: parseFloat((parseFloat(data.cureRate) * 100).toFixed(2)), name: '治愈率' },
          { value: parseFloat((parseFloat(data.activeRate) * 100).toFixed(2)), name: '现患率' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  })
}

onMounted(() => {
  myChart = echarts.init(chart.value)
  fetchStats()
})

</script>

<style scoped>
.container {
  max-width: 400px;
  margin: 60px auto;
  padding: 20px;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  color: #333;
  background-color: #f7fbff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(66, 139, 212, 0.15);
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 28px;
  align-items: flex-end;
}

label {
  font-weight: 600;
  font-size: 15px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  min-width: 180px;
  color: #428BD4;
}

input[type="text"],
input[type="date"],
select {
  padding: 10px 12px;
  font-size: 14px;
  border: 1px solid #64AFE9;
  border-radius: 8px;
  transition: all 0.3s;
  box-shadow: 0 1px 2px rgba(38, 195, 190, 0.3);
  background-color: #ffffff;
  color: #333;
}

input:focus,
select:focus {
  border-color: #428BD4;
  box-shadow: 0 0 0 2px rgba(66, 139, 212, 0.3);
  outline: none;
}

button {
  padding: 10px 20px;
  font-size: 15px;
  background-color: #428BD4;
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s, box-shadow 0.3s;
  height: 42px;
}

button:hover {
  background-color: #67F9D8;
  box-shadow: 0 2px 6px rgba(103, 249, 216, 0.4);
}

.error-msg {
  color: #FF917C;
  font-weight: 600;
  margin-bottom: 20px;
  padding: 12px 16px;
  background-color: #FFEAEA;
  border-left: 4px solid #FF917C;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.stats-card {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 10px rgba(38, 195, 190, 0.15);
  margin-bottom: 30px;
  font-size: 16px;
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  justify-content: space-between;
  border-left: 4px solid #428BD4;
  transition: all 0.3s ease;
}

.stats-card p {
  margin: 0;
  flex: 1 1 auto;
  min-width: 120px;
  white-space: nowrap;
}

.chart {
  width: 100%;
  max-width: 640px;
  height: 420px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(66, 139, 212, 0.1);
  padding: 16px;
}
</style>
