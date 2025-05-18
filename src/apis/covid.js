// apis/covid.js
import axios from 'axios';

export const fetchProvinceStats = async (params = {}) => {
  try {
    let query = [];
    if (params.day && params.day !== 'all' && params.month && params.month !== 'all') {
      const monthStr = params.month.toString().padStart(2, '0');
      const dayStr = params.day.toString().padStart(2, '0');
      query.push(`date=${params.year}-${monthStr}-${dayStr}`);
    } else if (params.year && params.year !== 'all') {
      query.push(`year=${params.year}`);
    }
    const queryString = query.length > 0 ? `?${query.join('&')}` : '';
    const url = `http://localhost:8081/api/stats/province${queryString}`;

    const response = await axios.get(url);
    return response.data;
  } catch (error) {
    console.error('请求疫情数据失败:', error);
    throw error;
  }
};

export const fetchNationalStats = async () => {
  try {
    const url = 'http://localhost:8081/api/stats/national';
    const response = await axios.get(url);
    return response.data;
  } catch (error) {
    console.error('请求全国疫情数据失败:', error);
    throw error;
  }
};