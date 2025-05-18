<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts';
import chinaMap from '@/assets/map/china.json';

const props = defineProps({
    rawData: {
        type: Object,
        default: null
    }
});

const chartContainer = ref(null);
let chart = null;

const initChart = () => {
    if (!chartContainer.value) return;

    if (chart) {
        chart.dispose();
    }

    chart = echarts.init(chartContainer.value);
    echarts.registerMap('china', chinaMap);
};

const renderChart = (data) => {
    if (!chart || !data) return;

    const chartData = Object.entries(data).map(([province, stats]) => ({
        name: province,
        value: stats['累计确诊人数'] ?? 0,
        details: {
            累计确诊人数: stats['累计确诊人数'] ?? 0,
            累计死亡人数: stats['累计死亡人数'] ?? 0,
            累计治愈人数: stats['累计治愈人数'] ?? 0,
            累计境外输入人数: stats['累计境外输入人数'] ?? 0,
            新增确诊人数: stats['新增确诊人数'] ?? 0,
            新增死亡人数: stats['新增死亡人数'] ?? 0,
            新增治愈人数: stats['新增治愈人数'] ?? 0,
            新增疑似病例数: stats['新增疑似病例数'] ?? 0,
        }
    }));

    // 👉 动态计算 min 和 max，并限制范围
    const values = chartData.map(item => item.value);
    const maxValue = Math.min(Math.max(...values), 10000); // 最大值不超过 10000
    const minValue = Math.max(Math.min(...values), 0);   // 最小值不低于 100

    const option = {
        backgroundColor: '#f9f9f9',
        title: {
            text: '国内疫情地图',
            left: 'center',
            textStyle: {
                color: '#333',
                fontWeight: 'bold',
                fontSize: 22
            }
        },
        tooltip: {
            trigger: 'item',
            formatter: params => {
                if (!params.data) return `${params.name}: 无数据`;
                const d = params.data.details;
                return `
                    <strong>${params.name}</strong><br/>
                    累计确诊人数: ${d.累计确诊人数}<br/>
                    累计死亡人数: ${d.累计死亡人数}<br/>
                    累计治愈人数: ${d.累计治愈人数}<br/>
                    累计境外输入人数: ${d.累计境外输入人数}<br/>
                    新增确诊人数: ${d.新增确诊人数}<br/>
                    新增死亡人数: ${d.新增死亡人数}<br/>
                    新增治愈人数: ${d.新增治愈人数}<br/>
                    新增疑似病例数: ${d.新增疑似病例数}
                `;
            }
        },
        visualMap: {
            min: minValue,
            max: maxValue,
            left: 'left',
            bottom: 'bottom',
            text: ['高', '低'],
            calculable: true,
            inRange: {
                color: ['#e0f3f8', '#08589e']
            }
        },
        series: [{
            name: '累计确诊人数',
            type: 'map',
            map: 'china',
            roam: true,
            label: {
                show: true,
                fontSize: 10,
                color: '#000'
            },
            data: chartData,
            emphasis: {
                label: {
                    show: true,
                    color: '#fff',
                    fontWeight: 'bold'
                },
                itemStyle: {
                    areaColor: '#ff7f50'
                }
            },
            itemStyle: {
                borderColor: '#777',
                borderWidth: 1,
                areaColor: '#cde6f7'
            }
        }]
    };

    chart.setOption(option);
};


onMounted(() => {
    initChart();
    renderChart(props.rawData);
});

watch(() => props.rawData, (newData) => {
    renderChart(newData);
});

onBeforeUnmount(() => {
    if (chart) {
        chart.dispose();
    }
});
</script>

<template>
    <div class="map-container" ref="chartContainer" style="height: 500px;">
        <p v-if="!props.rawData">地图加载中...</p>
    </div>
</template>

<style scoped>
.map-container {
    margin-top: 25px;
    border: 1px solid #ddd;
    border-radius: 10px;
    box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.05);
    background: linear-gradient(135deg, #f5f9ff 0%, #d9e6ff 100%);
}
</style>
