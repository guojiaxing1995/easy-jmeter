const taskRouter = {
    route: '/task/history',
    name: 'case',
    title: '测试记录',
    type: 'view', // 类型: folder, tab, view
    icon: 'iconfont icon-history',
    filePath: 'view/task/history-list.vue', // 文件路径
    order: 3,
    inNav: true,
    permission: ['测试记录'],
    keepAlive: true,
  }
  
  export default taskRouter