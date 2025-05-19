import axios from 'axios';

/**
 * 获取全国按月疫情汇总数据
 * @param {Object} params
 * @param {string} params.startMonth - 起始月份，格式如 '2020-02'
 * @param {string} params.endMonth - 结束月份，格式如 '2020-06'
 * @returns {Promise<Array>} 返回数组，元素包含 month、totalDeaths、totalRecovered、totalConfirmed 等字段
 */
export const fetchMonthlyWholeChinaSummary = async ({ startMonth, endMonth }) => {
    console.log('fetchMonthlyWholeChinaSummary 请求参数:', { startMonth, endMonth });

    try {
        const response = await axios.get('http://localhost:8081/api/barchart/monthly_wholechina/summary', {
            params: { startMonth, endMonth }
        });
        console.log('fetchMonthlyWholeChinaSummary 返回数据:', response.data);
        return response.data;
    } catch (error) {
        console.error('获取全国月度汇总数据失败:', error);
        throw error;
    }
};
