<template>
  <div class="container">
    <!-- 地图容器 -->
    <div class="map-container" ref="mapContainer" style="height: 700px;">
      <p v-if="!isMapLoaded">地图加载中...</p>
    </div>
  </div>
</template>

<script setup>
import * as echarts from "echarts";
import worldMap from "@/assets/map/world.json"; // 使用世界地图的数据
import { ref, onMounted, defineEmits } from "vue";
import { fetchCountryConfirmedLevel } from "@/apis/fetchCountryConfirmedLevel"; // 引入你的请求函数

const mapContainer = ref(null);
const isMapLoaded = ref(false);
const emit = defineEmits(["countryClick"]);
const countryData = ref([]); // 存储所有国家的 level 数据

// 定义颜色映射
const levelColors = [
  "#66cc66", // level 0: 浅绿
  "#0000FF", // level 1: 绿
  "#ffcc00", // level 2: 黄
  "#ff9900", // level 3: 橙
  "#ff3300", // level 4: 红橙
  "#990000"  // level 5: 红
];

// 前十大面积国家
const topCountries = [
  "Russian Federation", "Canada", "United States", "China", "Brazil",
  "Australia", "India", "Argentina", "Kazakhstan", "Algeria"
];

// 监听地图加载
onMounted(async () => {
  const chart = echarts.init(mapContainer.value);
  echarts.registerMap("world", worldMap);

  // 获取国家确诊数据并更新地图
  try {
    const data = await fetchCountryConfirmedLevel({
      year: 2021,
      month: 9,
      day: 1
    });
    countryData.value = data;
    updateMap(chart);
  } catch (error) {
    console.error("加载国家疫情数据失败", error);
  }

  // 添加点击事件
  chart.on("click", (params) => {
    if (params.componentType === "series" && params.seriesType === "map") {
      emit("countryClick", params.name);
    }
  });

  isMapLoaded.value = true;
});

// 更新地图显示
function updateMap(chart) {
  const data = countryData.value;
  const seriesData = data.map(item => ({
    name: item.country_name,
    value: item.total_confirmed,
    level: item.level
  }));

  const option = {
    tooltip: {
      trigger: "item",
      formatter: "{b}: {c} (Level {d})"
    },
    visualMap: {
      min: 0,
      max: 5,
      text: ["高", "低"],
      realtime: false,
      calculable: true,
      inRange: {

        color: ["#66cc66", "#0000FF","#ffcc00","#ff9900","#ff3300","#990000"]
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
        data: seriesData.map(item => ({
          name: item.name,
          value: item.value,
          itemStyle: {
            color: levelColors[item.level] // 根据level设置颜色
          },
          label: {
            show: topCountries.includes(item.name), // 只在前十大面积国家上显示名字
            formatter: item.name, // 显示国家名称
            textStyle: {
              color: '#FFFFFF', // 设置字体颜色为白色
              fontWeight: 'bold', // 设置字体加粗
            }
          }
        }))
      }
    ]
  };

  chart.setOption(option);
}
</script>

<style scoped>
.map-container {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  color: #666;
  background-color: #f0f0f0;
}
</style>
