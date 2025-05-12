// plugins/echarts.js
import * as echarts from 'echarts';  // 使用命名导入

const install = function (app) {
    // 将 $chart 挂载到全局属性上
    app.config.globalProperties.$chart = {
        line(dom, options) {
            const chart = echarts.init(dom);  // 创建图表实例
            chart.setOption(options);  // 设置图表的配置项
            return chart;  // 返回图表实例
        }
    };
};

export default install;