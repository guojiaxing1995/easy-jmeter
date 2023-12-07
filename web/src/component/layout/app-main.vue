<template>
  <el-scrollbar class="container" tag="section">
    <div class="wrapper" id="wrapper">
      <router-view v-slot="{ Component }" >
        <transition appear name="fade-transform" mode="out-in">
          <keep-alive>
            <component :is="Component" :key="$route.fullPath" v-if="$route.meta.keepAlive"/>
          </keep-alive>
        </transition>
        <transition appear name="fade-transform" mode="out-in">
          <component :is="Component" :key="$route.fullPath" v-if="!$route.meta.keepAlive"/>
        </transition>
      </router-view>
    </div>
  </el-scrollbar>
</template>

<script>
export default {
  name: 'AppMain',
}
</script>

<style lang="scss" scoped>
.container {
  .wrapper {
    width: 100%;
    height: 100%;
    text-align: left;
  }
}

</style>
