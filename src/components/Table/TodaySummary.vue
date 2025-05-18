<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { fetchNationalStats } from '@/apis/covid.js'

// 起始时间：2021-12-24 00:00:00
const startTime = new Date('2021-12-24T00:00:00')
// 当前显示时间，初始为起始时间
const now = ref(startTime.toLocaleString())

// 疫情数据状态
const summary = ref(null)
const loading = ref(false)
const error = ref(null)

// 定时器
let timers = []

// 封装请求函数，异步获取数据并处理状态
async function loadSummary() {
    loading.value = true
    error.value = null
    try {
        const data = await fetchNationalStats()
        summary.value = data
    } catch (err) {
        error.value = '获取数据失败，请稍后重试'
        console.error(err)
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    loadSummary()

    let simulatedTime = startTime.getTime()

    // 更新时间字符串每分钟刷新（模拟时间每分钟加一分钟）
    timers.push(setInterval(() => {
        simulatedTime += 60 * 1000 // 1分钟 = 60000毫秒
        now.value = new Date(simulatedTime).toLocaleString()
    }, 60 * 1000))

    // 每5分钟刷新一次疫情数据
    timers.push(setInterval(() => {
        loadSummary()
    }, 5 * 60 * 1000))
})

onUnmounted(() => {
    timers.forEach(timer => clearInterval(timer))
    timers = []
})
</script>


<template>
    <div class="today-summary">
        <h3>📅 今日疫情简报（更新时间：{{ now }}）</h3>

        <div v-if="loading" style="text-align:center; padding:20px;">加载中...</div>
        <div v-else-if="error" style="color: red; text-align:center; padding:20px;">{{ error }}</div>
        <div v-else-if="summary" class="summary-grid">
            <div class="item total-confirmed">
                <div class="label">累计确诊</div>
                <div class="value">{{ summary.totalConfirmed }}</div>
            </div>
            <div class="item total-deaths">
                <div class="label">累计死亡</div>
                <div class="value">{{ summary.totalDeaths }}</div>
            </div>
            <div class="item total-recovered">
                <div class="label">累计治愈</div>
                <div class="value">{{ summary.totalRecovered }}</div>
            </div>
            <div class="item total-imported">
                <div class="label">累计境外输入</div>
                <div class="value">{{ summary.totalImported }}</div>
            </div>
            <div class="item new-confirmed">
                <div class="label">新增确诊</div>
                <div class="value">{{ summary.newConfirmed }}</div>
            </div>
            <div class="item new-deaths">
                <div class="label">新增死亡</div>
                <div class="value">{{ summary.newDeaths }}</div>
            </div>
            <div class="item new-recovered">
                <div class="label">新增治愈</div>
                <div class="value">{{ summary.newRecovered }}</div>
            </div>
            <div class="item new-suspected">
                <div class="label">新增疑似</div>
                <div class="value">{{ summary.newSuspected }}</div>
            </div>
        </div>

        <div v-else style="text-align:center; padding:20px;">暂无数据</div>
    </div>
</template>

<style scoped>
.today-summary {
    background: #f9fbff;
    border-radius: 12px;
    padding: 24px 20px;
    margin-top: 24px;
    box-shadow: 0 8px 24px rgba(20, 40, 80, 0.12);
    font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
    color: #1a1f3d;
}

.today-summary h3 {
    margin-bottom: 20px;
    font-weight: 700;
    font-size: 1.4rem;
    color: #0d1a47;
    letter-spacing: 0.04em;
    display: flex;
    align-items: center;
    gap: 8px;
}

.summary-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
    gap: 18px 20px;
}

.item {
    background: #ffffff;
    border-radius: 10px;
    padding: 20px 16px;
    text-align: center;
    user-select: none;
    box-shadow: 0 3px 10px rgba(30, 60, 120, 0.08);
    transition: box-shadow 0.3s ease, transform 0.3s ease;
    cursor: default;
}

.item:hover {
    box-shadow: 0 6px 20px rgba(30, 60, 120, 0.2);
    transform: translateY(-4px);
}

.label {
    font-size: 15px;
    color: #6173a3;
    margin-bottom: 10px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.04em;
}

.value {
    font-size: 28px;
    font-weight: 700;
    color: #0d1a47;
    letter-spacing: 0.02em;
}

/* 蓝色调配色 */

.total-confirmed {
    background: #dbe8ff;
    color: #0b3d91;
    font-weight: 700;
}

.total-deaths {
    background: #c7d6f7;
    color: #223974;
}

.total-recovered {
    background: #d1e7ff;
    color: #1a3f8a;
}

.total-imported {
    background: #d9e8ff;
    color: #204a9a;
}

.new-confirmed {
    background: #c4dbff;
    color: #1651a0;
}

.new-deaths {
    background: #a9c3ff;
    color: #133e74;
}

.new-recovered {
    background: #b9d1ff;
    color: #175ba4;
}

.new-suspected {
    background: #e3eeff;
    color: #2a4c8a;
}

/* 响应式字体大小适配 */
@media (max-width: 600px) {
    .today-summary h3 {
        font-size: 1.1rem;
    }

    .value {
        font-size: 22px;
    }

    .summary-grid {
        grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
        gap: 14px 16px;
    }
}
</style>
