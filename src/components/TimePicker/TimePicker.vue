<template>
    <div class="time-picker">
        <!-- 年份选择 -->
        <select v-model="selectedYear" @change="handleYearChange">
            <option value="all">全部年份</option>
            <option v-for="year in years" :key="year" :value="year">{{ year }}</option>
        </select>

        <!-- 月份选择 -->
        <select v-model="selectedMonth" @change="handleMonthChange" :disabled="!selectedYear || selectedYear === 'all'">
            <option value="all">全部月份</option>
            <option v-for="month in months" :key="month" :value="month">{{ month }}月</option>
        </select>

        <!-- 日期选择 -->
        <select v-model="selectedDay" :disabled="!selectedMonth || selectedMonth === 'all'">
            <option value="all">全部日期</option>
            <option v-for="day in days" :key="day" :value="day">{{ day }}日</option>
        </select>
    </div>
</template>

<script setup>
import { ref, watchEffect, onBeforeMount } from 'vue'
import { getDateStructure } from '@/apis/date'

const dateStructure = ref({})
const years = ref([])
const months = ref([])
const days = ref([])

const selectedYear = ref('all')
const selectedMonth = ref('all')
const selectedDay = ref('all')

const emit = defineEmits(['time-change'])

const fetchDateStructure = async () => {
    try {
        const response = await getDateStructure()
        console.log(response)
        if (!response || typeof response !== 'object') {
            console.error('后端返回的数据格式错误:', response.data)
            return
        }
        dateStructure.value = response
        years.value = Object.keys(dateStructure.value).sort()
    } catch (error) {
        console.error('获取日期结构失败', error)
    }
}

const handleYearChange = () => {
    selectedMonth.value = 'all'
    selectedDay.value = 'all'
    months.value = []

    if (selectedYear.value === 'all') return

    const yearData = dateStructure.value[selectedYear.value]
    if (yearData && typeof yearData === 'object') {
        months.value = Object.keys(yearData).map(m => Number(m)).sort((a, b) => a - b)
    }
}

const handleMonthChange = () => {
    selectedDay.value = 'all'
    days.value = []

    if (selectedYear.value === 'all' || selectedMonth.value === 'all') return

    const monthData = dateStructure.value[selectedYear.value]?.[selectedMonth.value]
    if (Array.isArray(monthData)) {
        days.value = monthData
    }
}

watchEffect(() => {
    emit('time-change', {
        year: selectedYear.value,
        month: selectedMonth.value,
        day: selectedDay.value
    });
});


onBeforeMount(() => {
    fetchDateStructure()
})

defineExpose({
    selectedYear,
    selectedMonth,
    selectedDay
})
</script>

<style scoped>
.time-picker {
    display: flex;
    gap: 1rem;
    padding: 1rem;
    background-color: #f5faff;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 123, 255, 0.1);
    flex-wrap: wrap;
    justify-content: center;
}

.time-picker select {
    padding: 8px 12px;
    border: 1px solid #cce0ff;
    border-radius: 8px;
    background-color: white;
    color: #333;
    font-size: 16px;
    min-width: 120px;
    outline: none;
    transition: all 0.2s ease;
}

.time-picker select:disabled {
    background-color: #eef4fb;
    color: #aaa;
    border-color: #dde6f2;
}

.time-picker select:hover:not(:disabled),
.time-picker select:focus:not(:disabled) {
    border-color: #3399ff;
    box-shadow: 0 0 6px rgba(51, 153, 255, 0.3);
}
</style>
