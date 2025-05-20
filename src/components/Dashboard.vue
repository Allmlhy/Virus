<template>
  <div class="dashboard">
    <DateAndStats
        v-model:selectedYear="selectedYear"
        v-model:selectedMonth="selectedMonth"
        v-model:selectedDay="selectedDay"
    />
    <BarChart
        :selectedYear="selectedYear"
        :selectedMonth="selectedMonth"
        :selectedDay="selectedDay"
        :key="`${selectedYear}-${selectedMonth}-${selectedDay}`"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import DateAndStats from './DateAndStats.vue';
import BarChart from './BarChart.vue';

const selectedYear = ref(2021);
const selectedMonth = ref(1);
const selectedDay = ref(1);
</script>
