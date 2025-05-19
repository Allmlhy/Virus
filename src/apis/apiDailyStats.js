import axios from 'axios';

export const fetchDailyStats = async (params) => {
    console.log('fetchDailyStats 请求参数:', params);  // 打印请求参数
    try {
        const res = await new Promise((resolve, reject) => {
            axios.get('http://localhost:8081/api/barchart/timeseries/dailyStats', { params })
                .then(response => {
                    console.log('fetchDailyStats 返回数据:', response.data);  // 打印返回数据
                    resolve(response.data);
                })
                .catch(error => {
                    console.error('获取日统计数据失败:', error);
                    reject(error);
                });
        });
        return res;
    } catch (error) {
        console.error('fetchDailyStats 捕获异常:', error);
        throw error;
    }
};
