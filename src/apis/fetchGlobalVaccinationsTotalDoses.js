import axios from 'axios';

export const fetchGlobalVaccinationsTotalDoses = async (params) => {
    console.log('fetchGlobalVaccinationsTotalDoses 请求参数:', params);  // 打印请求参数
    try {
        const res = await new Promise((resolve, reject) => {
            axios.get('http://localhost:8081/api/global-vaccinations/total-doses', { params })
                .then(response => {
                    console.log('fetchGlobalVaccinationsTotalDoses 返回数据:', response.data);  // 打印返回数据
                    resolve(response.data);
                })
                .catch(error => {
                    console.error('获取全球累计接种剂次数据失败:', error);
                    reject(error);
                });
        });
        return res;
    } catch (error) {
        console.error('fetchGlobalVaccinationsTotalDoses 捕获异常:', error);
        throw error;
    }
};
