<template>
  <div ref="chartRef" style="width: 100%; height: 800px;"></div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)
let chartInstance = null

// 省份名兼容映射（接口返回是“上海”，地图需要“上海市”）
const provinceMap = {
  '北京': '北京市', '天津': '天津市', '上海': '上海市', '重庆': '重庆市',
  '河北': '河北省', '山西': '山西省', '辽宁': '辽宁省', '吉林': '吉林省',
  '黑龙江': '黑龙江省', '江苏': '江苏省', '浙江': '浙江省', '安徽': '安徽省',
  '福建': '福建省', '江西': '江西省', '山东': '山东省', '河南': '河南省',
  '湖北': '湖北省', '湖南': '湖南省', '广东': '广东省', '海南': '海南省',
  '四川': '四川省', '贵州': '贵州省', '云南': '云南省', '陕西': '陕西省',
  '甘肃': '甘肃省', '青海': '青海省', '台湾': '台湾省', '内蒙古': '内蒙古自治区',
  '广西': '广西壮族自治区', '西藏': '西藏自治区', '宁夏': '宁夏回族自治区',
  '新疆': '新疆维吾尔自治区', '香港': '香港特别行政区', '澳门': '澳门特别行政区'
}

const mapOption = (data) => ({
  title: {
    text: '全国累计确诊病例分布',
    left: 'center',
    top: 20,
    textStyle: {
      fontSize: 24,
      fontWeight: 'bold'
    }
  },
  visualMap: {
    left: 'right',
    min: 0,
    max: Math.max(...data.map(d => d.value)),
    inRange: {
      color: ['#e0f3f8', '#abd9e9', '#74add1', '#4575b4', '#313695']
    },
    text: ['高', '低'],
    calculable: true
  },
  series: [
    {
      id: 'confirmed',
      type: 'map',
      roam: true,
      map: 'china',
      animationDurationUpdate: 1000,
      universalTransition: true,
      data
    }
  ]
})

const barOption = (data) => ({
  title: {
    text: '全国累计确诊病例分布',
    left: 'center',
    top: 20,
    textStyle: {
      fontSize: 24,
      fontWeight: 'bold'
    }
  },
  xAxis: {
    type: 'value'
  },
  yAxis: {
    type: 'category',
    axisLabel: {
      rotate: 30
    },
    data: data.map(item => item.name)
  },
  animationDurationUpdate: 1000,
  series: {
    type: 'bar',
    id: 'confirmed',
    data: data.map(item => item.value),
    universalTransition: true
  }
})

onMounted(async () => {
  chartInstance = echarts.init(chartRef.value)
  chartInstance.showLoading()

  // 加载地图
  const res = await fetch('https://geo.datav.aliyun.com/areas/bound/100000_full.json')
  const chinaJson = await res.json()
  echarts.registerMap('china', chinaJson)

  // 加载接口数据
  const response = await fetch('http://localhost:8081/api/stats/province')
  const rawData = await response.json()

  // 格式化成 ECharts 需要的数据 [{ name: '上海市', value: 3019 }, ...]
  const confirmedData = Object.entries(rawData).map(([province, stats]) => ({
    name: provinceMap[province] || province,
    value: stats['累计确诊人数'] || 0
  }))

  confirmedData.sort((a, b) => a.value - b.value)

  chartInstance.hideLoading()
  let current = true
  chartInstance.setOption(mapOption(confirmedData))

  // 每 5 秒切换地图与柱状图
  setInterval(() => {
    current = !current
    const option = current ? mapOption(confirmedData) : barOption(confirmedData)
    chartInstance.setOption(option, true)
  }, 5000)
})
</script>

<style scoped>
</style>
