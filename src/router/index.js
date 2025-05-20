import { createRouter, createWebHistory } from 'vue-router'
// 导入页面组件
import Overview from '@/views/Overview.vue'
import Global from '@/views/Global.vue'
import Domestic from '@/views/Domestic.vue'
import Search from '@/views/Search.vue'
import AMapCountryMap_TopBar from "@/components/AMapCountryMap_TopBar.vue";

const routes = [
    { path: '/', name: 'Overview', component: Overview },
    { path: '/global', name: 'Global', component: Global },
    { path: '/domestic', name: 'Domestic', component: Domestic },
    { path: '/search', name: 'Search', component: Search },
    {
        path: '/country/:name',
        name: 'AMapCountryMap_TopBar',
        component: AMapCountryMap_TopBar
    }
]

export default createRouter({
    history: createWebHistory(),
    routes,
})
