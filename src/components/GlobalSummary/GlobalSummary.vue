<template>
  <div class="header-summary-wrapper">
    <div class="summary-left">
      <h2 class="summary-title">今日全球疫情指标速递</h2>
      <p class="update-time">截止时间：{{ formattedTime }}</p>
    </div>

    <div class="summary-container">
      <div class="summary-box" v-for="(item, index) in summaryData" :key="index">
        <p class="compare">较昨日 <span :class="item.compareClass">{{ item.compareText }}</span></p>
        <p class="number" :class="item.colorClass">{{ item.number }}</p>
        <p class="label">{{ item.label }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const summaryData = ref([
  {
    number: '14.3亿',
    colorClass: 'green',
    label: '疫苗接种总量',
    compareText: '+120万',
    compareClass: 'up',
  },
  {
    number: '158,734',
    colorClass: 'red',
    label: '全球新增确诊',
    compareText: '-4,213',
    compareClass: 'down',
  },
  {
    number: '6,940,502',
    colorClass: 'darkblue',
    label: '全球累计死亡数',
    compareText: '+1,040',
    compareClass: 'up',
  },
]);

const formattedTime = ref('2025-05-19 23:59');
</script>

<style scoped>
.header-summary-wrapper {
  display: flex;
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
  align-items: flex-start; /* 左对齐 */
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
  margin-top: 8px;
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

.red { color: #c9302c; }
.green { color: #5cb85c; }
.darkblue { color: #337ab7; }
.orange { color: #f0ad4e; }
.blue { color: #5bc0de; }
</style>
