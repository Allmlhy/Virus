<template>
  <div class="header-summary-wrapper">
    <div class="summary-left">
      <h2 class="summary-title">今日国内疫情指标速递</h2>
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
import { ref, onMounted } from 'vue';
import { fetchNationalStats } from '@/apis/covid.js';

const summaryData = ref([]);
const formattedTime = ref('');

onMounted(async () => {
  try {
    const res = await fetchNationalStats();

    summaryData.value = [
      {
        compareText: `+${res.newConfirmed}`,
        compareClass: res.newConfirmed === 0 ? 'same' : 'up',
        number: res.totalConfirmed,
        colorClass: 'red',
        label: '累计确诊',
      },
      {
        compareText: `+${res.newSuspected}`,
        compareClass: res.newSuspected === 0 ? 'same' : 'up',
        number: res.totalImported,
        colorClass: 'orange',
        label: '境外输入',
      },
      {
        compareText: `+${res.newDeaths}`,
        compareClass: res.newDeaths === 0 ? 'same' : 'up',
        number: res.totalDeaths,
        colorClass: 'darkblue',
        label: '累计死亡数',
      },
      {
        compareText: `+${res.newRecovered}`,
        compareClass: res.newRecovered === 0 ? 'same' : 'up',
        number: res.totalRecovered,
        colorClass: 'green',
        label: '累计治愈数',
      }
    ];

    const now = new Date();
    const pad = (n) => n.toString().padStart(2, '0');
    formattedTime.value = `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}`;
  } catch (error) {
    console.error('获取 summaryData 失败', error);
  }
});
</script>

<style scoped>
.header-summary-wrapper {
  display: flex;
  justify-content: center; /* 整体居中 */
  align-items: center;
  padding: 20px 24px 0;
  gap: 24px;
  flex-wrap: nowrap;
}

/* 竖直排列，左对齐 */
.summary-left {
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
  margin-top: 8px; /* 标题和时间之间间距 */
  margin-bottom: 0;
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
.orange { color: #f0ad4e; }
.blue { color: #5bc0de; }
.darkblue { color: #337ab7; }
.green { color: #5cb85c; }
</style>
