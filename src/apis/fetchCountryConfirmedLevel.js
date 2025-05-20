import axios from 'axios';

export const fetchCountryConfirmedLevel = async (params) => {
    console.log('fetchCountryConfirmedLevel 请求参数:', params);  // 打印请求参数
    try {
        const res = await new Promise((resolve, reject) => {
            axios.get('http://localhost:8081/api/global-vaccinations/country-confirmed-level', { params })
                .then(response => {
                    console.log('fetchCountryConfirmedLevel 返回数据:', response.data);  // 打印返回数据
                    resolve(response.data);
                })
                .catch(error => {
                    console.error('获取国家疫情确认数据失败:', error);
                    reject(error);
                });
        });
        return res;
    } catch (error) {
        console.error('fetchCountryConfirmedLevel 捕获异常:', error);
        throw error;
    }
};
