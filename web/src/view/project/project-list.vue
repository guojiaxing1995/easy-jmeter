<template>
  <div class="container" v-if="!showEdit">
    <div class="header">
      <div class="search">
        <el-input placeholder="请输入工程名称查询" v-model="name" clearable></el-input>
      </div>
    </div>
      <el-table :data="projects" v-loading="loading">
        <el-table-column prop="name" label="名称"></el-table-column>
        <el-table-column prop="count" label="用例个数"></el-table-column>
        <el-table-column prop="creator" label="创建人"></el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column prop="createtime" label="创建时间"></el-table-column>
        <el-table-column label="操作" fixed="right" width="275">
          <template #default="scope">
            <el-button plain size="small" type="primary" @click="handleEdit(scope.row.id)">用例</el-button>
            <el-button plain size="small" type="primary" @click="handleEdit(scope.row.id)">编辑</el-button>
            <el-button
              plain
              size="small"
              type="danger"
              @click="handleDelete(scope.row.id)"
              v-permission="{ permission: '删除图书', type: 'disabled' }"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          background :hide-on-single-page=true @current-change="handleCurrentChange" layout="prev, pager, next" :current-page="page" :page-size=10 :total="total">
        </el-pagination>
      </div>
  </div>
</template>

<script>
  import { onMounted, ref } from 'vue'
  import { get } from '@/lin/plugin/axios'

  export default {
    setup() {
      const showEdit = ref(false)
      const name = ref('')
      const projects = ref([])
      const total = ref(0)
      const page = ref(1)
      const pages = ref(0)
      const loading = ref(false)

      onMounted(() => {
        getProjects()
      })
      const getProjects = async () => {
        let res
        try {
          loading.value = true
          res = await get('/v1/project', { page: page.value, name: name.value }, { showBackend: true })
          console.log("res")
          projects.value = res.data
          total.value = res.total
          page.value = res.page
          pages.value = res.pages
          loading.value = false
        } catch (error) {
          loading.value = false
          projects.value = []
        }
      }

      return {
        projects,
        name,
        showEdit,
        total,
        page,
        pages,
        loading,
    }

    },
  }
</script>

<style lang="scss" scoped>
.container {
  padding: 0 30px;

  .header {
    display: flex;
    justify-content: flex-end;
    align-items: center;

    .search {
      height: 59px;
      line-height: 59px;
      color: $parent-title-color;
      font-size: 16px;
      font-weight: 500;
    }
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 30px;
    margin-bottom: 20px;
  }
}
</style>