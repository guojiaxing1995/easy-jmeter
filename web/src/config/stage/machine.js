const machineRouter = {
    route: '/machine/list',
    name: 'machine',
    title: '压力机管理',
    type: 'view', // 类型: folder, tab, view
    icon: 'iconfont icon-machine',
    filePath: 'view/machine/machine-list.vue', // 文件路径
    order: 3,
    inNav: true,
    permission: ['压力机管理'],
  }
  
  export default machineRouter
  