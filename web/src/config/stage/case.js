const caseRouter = {
    route: '/case/list',
    name: 'case',
    title: '用例管理',
    type: 'view', // 类型: folder, tab, view
    icon: 'iconfont icon-caseStore',
    filePath: 'view/case/case-list.vue', // 文件路径
    order: 2,
    inNav: true,
    permission: ['用例管理'],
    keepAlive: true,
  }
  
  export default caseRouter
  