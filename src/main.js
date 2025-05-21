import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import Echarts from './plugins/echarts.js';  // 导入 Echarts 插件
// 1. 引入 Ant Design Vue 及其样式
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/antd.css';

const app = createApp(App);

// 2. 注册 Ant Design Vue（必须在其他插件之前）
app.use(Antd);

// 3. 注册其他插件
app.use(router);  // 使用路由插件
app.use(Echarts); // 使用 Echarts 插件

app.mount('#app');







