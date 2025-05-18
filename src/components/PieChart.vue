<template>
  <div>
    <!-- 饼图展示 -->
    <div class="pie-chart-container" ref="pieChartContainer">
      <h1>疫情比例饼图</h1>

      <!-- 筛选控件 -->
      <div class="filter-container">
        <div class="row">
          <label>
            年：
            <select v-model="localParams.year" class="select-box">
              <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
            </select>
          </label>

          <label>
            月：
            <select v-model="localParams.month" class="select-box">
              <option :value="undefined">全部</option>
              <option v-for="m in months" :key="m" :value="m">{{ m }}</option>
            </select>
          </label>
        </div>

        <div class="row">
          <label>
            日：
            <select v-model="localParams.day" class="select-box">
              <option :value="undefined">全部</option>
              <option v-for="d in days" :key="d" :value="d">{{ d }}</option>
            </select>
          </label>

          <label>
            省份：
            <select v-model="localParams.province" class="select-box">
              <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
            </select>
          </label>

          <label>
            城市：
            <input type="text" v-model="localParams.city" class="input-box" placeholder="请输入城市名称" />
          </label>
        </div>
      </div>

      <div ref="chartRef" class="chart-box">
        <p v-show="!isPieChartLoaded" class="loading">饼图加载中...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, nextTick } from "vue";
import * as echarts from "echarts";
import { fetchPieChartStats } from "@/apis/pieChart";

// 双向绑定 props
const props = defineProps({
  queryParams: Object
});
const emit = defineEmits(["update:queryParams", "loading", "loaded"]);

const pieChartContainer = ref(null);
const chartRef = ref(null); // 新增一个 ref 用于 echarts 容器
const pieChart = ref(null);
const isPieChartLoaded = ref(false);

// 本地副本用于双向绑定
const localParams = reactive({ ...props.queryParams });

watch(
    () => ({ ...localParams }),
    (newParams) => {
      emit("update:queryParams", newParams);
    },
    { deep: true }
);

function renderPieChart(data) {
  if (!chartRef.value) return; // 使用 chartRef 作为 echarts 容器
  if (pieChart.value) pieChart.value.dispose();

  pieChart.value = echarts.init(chartRef.value);
  const option = {
    title: { text: "", left: "center", textStyle: { fontWeight: "bold", fontSize: 16, color: "#fff" } },
    tooltip: { trigger: "item" },
    legend: { bottom: 10, left: "center", textStyle: { color: "#fff" } },
    series: [
      {
        name: "比例",
        type: "pie",
        radius: "60%",
        data: [
          { value: data["死亡率"] || 0, name: "死亡率", itemStyle: { color: "#FF0000" } }, // 死亡率颜色改为红色
          { value: data["治愈率"] || 0, name: "治愈率", itemStyle: { color: "#32CD32" } }, // 治愈率颜色改为深绿色
          { value: data["在治率"] || 0, name: "在治率", itemStyle: { color: "#FFD700" } }  // 在治率颜色改为金色（稍微暗一点的黄色）
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: "rgba(0, 0, 0, 0.5)"
          }
        }
      }
    ]
  };

  pieChart.value.setOption(option);
  isPieChartLoaded.value = true;
  emit("loaded");
}

async function loadPieChartData() {
  isPieChartLoaded.value = false;
  emit("loading");
  try {
    await nextTick(); // 确保 DOM 更新完成
    const cleanedParams = { ...localParams };
    Object.keys(cleanedParams).forEach((key) => {
      if (cleanedParams[key] === undefined || cleanedParams[key] === "") {
        delete cleanedParams[key];
      }
    });

    const data = await fetchPieChartStats(cleanedParams);
    renderPieChart(data);
  } catch (error) {
    console.error("饼图加载失败:", error);
  }
}

// 监听参数变化并加载数据
watch(
    () => ({ ...localParams }),
    () => loadPieChartData(),
    { deep: true, immediate: true }
);

onMounted(() => {
  loadPieChartData();
});

// 静态下拉数据
const years = [2020, 2021, 2022, 2023, 2024];
const months = Array.from({ length: 12 }, (_, i) => i + 1);
const days = Array.from({ length: 31 }, (_, i) => i + 1);
const provinces = ["全国","北京", "天津", "上海", "重庆",
  "河北", "山西", "辽宁", "吉林", "黑龙江",
  "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南",
  "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南",
  "陕西", "甘肃", "青海", "台湾",
  "内蒙古", "广西", "西藏", "宁夏", "新疆",
  "香港", "澳门"];
</script>

<style scoped>
.loading {
  font-size: 18px;
  color: #ffffff;
  text-align: center;
  font-weight: bold;
}

.pie-chart-container {
  background-color: #002244; /* 蓝色背景 */
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-top: 20px;
  margin-bottom: 20px;
  position: relative; /* 保持容器相对定位 */
  width: 50%;
}

h1 {
  text-align: center;
  font-size: 24px; /* 增大标题字体 */
  font-weight: bold;
  color: #fff;
  margin-bottom: 15px; /* 将标题与筛选控件分开 */
}

.filter-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 20px; /* 保证筛选控件出现在标题正下方 */
  margin-bottom: 20px;
}

.row {
  display: flex;
  gap: 20px;
  justify-content: center;
}

label {
  font-size: 16px;
  font-weight: bold; /* 加粗 */
  color: #fff; /* 文字改为白色 */
}

.select-box,
.input-box {
  padding: 8px 12px;
  font-size: 14px;
  border-radius: 6px;
  border: 1px solid #ddd;
  outline: none;
  transition: all 0.3s;
  width: 100px; /* 宽度变窄 */
}

.select-box:hover,
.input-box:hover {
  border-color: #7fa7c1;
}

.select-box:focus,
.input-box:focus {
  border-color: #6c9bdb;
}

input::placeholder {
  color: #bbb;
}

input:focus::placeholder {
  color: #777;
}

select {
  background-color: #f5f5f5;
}

select:focus {
  background-color: #fff;
}

select,
input {
  margin: 0 10px;
}

select option {
  color: #333;
}

select option:disabled {
  color: #ff0000;
}

/* 饼图容器样式 */
.chart-box {
  width: 100%;
  height: 400px;
  border: 2px solid #fff; /* 添加白色边框 */
  border-radius: 8px; /* 添加圆角 */
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>