<template>
  <div ref="mapContainer" style="height: 100%; width: 100%" />
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted, ref } from 'vue'
import worldMap from 'echarts/map/json/world'  // 这里导入世界地图的GeoJSON文件

const mapContainer = ref(null)

onMounted(() => {
  const chart = echarts.init(mapContainer.value)

  // 注册世界地图
  echarts.registerMap('world', worldMap)

  // 设置地图选项
  chart.setOption({
    title: {text: '疫情地图（国内+国际）'},
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}'  // 显示城市名和数据
    },
    visualMap: {  // 视觉映射，显示数据的颜色变化
      min: 0,
      max: 1000000,
      left: 'left',
      top: 'bottom',
      text: ['High', 'Low'],
      calculable: true
    },
    series: [{
      name: '疫情地图',
      type: 'map',
      map: 'world', // 使用世界地图
      roam: true,  // 支持缩放
      label: {
        show: true,
      },
      data: [
        {name: 'China', value: 100000},   // 这里是中国的数据示例
        {name: 'USA', value: 300000},     // 这里是美国的数据示例
        {name: 'India', value: 200000},   // 这里是印度的数据示例
        {name: 'Brazil', value: 150000},  // 这里是巴西的数据示例
        // 添加其他国家的疫情数据
      ]
    }]
  })
})
</script>
