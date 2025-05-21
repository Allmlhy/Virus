<template>
  <div class="search">
    <NavBar />
    <div class="container">

      <div class="china-map-wrapper">
        <ChinaPopulationMap />
      </div>
      <!-- 省市选择及按钮 -->
      <div class="test-select">
        <label for="province">选择省份：</label>
        <select v-model="selectedProvince" id="province">
          <option disabled value="">请选择省份</option>
          <option v-for="prov in provinces" :key="prov">{{ prov }}</option>
        </select>

        <select v-model="selectedCity" id="city" @change="onCityChange">
          <option disabled value="">请选择城市</option>
          <option v-for="city in cities" :key="city">{{ city }}</option>
        </select>

        <!-- 模型选择 -->
        <label for="model" style="margin-left:20px;">预测模型：</label>
        <select id="model" v-model="selectedModel">
          <option value="tcn">TCN</option>
          <option value="lstm">LSTM</option>
          <option value="gru">GRU</option>
          <option value="transformer">Transformer</option>
        </select>

        <!-- 开始预测按钮 -->
        <button class="predict-btn" @click="predictData" :disabled="!selectedProvince || !selectedCity">
          开始预测
        </button>
      </div>

      <!-- 上传状态 -->
      <p v-if="uploadStatus" class="upload-status">{{ uploadStatus }}</p>
      <!-- 总趋势图 -->
      <div class="chart-section">
        <h3>疫情趋势图</h3>
        <!-- 骨架屏或加载提示（疫情趋势图） -->
        <div v-if="!selectedProvince || !selectedCity" class="chart-placeholder">
          <div class="skeleton-chart">
            <div class="spinner"></div>
            <p>请先选择省份和城市以查看疫情趋势图</p>
          </div>
        </div>
        <div v-else id="mainChart" style="width: 100%; height: 400px;"></div>
      </div>

      <!-- 每日新增图 -->
      <div class="chart-section">
        <h3>每日新增确诊与死亡</h3>
        <!-- 骨架屏或加载提示（每日新增图） -->
        <div v-if="!selectedProvince || !selectedCity" class="chart-placeholder">
          <div class="skeleton-chart">
            <div class="spinner"></div>
            <p>请先选择省份和城市以查看每日新增图</p>
          </div>
        </div>

        <div v-else id="dailyChart" style="width: 100%; height: 400px;"></div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import axios from "axios";
import * as echarts from "echarts";

import NavBar from "@/components/NavBar.vue";
import ChinaPopulationMap from "@/components/ChinaPopulationMap.vue";

const provinces = ref([]);
const cities = ref([]);
const selectedProvince = ref("");
const selectedCity = ref("");
const selectedModel = ref("tcn");
const uploadStatus = ref("");
const locationData = ref([]);

let mainChart = null;
let dailyChart = null;

// 获取省市数据
const fetchLocations = async () => {
  try {
    const response = await axios.get("http://localhost:8081/api/locations");
    locationData.value = response.data;
    provinces.value = [...new Set(locationData.value.map(loc => loc.province))];
  } catch (error) {
    console.error("获取省市数据失败：", error);
  }
};

// 监听省份变化，更新城市列表，并清空图表
watch(selectedProvince, (newProvince) => {
  if (newProvince) {
    cities.value = locationData.value
      .filter(loc => loc.province === newProvince)
      .map(loc => loc.city);
  } else {
    cities.value = [];
  }
  selectedCity.value = "";
  clearMainChart();
  clearDailyChart();
});

const onCityChange = () => {
  clearMainChart();
  clearDailyChart();
  loadChartData();
  loadDailyChartData();
};

const clearMainChart = () => {
  if (mainChart) mainChart.clear();
};

const clearDailyChart = () => {
  if (dailyChart) dailyChart.clear();
};

const loadChartData = () => {
  if (!selectedProvince.value || !selectedCity.value) {
    clearMainChart();
    return;
  }

  axios.get("http://localhost:8081/api/region-stats/history", {
    params: { province: selectedProvince.value, city: selectedCity.value },
  }).then((res) => {
    const historyData = res.data;
    const dates = historyData.map(i => i.date);
    const confirmed = historyData.map(i => i.totalConfirmed);
    const deaths = historyData.map(i => i.totalDeaths);
    const recovered = historyData.map(i => i.totalRecovered);

    const option = {
      animationDuration: 10000,
      title: { text: "疫情发展趋势" },
      tooltip: { trigger: "axis" },
      legend: { data: ["确诊", "死亡", "治愈"] },
      xAxis: { type: "category", data: dates },
      yAxis: { type: "value" },
      series: [
        { name: "确诊", type: "line", data: confirmed },
        { name: "死亡", type: "line", data: deaths },
        { name: "治愈", type: "line", data: recovered },
      ],
      dataZoom: [
        { type: 'slider', start: 80, end: 100 },
        { type: 'inside', start: 80, end: 100 }
      ]
    };

    if (!mainChart) mainChart = echarts.init(document.getElementById("mainChart"));
    mainChart.setOption(option);
  }).catch(console.error);
};

const loadDailyChartData = () => {
  if (!selectedProvince.value || !selectedCity.value) {
    clearDailyChart();
    return;
  }

  axios.get("http://localhost:8081/api/region-stats/daily", {
    params: { province: selectedProvince.value, city: selectedCity.value },
  }).then((res) => {
    renderDailyChart(res.data);
  }).catch(console.error);
};

