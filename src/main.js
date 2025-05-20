import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import Echarts from './plugins/echarts.js';  // 导入 Echarts 插件


const app = createApp(App);

app.use(router);  // 使用路由插件
app.use(Echarts); // 使用 Echarts 插件

app.mount('#app');







