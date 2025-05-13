<template>
  <div class="international">
    <NavBar />
    <div class="container">
      <h2>国际疫情指标</h2>

      <div class="summary-container">
        <div class="summary-box">
          <p class="compare">较昨日 <span class="up">+100</span></p>
          <p class="number red">1000000</p>
          <p class="label">全球累计死亡数</p>
        </div>
        <div class="summary-box">
          <p class="compare">较昨日 <span class="up">+5000</span></p>
          <p class="number orange">50000000</p>
          <p class="label">全球累计确诊数</p>
        </div>
        <div class="summary-box">
          <p class="compare">较昨日 <span class="up">+2000000</span></p>
          <p class="number blue">300000000</p>
          <p class="label">全球累计接种数量</p>
        </div>
      </div>

      <div class="map-wrapper">
        <div class="map-container" ref="chartContainer">
          <p v-if="!isMapLoaded">地图加载中...</p>
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
.container {
  padding-top: 60px;
  margin: 0 20px;
}

h2 {
  text-align: center;
}

.summary-container {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 10px 0;
  margin: 10px 0 20px;
  border-top: 1px solid #eaeaea;
  border-bottom: 1px solid #eaeaea;
}

.summary-box {
  width: 130px;
  padding: 8px 6px;
  text-align: center;
  background: #fafafa;
  border: 1px solid #eee;
  border-radius: 6px;
}

.compare {
  font-size: 12px;
  color: #888;
  margin-bottom: 4px;
}

.compare .up {
  color: #c9302c;
}

.compare .down {
  color: #5bc0de;
}

.compare .same {
  color: #999;
}

.number {
  font-size: 18px;
  font-weight: bold;
  margin: 2px 0;
}

.label {
  font-size: 13px;
  color: #333;
}

.red { color: #c9302c; }
.orange { color: #f0ad4e; }
.blue { color: #5bc0de; }

.map-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.map-container {
  width: 100%;
  height: 400px;
  background-color: #f0f0f0;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  color: #666;
}

.right-placeholder {
  flex-grow: 1;
}
</style>