const predictData = async () => {
  if (!selectedProvince.value || !selectedCity.value) {
    uploadStatus.value = "请先选择省份和城市！";
    return;
  }

  try {
    uploadStatus.value = "正在准备预测数据...";
    const res = await axios.get("http://localhost:8081/api/region-stats/daily", {
      params: { province: selectedProvince.value, city: selectedCity.value },
    });
    const dailyData = res.data;
    const lastTwoWeeks = dailyData.slice(-14).map(d => ({
      date: d.date,
      newConfirmed: d.newConfirmed,
      newDeaths: d.newDeaths,
    }));

    const modelUrlMap = {
      lstm: "http://localhost:8000/predict_lstm/",
      tcn: "http://localhost:8000/predict_tcn/",
      gru: "http://localhost:8000/predict_gru/",
      transformer: "http://localhost:8000/predict_transformer/",
    };

    const predictUrl = modelUrlMap[selectedModel.value] || modelUrlMap["lstm"];
    const predictRes = await axios.post(predictUrl, lastTwoWeeks);

    if (predictRes.status === 200 && Array.isArray(predictRes.data)) {
      uploadStatus.value = "预测成功！";
      const updatedData = [...dailyData];
      predictRes.data.forEach(pred => {
        updatedData.push({
          date: pred.date,
          newConfirmed: pred.predictedConfirmed,
          newDeaths: pred.predictedDeaths
        });
      });
      renderDailyChart(updatedData);
    } else {
      uploadStatus.value = "预测失败，接口返回异常";
    }
  } catch (e) {
    uploadStatus.value = "预测失败，发生错误";
    console.error(e);
  }
};

const renderDailyChart = (dailyData) => {
  const dates = dailyData.map(d => d.date);
  const newConfirmed = dailyData.map(d => d.newConfirmed);
  const newDeaths = dailyData.map(d => d.newDeaths);
  const lastIndex = 704;

  const option = {
    animationDuration: 10000,
    title: { text: "每日新增确诊与死亡（含预测）" },
    tooltip: { trigger: "axis" },
    legend: { data: ["新增确诊", "新增死亡"] },
    xAxis: { type: "category", data: dates },
    yAxis: { type: "value" },
    series: [
      {
        name: "新增确诊",
        type: "line",
        data: newConfirmed,
        markLine: {
          symbol: 'none',
          lineStyle: {
            type: 'dashed',
            color: '#888'
          },
          data: [
            { xAxis: dates[lastIndex] }
          ]
        }
      },
      {
        name: "新增死亡",
        type: "line",
        data: newDeaths
      }
    ],
    dataZoom: [
      { type: 'slider', start: 80, end: 100 },
      { type: 'inside', start: 80, end: 100 }
    ]
  };

  if (!dailyChart) dailyChart = echarts.init(document.getElementById("dailyChart"));
  dailyChart.setOption(option);
};

onMounted(fetchLocations);
</script>

<style scoped>
.search {
  background-color: #f5f7fa;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  padding-bottom: 50px;
  min-height: 100vh;
  color: #333;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.china-map-wrapper {
  margin-top: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  overflow: hidden;
  background-color: #fff;
  padding: 15px;
}

/* 选择区域卡片样式 */
.test-select {
  margin-top: 20px;
  background: #fff;
  padding: 20px 25px;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.15);
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 30px;
  user-select: none;
}

.test-select label {
  font-weight: 600;
  color: #3a3a3a;
  white-space: nowrap;
}

.test-select select {
  min-width: 140px;
  padding: 8px 12px;
  border: 1.8px solid #d0d7e6;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s ease;
  cursor: pointer;
}

.test-select select:hover {
  border-color: #409eff;
}

.test-select select:focus {
  outline: none;
  border-color: #0066ff;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.3);
}

.predict-btn {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  padding: 10px 28px;
  border: none;
  font-weight: 700;
  font-size: 16px;
  border-radius: 8px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transition: background 0.3s ease, box-shadow 0.3s ease;
  user-select: none;
}

.predict-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #337acc 0%, #5599ff 100%);
  box-shadow: 0 6px 20px rgba(51, 122, 204, 0.6);
}

.predict-btn:disabled {
  background: #c2c9d9;
  cursor: not-allowed;
  box-shadow: none;
  color: #7a7f8a;
}

/* 上传状态提示 */
.upload-status {
  font-size: 14px;
  color: #555;
  margin-bottom: 15px;
  font-style: italic;
  user-select: text;
}

/* 图表区域 */
.chart-section {
  margin-top: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.1);
  padding: 25px 30px;
  user-select: none;
}

.chart-section h3 {
  margin-bottom: 20px;
  font-weight: 700;
  color: #222;
  border-left: 4px solid #409eff;
  padding-left: 12px;
  font-size: 22px;
}

/* 骨架屏与加载提示 */
.chart-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
  background-color: #e6ecf7;
  border-radius: 12px;
  border: 2px dashed #a6b1d1;
  color: #637096;
  font-size: 18px;
  text-align: center;
  flex-direction: column;
  animation: fadeIn 0.4s ease-in-out;
  user-select: none;
}

.skeleton-chart .spinner {
  margin: 0 auto;
  border: 6px solid #dce3f2;
  border-top: 6px solid #409eff;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  animation: spin 1.2s linear infinite;
  margin-bottom: 12px;
}

/* 动画 */
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.97);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .test-select {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }

  .test-select label,
  .test-select select,
  .predict-btn {
    width: 100%;
  }

  .predict-btn {
    padding: 12px 0;
    font-size: 18px;
  }

  .chart-section h3 {
    font-size: 20px;
  }
}
</style>
