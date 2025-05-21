<template>
    <a-card title="国家疫情数据分析" :bordered="true" class="dashboard-card">
        <a-row :gutter="[16, 16]" align="middle">
            <!-- <a-col :span="6">
                <a-select v-model:value="selectedCountry" placeholder="请选择国家" @change="fetchData"
                    class="country-select">
                    <a-select-option value="China">中国</a-select-option>
                    <a-select-option value="USA">美国</a-select-option>
                    <a-select-option value="Italy">意大利</a-select-option>
                </a-select>
            </a-col> -->

            <a-col :span="14">
                <div class="date-range-container">
                    <a-space>
                        <a-date-picker v-model:value="startDate" placeholder="开始日期" format="YYYY-MM-DD"
                            @change="handleDateChange" class="date-picker" :disabled-date="disabledStartDate"
                            :inputReadOnly="true" :allowClear="false"
                            :getPopupContainer="trigger => trigger.parentNode" />
                        <span class="date-separator">至</span>
                        <a-date-picker v-model:value="endDate" placeholder="结束日期" format="YYYY-MM-DD"
                            @change="handleDateChange" class="date-picker" :disabled-date="disabledEndDate"
                            :inputReadOnly="true" :allowClear="false"
                            :getPopupContainer="trigger => trigger.parentNode" />
                        <a-button type="default" @click="resetDateRange" class="reset-btn"
                            :disabled="!startDate && !endDate">
                            <template #icon><reload-outlined /></template>
                            重置
                        </a-button>
                    </a-space>
                </div>
            </a-col>

            <a-col :span="4">
                <a-button type="primary" @click="applyDateRange" class="apply-btn" :disabled="!dateRangeValid">
                    <template #icon><check-outlined /></template>
                    应用
                </a-button>
            </a-col>
        </a-row>

        <a-divider class="divider" />

        <div class="chart-container">
            <div class="line-chart-box">
                <a-card title="疫情趋势" :bordered="false" class="chart-card">
                    <v-chart class="chart" :option="lineChartOptions" autoresize />
                </a-card>
            </div>
            <div class="bar-chart-box">
                <a-card title="疫情汇总" :bordered="false" class="chart-card">
                    <v-chart class="chart" :option="barChartOptions" autoresize />
                </a-card>
            </div>
        </div>
    </a-card>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import axios from 'axios'
