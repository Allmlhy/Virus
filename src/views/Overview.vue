<template>
  <div>
    <NavBar />
    <div class="container">
      <h2>疫情地图（国内+国际）</h2>
      <p>这是一个占位图，待后续数据加载。</p>
      <div class="map-container" ref="mapContainer">
        <p>占位地图，待加载...</p>
      </div>
      <div class="data-container">
        <div class="data-box">
          <h3>每日死亡数</h3>
          <p>模拟数据：1000</p>
        </div>
        <div class="data-box">
          <h3>每日确诊数</h3>
          <p>模拟数据：5000</p>
        </div>
        <div class="data-box">
          <h3>总接种数量</h3>
          <p>模拟数据：8000</p>
        </div>
        <div class="data-box">
          <h3>每日治愈数</h3>
          <p>模拟数据：3000</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import NavBar from "@/components/NavBar.vue";
import * as echarts from 'echarts'; // 引入echarts库

// 定义mapContainer的引用
const mapContainer = ref(null);

onMounted(() => {
  // 确保引用的DOM元素加载完成
  if (mapContainer.value) {
    // 初始化ECharts实例
    const myChart = echarts.init(mapContainer.value);

    // 设置图表的配置项
    const option = {
      title: {
        text: '疫情数据图表'
      },
      tooltip: {},
      legend: {
        data: ['每日确诊数', '每日死亡数']
      },
      xAxis: {
        data: ['2024-05-01', '2024-05-02', '2024-05-03', '2024-05-04', '2024-05-05']
      },
      yAxis: {},
      series: [
        {
          name: '每日确诊数',
          type: 'line',
          data: [5000, 5100, 5200, 5300, 5400]
        },
        {
          name: '每日死亡数',
          type: 'line',
          data: [1000, 1200, 1100, 1150, 1300]
        }
      ]
    };

    // 使用设置的配置项显示图表
    myChart.setOption(option);
  }
});
</script>

<style scoped>
.container {
  padding-top: 80px; /* 预留空间避免被 NavBar 遮挡 */
  margin: 0 20px;
}

h2 {
  text-align: center;
}

.map-container {
  height: 400px;
  background-color: #f0f0f0;
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  color: #666;
}

.data-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.data-box {
  background-color: #fff;
  border: 1px solid #ddd;
  padding: 10px;
  text-align: center;
}

.data-box h3 {
  margin: 0;
  color: #333;
}

.data-box p {
  margin-top: 10px;
  color: #555;
}
</style>
