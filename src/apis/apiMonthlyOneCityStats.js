import axios from 'axios';

export const fetchMonthlyOneCitySummary = async (params) => {
    console.log('fetchMonthlyOneCitySummary 请求参数:', params);  // 打印请求参数
    try {
        const res = await new Promise((resolve, reject) => {
            axios.get('http://localhost:8082/api/barchart/monthly_onecity/summary', { params })
                .then(response => {
                    console.log('fetchMonthlyOneCitySummary 返回数据:', response.data);  // 打印返回数据
                    resolve(response.data);
                })
                .catch(error => {
                    console.error('获取月度某市统计数据失败:', error);
                    reject(error);
                });
        });
        return res;
    } catch (error) {
        console.error('fetchMonthlyOneCitySummary 捕获异常:', error);
        throw error;
    }
};