import { useDateFormat } from '@vueuse/core'
import VChart from 'vue-echarts'
import { ReloadOutlined, CheckOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
const props = defineProps({
    country: {
        type: String,
        required: true
    }
})
const selectedCountry = ref(props.country || 'China')
const startDate = ref(null)
const endDate = ref(null)
const appliedRange = ref([])
const dailyStats = ref([])
const totalConfirmed = ref(0)
const totalDeaths = ref(0)
const totalVaccinated = ref(0)


// 修正后的 watch
watch(() => selectedCountry.value, (newVal) => {
    if (newVal) fetchData()
})

const dateRangeValid = computed(() => {
    return startDate.value && endDate.value && startDate.value <= endDate.value
})

const disabledStartDate = current => {
    return endDate.value && current > endDate.value
}

const disabledEndDate = current => {
    return startDate.value && current < startDate.value
}

const handleDateChange = () => {
    if (startDate.value && endDate.value && startDate.value > endDate.value) {
        const temp = startDate.value
        startDate.value = endDate.value
        endDate.value = temp
    }
}

const resetDateRange = () => {
    startDate.value = null
    endDate.value = null
    appliedRange.value = []
    fetchData()
}

const applyDateRange = () => {
    if (dateRangeValid.value) {
        appliedRange.value = [startDate.value, endDate.value]
        fetchData()
    }
}

const fetchData = async () => {
    if (!selectedCountry.value) return

    let url = `http://localhost:8081/api/globalstats/country?country=${selectedCountry.value}`

    if (appliedRange.value.length === 2) {
        const [start, end] = appliedRange.value
        url += `&start=${useDateFormat(start, 'YYYY-MM-DD').value}&end=${useDateFormat(end, 'YYYY-MM-DD').value}`
    }

    try {
        const { data } = await axios.get(url)
        const sortedStats = data.dailyStats.sort((a, b) => new Date(a.date) - new Date(b.date))
        dailyStats.value = sortedStats

        totalConfirmed.value = data.totalConfirmed
        totalDeaths.value = data.totalDeaths
        totalVaccinated.value = data.totalVaccinated
    } catch (error) {
        console.error('Error fetching data:', error)
    }
}

// Initial fetch
fetchData()

const displayStats = computed(() => {
    return appliedRange.value.length === 2 ? dailyStats.value : dailyStats.value.slice(-30)
})

const lineChartOptions = computed(() => ({
    tooltip: {
        trigger: 'axis',
        formatter: params => {
            const date = params[0].axisValue
            const confirmed = params[0].value
            const deaths = params[1].value
            const vaccinated = params[2].value

            return `
                <div style="font-weight: bold; margin-bottom: 5px">${date}</div>
                <div style="display: flex; align-items: center; margin: 3px 0">
                    <span style="display: inline-block; width: 10px; height: 10px; background: #f56c6c; margin-right: 5px"></span>
                    新增确诊: ${confirmed.toLocaleString()}
                </div>
                <div style="display: flex; align-items: center; margin: 3px 0">
                    <span style="display: inline-block; width: 10px; height: 10px; background: #909399; margin-right: 5px"></span>
                    新增死亡: ${deaths.toLocaleString()}
                </div>
                <div style="display: flex; align-items: center; margin: 3px 0">
                    <span style="display: inline-block; width: 10px; height: 10px; background: #67c23a; margin-right: 5px"></span>
                    疫苗接种: ${vaccinated.toLocaleString()}
                </div>
            `
        }
    },
    legend: {
        data: ['新增确诊', '新增死亡', '疫苗接种'],
        bottom: 0
    },
    grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        top: '10%',
        containLabel: true
    },
    xAxis: {
        type: 'category',
        data: displayStats.value.map(d => d.date),
        axisLabel: {
            rotate: 45,
            formatter: value => dayjs(value).format('MM-DD')
        },
        axisLine: {
            lineStyle: {
                color: '#d9d9d9'
            }
        }
    },
    yAxis: [
        {
            type: 'value',
            name: '新增确诊',
            position: 'left',
            axisLine: {
                show: true,
                lineStyle: {
                    color: '#f56c6c'
                }
            },
            splitLine: {
                lineStyle: {
                    type: 'dashed'
                }
            }
        },
        {
            type: 'value',
            name: '新增死亡',
            position: 'right',
            offset: 0,
            axisLine: {
                show: true,
                lineStyle: {
                    color: '#909399'
                }
            },
            splitLine: {
                show: false
            }
        },
        {
            type: 'value',
            name: '疫苗接种',
            position: 'right',
            offset: 60,
            axisLine: {
                show: true,
                lineStyle: {
                    color: '#67c23a'
                }
            },
            splitLine: {
                show: false
            }
        }
    ],
    series: [
        {
            name: '新增确诊',
            type: 'line',
            smooth: true,
            yAxisIndex: 0,
            data: displayStats.value.map(d => d.newConfirmed),
            lineStyle: {
                color: '#f56c6c',
                width: 3
            },
            itemStyle: {
                color: '#f56c6c'
            },
            symbol: 'circle',
            symbolSize: 6,
            areaStyle: {
                color: {
                    type: 'linear',
                    x: 0,
                    y: 0,
                    x2: 0,
                    y2: 1,
                    colorStops: [{
                        offset: 0,
                        color: 'rgba(245, 108, 108, 0.3)'
                    }, {
                        offset: 1,
                        color: 'rgba(245, 108, 108, 0)'
                    }]
                }
            }
        },
        {
            name: '新增死亡',
            type: 'line',
            smooth: true,
            yAxisIndex: 1,
            data: displayStats.value.map(d => d.newDeaths),
            lineStyle: {
                color: '#909399',
                width: 2
            },
            itemStyle: {
                color: '#909399'
            },
            symbol: 'circle',
            symbolSize: 6
        },
        {
            name: '疫苗接种',
            type: 'line',
            smooth: true,
            yAxisIndex: 2,
            data: displayStats.value.map(d => d.dailyVaccinated),
            lineStyle: {
                color: '#67c23a',
                width: 3
            },
            itemStyle: {
                color: '#67c23a'
            },
            symbol: 'circle',
            symbolSize: 6,
            areaStyle: {
                color: {
                    type: 'linear',
                    x: 0,
                    y: 0,
                    x2: 0,
                    y2: 1,
                    colorStops: [{
                        offset: 0,
                        color: 'rgba(103, 194, 58, 0.3)'
                    }, {
                        offset: 1,
                        color: 'rgba(103, 194, 58, 0)'
                    }]
                }
            }
        }
    ]
}))

