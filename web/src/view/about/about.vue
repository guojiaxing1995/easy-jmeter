<template>
  <div class="container">
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  setup() {
    const showTeam = ref(false)
    const activeName = ref('first')
    const { clientWidth } = document.body

    onMounted(() => {
      if (clientWidth > 1200 && clientWidth < 1330) {
        showTeam.value = true
      } else {
        showTeam.value = false
      }
    })

    /**
     * 切换选项
     */
    const handleArticle = link => {
      window.open(link)
    }

    return {
      showTeam,
      activeName,
      handleArticle,
    }
  },
}
</script>

<style scoped lang="scss">
.container {
  padding: 20px;
}

@media screen and (max-width: 1200px) {
  .container {
    display: none;
  }
  .container {
    width: 100%;
  }
}

@media screen and (max-width: 1200px) {
  .container {
    width: 100%;
  }
}
</style>
