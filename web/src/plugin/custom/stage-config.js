const CustomRouter = {
  route: null,
  name: null,
  title: '自定义组件',
  type: 'folder',
  icon: 'iconfont icon-zidingyi',
  filePath: 'view/custom/',
  order: null,
  inNav: true,
  permission: ['自定义组件'],
  children: [
    {
      title: 'multiple 多重输入',
      type: 'view',
      name: 'Multiple',
      route: '/custom/multiple',
      filePath: 'plugin/custom/view/multiple-input.vue',
      inNav: true,
      icon: 'iconfont icon-multiple_inputs',
      permission: null,
    },
  ],
}

export default CustomRouter