const barChartOptions = computed(() => ({
    tooltip: {
        trigger: 'item',
        formatter: params => {
            const name = params.name
            const value = params.value.toLocaleString()
            const color = params.color
            const percent = ((params.value / Math.max(totalConfirmed.value, totalDeaths.value, totalVaccinated.value)) * 100).toFixed(1)

            return `
                <div style="font-weight: bold; margin-bottom: 5px">${name}</div>
                <div style="display: flex; align-items: center; margin: 3px 0">
                    <span style="display: inline-block; width: 10px; height: 10px; background: ${color}; margin-right: 5px"></span>
                    数值: ${value}
                </div>
                <div style="display: flex; align-items: center; margin: 3px 0">
                    <span style="display: inline-block; width: 10px; height: 10px; background: transparent; margin-right: 5px"></span>
                    占比: ${percent}%
                </div>
            `
        }
    },
    legend: {
        data: ['总确诊', '总死亡', '总接种'],
        bottom: 0
    },
    grid: {
        left: '3%',
        right: '8%',
        top: '10%',
        bottom: '10%',
        containLabel: true
    },
    xAxis: {
        type: 'value',
        axisLine: {
            lineStyle: {
                color: '#d9d9d9'
            }
        },
        splitLine: {
            lineStyle: {
                type: 'dashed'
            }
        },
        scale: true,
        axisLabel: {
            formatter: value => {
                if (value >= 1000000) return `${(value / 1000000).toFixed(1)}M`
                if (value >= 1000) return `${(value / 1000).toFixed(1)}K`
                return value
            }
        }
    },
    yAxis: {
        type: 'category',
        data: ['总确诊', '总死亡', '总接种'],
        axisLabel: {
            color: '#333',
            fontWeight: 'bold'
        },
        axisLine: {
            lineStyle: {
                color: '#d9d9d9'
            }
        }
    },
    series: [
        {
            name: '总数据',
            type: 'bar',
            data: [totalConfirmed.value, totalDeaths.value, totalVaccinated.value],
            barWidth: '40%',
            itemStyle: {
                color: function (params) {
                    const colors = ['#f56c6c', '#909399', '#67c23a']
                    return colors[params.dataIndex]
                },
                borderRadius: [4, 4, 4, 4]
            },
            label: {
                show: true,
                position: 'right',
                formatter: params => {
                    const value = params.value
                    if (value >= 1000000) return `${(value / 1000000).toFixed(1)}M`
                    if (value >= 1000) return `${(value / 1000).toFixed(1)}K`
                    return value.toLocaleString()
                },
                fontWeight: 'bold',
                color: '#333'
            },
            markLine: {
                silent: true,
                lineStyle: {
                    type: 'dashed',
                    color: '#ccc'
                },
                data: [{ type: 'max', name: '最大值' }]
            }
        }
    ]
}))

</script>

<style scoped>
.dashboard-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.country-select {
    width: 100%;
}

.date-range-container {
    display: flex;
    align-items: center;
    justify-content: flex-end;
}

.date-picker {
    width: 140px;
}

.date-separator {
    margin: 0 8px;
    color: rgba(0, 0, 0, 0.45);
}

.reset-btn {
    margin-left: 12px;
}

.apply-btn {
    width: 100%;
}

.divider {
    margin: 16px 0;
}

.chart-container {
    display: flex;
    gap: 16px;
    margin-top: 16px;
}

