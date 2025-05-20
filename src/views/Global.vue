<template>
  <div class="international">
    <div class="navbar-class">
      <NavBar />
    </div>
    <div class="container">
      <h2>国际疫情指标</h2>
      <p>这里显示全球疫情的相关数据。</p>
      <div class="map-container" ref="chartContainer" style="height: 400px;">
        <p v-if="!isMapLoaded">地图加载中...</p>
      </div>
      <div class="data-container">
        <div class="data-box">
          <h3>全球每日死亡数</h3>
          <p>模拟数据：10000</p>
        </div>
        <div class="data-box">
          <h3>全球每日确诊数</h3>
          <p>模拟数据：50000</p>
        </div>
        <div class="data-box">
          <h3>全球总接种数量</h3>
          <p>模拟数据：3000000</p>
        </div>
        <div class="data-box">
          <h3>全球每日治愈数</h3>
          <p>模拟数据：45000</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import NavBar from "@/components/NavBar.vue";
import { ref, onMounted } from "vue";
import * as echarts from "echarts";
import worldMap from "@/assets/map/world.json"; // ✅ 使用你的路径

const chartContainer = ref(null);
const isMapLoaded = ref(false);

onMounted(() => {
  const chart = echarts.init(chartContainer.value);
  echarts.registerMap("world", worldMap);

  const option = {
    tooltip: {
      trigger: "item",
      formatter: "{b}: {c}"
    },
    visualMap: {
      min: 0,
      max: 100000,
      text: ["高", "低"],
      realtime: false,
      calculable: true,
      inRange: {
        color: ["#e0ffff", "#006edd"]
      }
    },
    series: [
      {
        name: "确诊数",
        type: "map",
        map: "world",
        roam: true,
        emphasis: {
          label: {
            show: true
          }
        },
        data: [
          { name: "United States", value: 20000 },
          { name: "India", value: 15000 },
          { name: "Brazil", value: 12000 },
          { name: "China", value: 800 },
          { name: "Russia", value: 9000 }
        ]
      }
    ]
  };

  chart.setOption(option);
  isMapLoaded.value = true;
});
</script>

<style scoped>
.navbar-class {
  padding: 40px 0;
}

.container {
  margin: 20px;
}

h2 {
  text-align: center;
}

.map-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  color: #666;
  background-color: #f0f0f0;
}

.data-container {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
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
  margin: 10px 0 0;
  color: #555;
}
</style>
