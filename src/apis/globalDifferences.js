import axios from 'axios';

export const fetchGlobalDifferences = async (params) => {
    console.log('fetchGlobalDifferences 请求参数:', params);  // 打印请求参数
    try {
        // 直接返回 axios 请求的 Promise，不需要再用 new Promise 包装
        const res = await axios.get('http://localhost:8081/api/global-vaccinations/global-differences', { params });

        console.log('fetchGlobalDifferences 返回数据:', res.data);  // 打印返回数据
        return res.data;  // 返回数据
    } catch (error) {
        console.error('获取全球差异数据失败:', error);  // 打印错误
        throw new Error('获取全球差异数据失败');  // 抛出更具体的错误信息
    }
};
