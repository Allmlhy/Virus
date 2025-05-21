<template>
  <div class="container">
    <h2 class="chart-title">🌍 全球累计确诊数</h2>

    <div class="threshold-container">
      <label for="thresholdRange">
        确诊数阈值:
        <span class="threshold-value">{{ threshold }}</span>
      </label>
      <input id="thresholdRange" type="range" min="0" max="500000" step="1000" v-model="threshold" />
    </div>

    <div ref="chartRef" class="globe-chart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import * as echarts from 'echarts'
import 'echarts-gl'
import { locationData } from '../assets/earth/Location.js'

const chartRef = ref(null)
let myChart = null

const threshold = ref(10000)
const rawData = ref({})

const locationMap = new Map(
  locationData.map(([name, lat, lon]) => [name, [parseFloat(lat), parseFloat(lon)]])
)

function shuffle(array) {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
      ;[array[i], array[j]] = [array[j], array[i]]
  }
  return array
}

const fetchConfirmedData = async () => {
  try {
    const response = await fetch('http://localhost:8081/api/stats/confirmed/current')
    if (!response.ok) throw new Error('网络请求失败')
    rawData.value = await response.json()
    updateChart()
  } catch (err) {
    console.error('获取确诊数据失败:', err)
  }
}

const scatterData = computed(() => {
  const filtered = []
  for (const [country, count] of Object.entries(rawData.value)) {
    if (count > threshold.value && locationMap.has(country)) {
      const [lat, lon] = locationMap.get(country)
      filtered.push({
        name: country,
        value: [lon, lat, 0]
      })
    }
  }
  return shuffle(filtered).slice(0, 10)
})

const updateChart = () => {
  if (!chartRef.value) return

  if (!myChart) {
    myChart = echarts.init(chartRef.value)
    window.addEventListener('resize', () => myChart && myChart.resize())
  }

  const option = {
    backgroundColor: '#000',
    globe: {
      baseTexture: '/earth/earth.jpg',
      displacementScale: 0.15,
      shading: 'lambert',
      viewControl: {
        distance: 140,
        autoRotate: true,
        autoRotateSpeed: 5
      },
      environment:
        'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMSIgaGVpZ2h0PSIxIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgo8cmVjdCB3aWR0aD0iMSIgaGVpZ2h0PSIxIiBmaWxsPSIjY2VlZmZmIi8+Cjwvc3ZnPg==',
      light: {
        ambient: { intensity: 0.6 },
        main: { intensity: 1.8, shadow: true }
      },
      postEffect: {
        enable: true,
        SSAO: { enable: true, radius: 8, intensity: 1.2 }
      }
    },
    series: [
      {
        type: 'scatter3D',
        coordinateSystem: 'globe',
        symbolSize: val => {
          const count = rawData.value[val.name]
          return count ? Math.log10(count) * 3.5 : 0
        },
        itemStyle: {
          color: '#00ccff',
          shadowBlur: 10,
          shadowColor: '#00ccff88'
        },
        label: {
          show: true,
          formatter: params => `${params.name}\n确诊: ${rawData.value[params.name]}`,
          color: '#fff',
          fontSize: 10,
          fontWeight: 'bold',
          backgroundColor: 'rgba(0,0,0,0.6)',
          borderRadius: 5,
          padding: [4, 6],
          borderColor: '#00ccff',
          borderWidth: 1,
          distance: 15,
          hideOverlap: true
        },
        emphasis: {
          label: {
            show: true,
            color: '#00ccff',
            fontWeight: 'bolder',
            fontSize: 12
          }
        },
        data: scatterData.value
      }
    ]
  }

  myChart.setOption(option)
}

onMounted(fetchConfirmedData)
watch([threshold, rawData], updateChart)
</script>

<style scoped>
.container {
  padding: 20px;
  background-color: #f5f8fa;
  border-radius: 16px;
  box-shadow: 0 0 15px rgba(0, 204, 255, 0.2);
}

.chart-title {
  color: #004466;
  font-weight: bold;
  font-size: 20px;
  text-align: center;
  margin-bottom: 25px;
  user-select: none;
}

.threshold-container {
  margin: 0 auto 20px auto;
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #004466;
}

.threshold-container label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.threshold-value {
  color: #ff3366;
  font-size: 16px;
  font-weight: bold;
  font-family: 'Segoe UI', sans-serif;
  min-width: 80px;
  text-align: right;
}

input[type="range"] {
  width: 320px;
  height: 10px;
  background: linear-gradient(to right, #00ccff, #3399ff);
  border-radius: 5px;
  appearance: none;
  cursor: pointer;
  transition: background 0.3s;
}

input[type="range"]::-webkit-slider-thumb {
  appearance: none;
  width: 22px;
  height: 22px;
  background-color: #ffffff;
  border: 3px solid #00ccff;
  border-radius: 50%;
  box-shadow: 0 0 6px #00ccff;
  transition: all 0.3s ease;
}

input[type="range"]:hover::-webkit-slider-thumb {
  transform: scale(1.15);
  background-color: #ccf5ff;
  border-color: #66e0ff;
}

.globe-chart {
  width: 100%;
  height: 600px;
  border-radius: 16px;
  background-color: #000;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}
</style>
