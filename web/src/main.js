import 'dayjs/locale/zh-cn'
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import locale from 'element-plus/es/locale/lang/zh-cn'

import '@/config/global'
import 'lin/plugin'
import { filters } from 'lin/filter'
import permissionDirective from 'lin/directive/authorize'

import App from '@/app.vue'
import store from '@/store'
import router from '@/router'

import SocketIO from "socket.io-client"

import LinNotify from '@/component/notify'
import LIcon from '@/component/base/icon/lin-icon'
import StickyTop from '@/component/base/sticky-top/sticky-top'
import SourceCode from '@/component/base/source-code/source-code'

import '@/assets/style/index.scss'
import 'element-plus/dist/index.css'
import '@/assets/style/realize/element-variable.scss'

import echarts from '@/config/echarts'

const app = createApp(App)

app.use(store)
app.use(router)
app.use(ElementPlus, { locale })
app.use(LinNotify, {
  reconnection: true,
  reconnectionAttempts: 5,
  reconnectionDelay: 3000,
})

const socketio = SocketIO(process.env.VUE_APP_SOCKETIO_URL, {
  query: {
    "client-type": "web"
  }
})
app.component(socketio)
app.provide('socketio', socketio)
// base 组件注册
app.component('l-icon', LIcon)
app.component('sticky-top', StickyTop)
app.component('source-code', SourceCode)

app.config.globalProperties.$filters = filters
app.config.globalProperties.$echarts = echarts

app.directive('permission', permissionDirective)

app.mount('#app')

// 设置 App 实例
window.App = app
