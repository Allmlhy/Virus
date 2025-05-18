import axios from 'axios'

export const getDateStructure = async () => {
  console.log('getDateStructure 请求开始')
  try {
    const res = await new Promise((resolve, reject) => {
      axios.get('http://localhost:8081/api/date/structure')
        .then(response => {
          console.log('getDateStructure 返回数据:', response.data)  // 打印返回数据
          resolve(response.data)
        })
        .catch(error => {
          console.error('获取日期结构失败:', error)
          reject(error)
        })
    })
    return res
  } catch (error) {
    console.error('getDateStructure 捕获异常:', error)
    throw error
  }
}
