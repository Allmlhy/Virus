<template>
  <div>
    <!-- 饼图展示 -->
    <div class="pie-chart-container" ref="pieChartContainer">
      <h1>疫情累计确诊、死亡、治愈数占比图</h1>

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
          <label>
            日：
            <select v-model="localParams.day" class="select-box">
              <option :value="undefined">全部</option>
              <option v-for="d in days" :key="d" :value="d">{{ d }}</option>
            </select>
          </label>
        </div>

        <div class="row">
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
          {
            value: data["死亡率"] || 0,
            name: "死亡率",
            itemStyle: { color: "#B0BEC5" } // 柔和灰蓝色
          },
          {
            value: data["治愈率"] || 0,
            name: "治愈率",
            itemStyle: { color: "#80CBC4" } // 青绿色
          },
          {
            value: data["在治率"] || 0,
            name: "在治率",
            itemStyle: { color: "#AED581" } // 黄绿色
          }
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
.pie-chart-container {
  max-width: 700px;
  background-color: #fff;
  margin-left: 30px;
  height: 700px;
  border-radius: 12px;
  color: #333;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  user-select: none;
}

h1 {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  color: #333; /* 改为深色字体 */
  margin-bottom: 15px;
}

label {
  font-size: 16px;
  font-weight: bold;
  color: #333; /* 改为深色字体 */
}

.loading {
  font-size: 18px;
  color: #333; /* 深色加载提示 */
  text-align: center;
  font-weight: bold;
}

.chart-box {
  width: 100%;
  height: 100%;
  border: 2px solid #ccc; /* 浅灰边框 */
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.row {
  display: flex;
  gap: 20px;
  text-align: center;
  justify-content: center;
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

.filter-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center; /* ⬅️ 让每一行居中 */
  margin: 10px 0;
}

.row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-start;
}

.form-item {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  display: flex;
  align-items: center;
  background: #f1f5f9;
  padding: 6px 10px;
  border-radius: 6px;
  box-shadow: inset 0 1px 2px rgba(0,0,0,0.05);
}

/* 基础统一样式，供所有select和input继承 */
/* 基础统一样式，供所有select和input继承 */
.select-box,
.input-box {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-color: #fff;
  border: 1.5px solid #4a90e2;   /* 主蓝色边框 */
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
  color: #1a3f72;               /* 深蓝色字体 */
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  box-sizing: border-box;
  outline: none;
  cursor: text;
}

/* input 额外宽度 */
.input-box {
  border-color: #7a9cc6;        /* 稍灰蓝 */
  color: #3d5a80;               /* 稍浅的深蓝 */
  width: 140px;                 /* 宽度加大 */
}


/* 只有下拉框有箭头和额外右侧padding */
.select-box {
  padding-right: 32px;
  cursor: pointer;
  background-image: url("data:image/svg+xml,%3Csvg fill='none' height='10' viewBox='0 0 24 24' width='10' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M7 10l5 5 5-5' stroke='%23a67cdb' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 12px 12px;
}


/* 悬停状态 */
.select-box:hover,
.input-box:hover {
  border-color: #3a7bd5; /* 明亮蓝色 */
  box-shadow: 0 0 6px rgba(58, 123, 213, 0.5);  /* 蓝色阴影 */
}

/* 聚焦状态 */
.select-box:focus,
.input-box:focus {
  border-color: #255aab; /* 深蓝色 */
  box-shadow: 0 0 8px rgba(37, 90, 171, 0.7);
  outline: none;
}

/* 输入框的placeholder颜色 */
input::placeholder {
  color: #8faeea; /* 浅蓝色 */
}

input:focus::placeholder {
  color: #3a7bd5; /* 明亮蓝 */
}
</style>