<template>
  <div>
    <!-- 疫情指标头部汇总 -->
    <div class="header-summary-wrapper">
      <div class="summary-left">
        <h2 class="summary-title">今日全球疫情指标速递</h2>

        <!-- 日期选择移动至此 -->
        <div class="date-picker-inline">
          <p class="update-time">截止时间：{{ formattedTime }}</p>
          <select v-model="selectedYear" id="year">
            <option v-for="year in years" :key="year" :value="year">{{ year }}</option>
          </select>
          <label for="year">年</label>

          <select v-model="selectedMonth" id="month">
            <option v-for="month in months" :key="month" :value="month">{{ month }}</option>
          </select>
          <label for="month">月</label>

          <select v-model="selectedDay" id="day">
            <option v-for="day in daysInMonth" :key="day" :value="day">{{ day }}</option>
          </select>
          <label for="day">日</label>
        </div>
      </div>

      <div class="summary-container">
        <div class="summary-box" v-for="(item, index) in summaryData" :key="index">
          <p class="compare">较昨日 <span :class="item.compareClass">{{ item.compareText }}</span></p>
          <p class="number" :class="item.colorClass">{{ item.number }}</p>
          <p class="label">{{ item.label }}</p>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, watch } from "vue";
import { fetchGlobalStatsTotal, fetchGlobalStatsTopBottom } from "@/apis/globalStats";
import { fetchGlobalVaccinationsTotalDoses } from "@/apis/fetchGlobalVaccinationsTotalDoses";
import { fetchGlobalDifferences } from "@/apis/globalDifferences";

// Emit 事件
const emit = defineEmits([
  'update:selectedYear',
  'update:selectedMonth',
  'update:selectedDay'
]);

// 日期选择
const selectedYear = ref(2021);
const selectedMonth = ref(1);
const selectedDay = ref(1);

const years = [2020, 2021, 2022, 2023, 2024, 2025];
const months = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
const daysInMonth = ref([]);

const calculateDaysInMonth = (year, month) => new Date(year, month, 0).getDate();

const updateDays = () => {
  const days = calculateDaysInMonth(selectedYear.value, selectedMonth.value);
  daysInMonth.value = Array.from({ length: days }, (_, i) => i + 1);
  if (selectedDay.value > days) selectedDay.value = days;
};

watch([selectedYear, selectedMonth], () => {
  updateDays();
  emit('update:selectedYear', selectedYear.value);
  emit('update:selectedMonth', selectedMonth.value);
}, { immediate: true });

watch(selectedDay, () => {
  emit('update:selectedDay', selectedDay.value);
});

// 数据
const globalStatsTotal = ref(null);
const globalVaccinationsTotalDoses = ref(null);
const globalDifferences = ref(null);
const globalStatsTopBottom = ref(null);
const isLoading = ref(true);

// 汇总展示数据
const summaryData = ref([
  {
    number: '加载中...',
    colorClass: 'green',
    label: '疫苗接种总量',
    compareText: '加载中...',
    compareClass: 'up',
  },
  {
    number: '加载中...',
    colorClass: 'red',
    label: '全球新增确诊',
    compareText: '加载中...',
    compareClass: 'down',
  },
  {
    number: '加载中...',
    colorClass: 'darkblue',
    label: '全球累计死亡数',
    compareText: '加载中...',
    compareClass: 'up',
  },
]);

const formattedTime = ref('');

