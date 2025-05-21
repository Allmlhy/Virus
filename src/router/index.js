import { createRouter, createWebHistory } from 'vue-router'
// 导入页面组件
import Overview from '@/views/Overview.vue'
import Global from '@/views/Global.vue'
import Domestic from '@/views/Domestic.vue'
import Search from '@/views/Search.vue'
import CountryPage from "@/views/CountryPage.vue";

const routes = [
    { path: '/', name: 'Overview', component: Overview },
    { path: '/global', name: 'Global', component: Global },
    { path: '/domestic', name: 'Domestic', component: Domestic },
    { path: '/search', name: 'Search', component: Search },
    {
        path: '/country/:name',
        name: 'CountryPage',
        component: CountryPage
    },
    {
        path: '/country/:name/:code?',
        name: 'CountryPage',
        component: CountryPage,
    },
]

export default createRouter({
    history: createWebHistory(),
    routes,
})
