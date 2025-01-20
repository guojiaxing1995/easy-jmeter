const jmeterRouter = {
    route: null,
    name: null,
    title: '聚合报告记录',
    type: 'tab', // 类型: folder, tab, view
    icon: 'iconfont icon-aggregateReport',
    filePath: 'view/jmeter/', // 文件路径
    order: null,
    inNav: true,
    permission: ['聚合报告记录'],
    children: [
      {
        title: '查询',
        type: 'view',
        name: 'aggregateReportSearch',
        route: '/jmeter/aggregateReport/search',
        filePath: 'view/jmeter/aggregate-report-search.vue',
        inNav: true,
        icon: 'iconfont icon-search',
        keepAlive: true,
      },
      {
        title: '记录',
        type: 'view',
        name: 'aggregateReportRecord',
        route: '/jmeter/aggregateReport/record',
        filePath: 'view/jmeter/aggregate-report-record.vue',
        inNav: true,
        icon: 'iconfont icon-record',
        keepAlive: true,
      },
    ],
  }
  
  export default jmeterRouter
  