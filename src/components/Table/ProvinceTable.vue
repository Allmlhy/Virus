<script setup>
import { ref, onMounted, watch } from 'vue'
import { fetchProvinceStats } from '@/apis/covid'
import * as echarts from 'echarts'
import axios from 'axios'

const provinceData = ref({})
const provinces = ref([])

const cityStats = ref([]) // 市级数据
const selectedProvince = ref('') // 当前选中省，空代表全国视角

const selectedMetrics = ref(['totalConfirmed']) // 默认显示确诊数
const allMetrics = [
    { key: 'totalConfirmed', label: '确诊' },
    { key: 'totalRecovered', label: '治愈' },
    { key: 'totalDeaths', label: '死亡' },
    { key: 'totalImported', label: '输入' },
    { key: 'totalAsymptomatic', label: '无症状' },
    { key: 'currentConfirmed', label: '现有确诊' }
]

onMounted(async () => {
    try {
        const data = await fetchProvinceStats()
        provinceData.value = data
        provinces.value = Object.entries(data).map(([name, stats]) => ({
            name,
            confirmed: stats['累计确诊人数'],
            cured: stats['累计治愈人数'],
            dead: stats['累计死亡人数']
        }))
        drawProvinceChart()  // 页面初始绘制全国省份数据
    } catch (err) {
        console.error('获取省份数据失败:', err)
    }
})

const fetchCityStats = async (provinceName) => {
    try {
        selectedProvince.value = provinceName
        const res = await axios.get('http://localhost:8081/api/stats/city', {
            params: { province: provinceName }
        })
        cityStats.value = res.data
        drawChart()
    } catch (err) {
        console.error('获取城市数据失败:', err)
    }
}

// 绘制全国省份柱状图
const drawProvinceChart = () => {
    const chartDom = document.getElementById('city-chart')
    if (!chartDom) return

    const oldChart = echarts.getInstanceByDom(chartDom)
    if (oldChart) oldChart.dispose()

    const myChart = echarts.init(chartDom)

    const option = {
        tooltip: { trigger: 'axis' },
        legend: {
            data: selectedMetrics.value.map(m => allMetrics.find(i => i.key === m)?.label)
        },
        dataZoom: [
            { type: 'slider', start: 0, end: 100 },
            { type: 'inside' }
        ],
        xAxis: {
            type: 'category',
            data: provinces.value.map(item => item.name),
            axisLabel: {
                rotate: 45,
                fontSize: 10,
                interval: 0,
                formatter: (value) => value.length > 6 ? value.slice(0, 6) + '...' : value
            }
        },
        yAxis: { type: 'value' },
        series: selectedMetrics.value.map(metric => {
            // 省级数据中的字段名不同，需要映射对应字段
            return {
                name: allMetrics.find(i => i.key === metric)?.label,
                type: 'bar',
                data: provinces.value.map(item => {
                    // 映射对应字段
                    if (metric === 'totalConfirmed') return item.confirmed
                    if (metric === 'totalRecovered') return item.cured
                    if (metric === 'totalDeaths') return item.dead
                    // 省级数据没有输入、无症状、现有确诊字段，显示0或null
                    return 0
                }),
                barWidth: 12,
                barCategoryGap: '20%'
            }
        })
    }
    myChart.setOption(option)
}

// 绘制市级柱状图
const drawChart = () => {
    const chartDom = document.getElementById('city-chart')
    if (!chartDom) return

    const oldChart = echarts.getInstanceByDom(chartDom)
    if (oldChart) oldChart.dispose()

    const myChart = echarts.init(chartDom)

    const option = {
        tooltip: { trigger: 'axis' },
        legend: {
            data: selectedMetrics.value.map(m => allMetrics.find(i => i.key === m)?.label)
        },
        dataZoom: [
            { type: 'slider', start: 0, end: 100 },
            { type: 'inside' }
        ],
        xAxis: {
            type: 'category',
            data: cityStats.value.map(item => item.city),
            axisLabel: {
                rotate: 45,
                fontSize: 10,
                interval: 0,
                formatter: (value) => value.length > 6 ? value.slice(0, 6) + '...' : value
            }
        },
        yAxis: { type: 'value' },
        series: selectedMetrics.value.map(metric => ({
            name: allMetrics.find(i => i.key === metric)?.label,
            type: 'bar',
            data: cityStats.value.map(item => item[metric]),
            barWidth: 12,
            barCategoryGap: '20%'
        }))
    }

    myChart.setOption(option)
}

// 监听指标选择变化，如果是显示市级，则更新市级图；否则更新省级图
watch(selectedMetrics, () => {
    if (selectedProvince.value) {
        drawChart()
    } else {
        drawProvinceChart()
    }
})

</script>

<template>
    <div class="province-table">
        <!-- 表格区域 -->
        <div class="table-section">
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>省份</th>
                            <th>确诊</th>
                            <th>治愈</th>
                            <th>死亡</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="province in provinces" :key="province.name" @click="fetchCityStats(province.name)"
                            class="clickable-row">
                            <td>{{ province.name }}</td>
                            <td>{{ province.confirmed ?? '-' }}</td>
                            <td>{{ province.cured ?? '-' }}</td>
                            <td>{{ province.dead ?? '-' }}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- 图表区域始终展示 -->
        <div class="chart-section">
            <h3 v-if="selectedProvince">{{ selectedProvince }} - 城市疫情数据</h3>
            <h3 v-else>全国省份疫情数据</h3>

            <div class="metric-selector">
                <label v-for="m in allMetrics" :key="m.key" class="checkbox">
                    <input type="checkbox" v-model="selectedMetrics" :value="m.key"
                        @change="selectedProvince.value ? drawChart() : drawProvinceChart()" />
                    {{ m.label }}
                </label>
            </div>

            <div id="city-chart" style="width: 100%; height: 250px; margin-top: 16px;" />
        </div>
    </div>
</template>


<style scoped>
.province-table {
    padding: 24px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    max-width: 1000px;
    margin: 0 auto;
    font-family: 'Microsoft YaHei', sans-serif;
    display: flex;
    flex-direction: column;
    gap: 32px;
}

/* 表格区域固定高度、可滚动 */
.table-section {
    max-height: 300px;
    overflow-y: auto;
    border: 1px solid #ddd;
    border-radius: 12px;
}

/* 表格容器宽度自动 */
.table-container {
    min-width: 100%;
}

.table-container {
    max-height: 150px;
    /* 固定区域大小 */
    overflow-y: auto;
    border-radius: 12px;
    overflow-x: hidden;
}

/* 表格样式 */
table {
    width: 100%;
    border-collapse: collapse;
    background-color: #fbfbfb;
}

th,
td {
    padding: 12px 14px;
    border: 1px solid #ddd;
    text-align: center;
    font-size: 15px;
}

thead {
    background-color: #e3f2fd;
    color: #333;
    font-weight: bold;
}

.clickable-row {
    cursor: pointer;
    transition: background-color 0.3s ease, transform 0.2s ease;
}

.clickable-row:hover {
    background-color: #e0f7fa;
    transform: scale(1.01);
}

/* 图表区域 */
.chart-section {
    height: 370px;
    position: relative;
    background: #f9f9f9;
    padding: 16px;
    border-radius: 12px;
    box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.05);
}

.metric-selector {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-bottom: 12px;
}

.metric-selector .checkbox {
    font-size: 14px;
}
</style>
