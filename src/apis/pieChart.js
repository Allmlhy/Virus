import axios from 'axios';

export const fetchPieChartStats = async (params) => {
    console.log('fetchPieChartStats 请求参数:', params);  // 打印请求参数
    try {
        const res = await new Promise((resolve, reject) => {
            axios.get('http://localhost:8082/api/piechart/stats', { params })
                .then(response => {
                    console.log('fetchPieChartStats 返回数据:', response.data);  // 打印返回数据
                    resolve(response.data);
                })
                .catch(error => {
                    console.error('获取饼图数据失败:', error);
                    reject(error);
                });
        });
        return res;
    } catch (error) {
        console.error('fetchPieChartStats 捕获异常:', error);
        throw error;
    }
};
