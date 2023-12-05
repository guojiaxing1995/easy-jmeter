const projectRouter = {
    route: '/project/list',
    name: 'project',
    title: '项目管理',
    type: 'view', // 类型: folder, tab, view
    icon: 'iconfont icon-project',
    filePath: 'view/project/project-list.vue', // 文件路径
    order: 4,
    inNav: true,
    permission: ['项目管理'],
  }
  
  export default projectRouter
  