.line-chart-box {
    flex: 3;
    min-width: 0;
}

.bar-chart-box {
    flex: 1;
    min-width: 0;
}

.chart-card {
    height: 100%;
    border-radius: 8px;
    box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.06);
}

.chart {
    width: 100%;
    height: 400px;
}

@media (max-width: 992px) {
    .chart-container {
        flex-direction: column;
    }

    .line-chart-box,
    .bar-chart-box {
        flex: auto;
    }
}

/* 日期选择容器 */
.date-range-container {
    display: flex;
    align-items: center;
    justify-content: center;
    /* 修改为居中 */
    padding: 8px 12px;
    background: #f8f9fa;
    border-radius: 6px;
    transition: all 0.2s ease;
    margin: 0 auto;
    /* 添加自动外边距 */
    max-width: 900px;
    /* 添加最大宽度限制 */
}

.date-range-container:hover {
    background: #f1f3f5;
}

/* 日期选择器增强样式 */
.date-picker {
    width: 180px;
    border-radius: 6px;
    transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);

    /* 输入框内部样式 */
    :deep(.ant-picker-input) {
        height: 40px;
        padding: 0 12px;

        input {
            font-size: 14px;
            color: #2c3e50;
        }
    }

    /* 图标颜色 */
    :deep(.ant-picker-suffix) {
        color: #6c757d;
        margin-left: 8px;
    }

    /* 聚焦状态 */
    &:focus-within {
        box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);

        :deep(.ant-picker-suffix) {
            color: #1890ff;
        }
    }
}

/* 日期分隔符 */
.date-separator {
    margin: 0 12px;
    color: #495057;
    font-weight: 500;
    font-size: 15px;
    position: relative;
    top: 1px;
}

/* 按钮组优化 */
.reset-btn {
    margin-left: 16px;
    padding: 0 16px;
    height: 40px;
    border-radius: 6px;
    font-weight: 500;
    transition: all 0.3s;

    &:hover {
        color: #1890ff !important;
        border-color: #1890ff !important;
        background: rgba(24, 144, 255, 0.05);

        :deep(svg) {
            color: inherit;
        }
    }
}

.apply-btn {
    height: 40px;
    border-radius: 6px;
    font-weight: 500;
    transition: all 0.3s;
    box-shadow: 0 2px 8px -2px rgba(24, 144, 255, 0.3);

    &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px -2px rgba(24, 144, 255, 0.4);
    }

    /* 禁用状态 */
    &[disabled] {
        box-shadow: none;
        opacity: 0.6;
    }
}

/* 弹出面板美化 */
.date-picker :deep(.ant-picker-panel) {
    border-radius: 8px;
    box-shadow: 0 3px 6px -4px rgba(0, 0, 0, 0.12),
        0 6px 16px 0 rgba(0, 0, 0, 0.08),
        0 9px 28px 8px rgba(0, 0, 0, 0.05);

    /* 头部样式 */
    .ant-picker-header {
        padding: 12px 16px;
        border-bottom: 1px solid #f0f0f0;

        button {
            transition: color 0.3s;

            &:hover {
                color: #1890ff;
            }
        }
    }

    /* 日期单元格 */
    .ant-picker-cell {
        padding: 8px 0;

        &:hover .ant-picker-cell-inner {
            background: rgba(24, 144, 255, 0.1) !important;
        }

        &-in-view {
            color: #2c3e50;
        }

        &-today::before {
            border-color: #1890ff;
        }

        &-selected .ant-picker-cell-inner {
            background: #1890ff;
            color: white;
            border-radius: 4px;
        }
    }
}

/* 响应式调整 */
/* 移动端适配调整 */
@media (max-width: 768px) {
    .date-range-container {
        justify-content: flex-start;
        /* 小屏幕左对齐 */
        max-width: 100%;
        padding: 12px;
    }

    /* 保持原有移动端样式 */
    .date-picker {
        width: 100% !important;
        /* 全宽度 */
        margin-bottom: 8px;
    }

    .reset-btn,
    .apply-btn {
        width: 100%;
        margin-top: 8px;
    }
}
</style>