import axios from 'axios';

export const fetchGlobalStatsTotal = async (params) => {
    console.log('fetchGlobalStatsTotal 请求参数:', params);  // 打印请求参数
    try {
        const res = await new Promise((resolve, reject) => {
            axios.get('http://localhost:8081/api/global-stats/total', { params })
                .then(response => {
                    console.log('fetchGlobalStatsTotal 返回数据:', response.data);  // 打印返回数据
                    resolve(response.data);
                })
                .catch(error => {
                    console.error('获取全球总统计数据失败:', error);
                    reject(error);
                });
        });
        return res;
    } catch (error) {
        console.error('fetchGlobalStatsTotal 捕获异常:', error);
        throw error;
    }
};



export const fetchGlobalStatsTopBottom = async (params) => {
    console.log('fetchGlobalStatsTopBottom 请求参数:', params);  // 打印请求参数
    try {
        const res = await new Promise((resolve, reject) => {
            axios.get('http://localhost:8081/api/global-stats/top-bottom', { params })
                .then(response => {
                    console.log('fetchGlobalStatsTopBottom 返回数据:', response.data);  // 打印返回数据
                    resolve(response.data);
                })
                .catch(error => {
                    console.error('获取全球统计数据失败:', error);
                    reject(error);
                });
        });
        return res;
    } catch (error) {
        console.error('fetchGlobalStatsTopBottom 捕获异常:', error);
        throw error;
    }
};


export const fetchGlobalStatsDailyChange = async (params) => {
    console.log('fetchGlobalStatsDailyChange 请求参数:', params);  // 打印请求参数
    try {
        const res = await new Promise((resolve, reject) => {
            axios.get('http://localhost:8081/api/global-stats/daily-change', { params })
                .then(response => {
                    console.log('fetchGlobalStatsDailyChange 返回数据:', response.data);  // 打印返回数据
                    resolve(response.data);
                })
                .catch(error => {
                    console.error('获取全球每日变化统计数据失败:', error);
                    reject(error);
                });
        });
        return res;
    } catch (error) {
        console.error('fetchGlobalStatsDailyChange 捕获异常:', error);
        throw error;
    }
};
