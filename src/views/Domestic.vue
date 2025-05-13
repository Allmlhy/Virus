<template>
  <div class="domestic">
    <NavBar />
    <div class="container">
      <h2>国内疫情指标</h2>

      <div class="summary-container">
        <div class="summary-box">
          <p class="compare">较昨日 <span class="up">+30</span></p>
          <p class="number red">125073</p>
          <p class="label">累计确诊</p>
        </div>
        <div class="summary-box">
          <p class="compare">较昨日 <span class="up">+25</span></p>
          <p class="number orange">9303</p>
          <p class="label">现存疑似</p>
        </div>
        <div class="summary-box">
          <p class="compare">较昨日 <span class="down">-8</span></p>
          <p class="number blue">361</p>
          <p class="label">现存重症</p>
        </div>
        <div class="summary-box">
          <p class="compare">较昨日 <span class="same">0</span></p>
          <p class="number darkblue">800</p>
          <p class="label">累计死亡数</p>
        </div>
        <div class="summary-box">
          <p class="compare">较昨日 <span class="up">+20</span></p>
          <p class="number green">2500</p>
          <p class="label">累计治愈数</p>
        </div>
      </div>


<!--      <p>这里显示国内疫情的相关数据。</p>-->

      <div class="map-wrapper">
        <div class="map-container" ref="chartContainer">
          <p v-if="!isMapLoaded">地图加载中...</p>
        </div>
        <div class="right-placeholder">
          <!-- 可用于后续补充图表、说明等 -->
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import NavBar from "@/components/NavBar.vue";
import { ref, onMounted } from 'vue';
import * as echarts from 'echarts';
import chinaMap from '@/assets/map/china.json';

const chartContainer = ref(null);
const isMapLoaded = ref(false);

onMounted(() => {
  const chart = echarts.init(chartContainer.value);
  echarts.registerMap('china', chinaMap);

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}'
    },
    visualMap: {
      min: 0,
      max: 1000,
      left: 'left',
      top: 'bottom',
      text: ['高', '低'],
      calculable: true
    },
    series: [{
      name: '确诊数',
      type: 'map',
      map: 'china',
      label: {
        show: true
      },
      data: [
        { name: '北京', value: 123 },
        { name: '上海', value: 321 },
        { name: '广东', value: 654 }
      ]
    }]
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
.darkblue { color: #337ab7; }
.green { color: #5cb85c; }


.darkblue {
  color: #337ab7;
}

.green {
  color: #5cb85c;
}

.label {
  font-size: 16px;
  color: #333;
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