// 获取数据
const fetchData = async () => {
  isLoading.value = true;

  const params = {
    year: selectedYear.value,
    month: selectedMonth.value,
    day: selectedDay.value,
  };

  try {
    const [statsData, vaccinationsData, differencesData, topBottomData] = await Promise.all([
      fetchGlobalStatsTotal(params),
      fetchGlobalVaccinationsTotalDoses(params),
      fetchGlobalDifferences(params),
      fetchGlobalStatsTopBottom(params)
    ]);

    globalStatsTotal.value = statsData;
    globalVaccinationsTotalDoses.value = vaccinationsData;
    globalDifferences.value = differencesData;
    globalStatsTopBottom.value = topBottomData;

    // 更新 summaryData
    summaryData.value = [
      {
        number: vaccinationsData?.total_doses || 'N/A',
        colorClass: 'green',
        label: '疫苗接种总量',
        compareText: `${differencesData?.new_doses_diff > 0 ? '+' : ''}${differencesData?.new_doses_diff || 0}`,
        compareClass: differencesData?.new_doses_diff > 0 ? 'up' : (differencesData?.new_doses_diff < 0 ? 'down' : 'same'),
      },
      {
        number: statsData?.total_confirmed || 'N/A',
        colorClass: 'red',
        label: '全球新增确诊',
        compareText: `${differencesData?.new_confirmed_diff > 0 ? '+' : ''}${differencesData?.new_confirmed_diff || 0}`,
        compareClass: differencesData?.new_confirmed_diff > 0 ? 'up' : (differencesData?.new_confirmed_diff < 0 ? 'down' : 'same'),
      },
      {
        number: statsData?.total_deaths || 'N/A',
        colorClass: 'darkblue',
        label: '全球累计死亡数',
        compareText: `${differencesData?.new_deaths_diff > 0 ? '+' : ''}${differencesData?.new_deaths_diff || 0}`,
        compareClass: differencesData?.new_deaths_diff > 0 ? 'up' : (differencesData?.new_deaths_diff < 0 ? 'down' : 'same'),
      }
    ];

    formattedTime.value = `${params.year}-${String(params.month).padStart(2, '0')}-${String(params.day).padStart(2, '0')} 23:59`;
  } catch (error) {
    console.error('数据加载失败:', error);
  } finally {
    isLoading.value = false;
  }
};

watch([selectedYear, selectedMonth, selectedDay], fetchData, { immediate: true });

const getColorStyle = (value) => {
  if (value > 0) return { color: 'red' };
  if (value < 0) return { color: 'green' };
  return { color: 'black' };
};
</script>

<style scoped>
/* 头部汇总样式 */
.header-summary-wrapper {
  display: flex;
  margin-bottom: 20px;
  justify-content: center;
  align-items: center;
  padding: 20px 24px 0;
  gap: 24px;
  flex-wrap: nowrap;
}

.summary-left {
  min-width: 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
}

.summary-title {
  font-size: 24px;
  font-weight: bold;
  color: #222;
  margin: 0;
  white-space: nowrap;
  line-height: 1;
}

.update-time {
  font-size: 13px;
  color: #999;
  margin-top: 20px;
}

.summary-container {
  display: flex;
  flex-wrap: nowrap;
  gap: 12px;
}

.summary-box {
  width: 130px;
  padding: 8px 6px;
  text-align: center;
  background: rgba(250, 250, 250, 0.92);
  border: 1px solid #ddd;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(2px);
  transition: box-shadow 0.3s ease;
}

.compare {
  font-size: 12px;
  color: #888;
  margin-bottom: 4px;
}

.up {
  color: #c9302c;
}

.down {
  color: #5bc0de;
}

.same {
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

.red {
  color: #c9302c;
}

.green {
  color: #5cb85c;
}

.darkblue {
  color: #337ab7;
}

.orange {
  color: #f0ad4e;
}

.blue {
  color: #5bc0de;
}

/* 原始样式 */
.data-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 20px;
  margin-bottom: 10px;
}

.data-box {
  background-color: #fff;
  border: 1px solid #ddd;
  padding: 10px;
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.data-box h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.data-box p {
  margin: 10px 0 0;
  color: #555;
  font-size: 24px;
  font-weight: bold;
}

.date-picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.date-picker label {
  margin: 0;
  font-weight: 500;
}

.date-picker select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .data-container {
    grid-template-columns: 1fr;
  }
}

.date-picker-inline {
  display: flex;
  align-items: center;
  gap: 4px;
  /* 缩小间距 */
  flex-wrap: wrap;
  font-size: 13px;
  /* 字体稍小 */
  color: #666;
  /* 灰色，和截止时间一致 */
}

.date-picker-inline select {
  padding: 4px 8px;
  /* 略微缩小内边距 */
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: #fff;
  font-size: 13px;
  /* 字体大小同步 */
  min-width: 70px;
  transition: border-color 0.2s ease;
  color: #333;
  /* 保持选择文字颜色稍深，方便阅读 */
}

.date-picker-inline select:focus {
  border-color: #5cb85c;
  outline: none;
  box-shadow: 0 0 0 2px rgba(92, 184, 92, 0.2);
}

.date-picker-inline label {
  margin-right: 6px;
  /* 略微缩小右边距 */
  color: #666;
  /* 灰色 */
  font-weight: 500;
  user-select: none;
}
</style>