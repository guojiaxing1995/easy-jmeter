const hiddenRouter = {
    route: null,
    name: 'hidden',
    title: '',
    type: 'folder', // 类型: folder, tab, view
    icon: 'iconfont icon-iconset0103',
    filePath: 'view/hidden/', // 文件路径
    order: null,
    inNav: false,
    children: [
      {
        title: '测试详情',
        type: 'view',
        name: 'caseDetail',
        route: '/case/detail',
        filePath: 'view/case/detail.vue',
        inNav: false,
        icon: 'iconfont icon-iconset0103',
        keepAlive: true,
      },
    ],
  }
  
  export default hiddenRouter
  