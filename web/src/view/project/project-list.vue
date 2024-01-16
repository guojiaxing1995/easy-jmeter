<template>
  <div class="container" v-if="!showEdit">
    <div class="header">
      <div class="newBtn"><el-button type="primary" @click="handleCreate">新 增</el-button></div>
      <div class="search">
        <el-input placeholder="请输入工程名称查询" v-model="name" clearable></el-input>
      </div>
    </div>
      <el-table :data="projects" v-loading="loading">
        <el-table-column prop="name" label="名称" width="300" show-overflow-tooltip></el-table-column>
        <el-table-column prop="case_num" label="用例条数" width="130"></el-table-column>
        <el-table-column prop="creator" label="创建人" width="160" show-overflow-tooltip></el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
        <el-table-column prop="create_time" label="创建时间" width="230"></el-table-column>
        <el-table-column label="操作" fixed="right" width="265">
          <template #default="scope">
            <el-button plain size="small" type="primary" @click="handleCase(scope.row.id)">用例</el-button>
            <el-button plain size="small" type="primary" @click="handleEdit(scope.row.id)">编辑</el-button>
            <el-button plain size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          background :hide-on-single-page=true @current-change="handleCurrentChange" layout="prev, pager, next" :current-page="pageData.page + 1" :page-size=10 :total="pageData.total">
        </el-pagination>
      </div>
  </div>
  <project v-else @editClose="editClose" :editProjectId="editProjectId"></project>
</template>

<script>
  import Utils from 'lin/util/util'
  import { onMounted, ref ,watch } from 'vue'
  import { useRouter } from "vue-router"
  import { get,_delete } from '@/lin/plugin/axios'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import Project from './project'

  export default {
    components: {
      Project,
    },
    setup() {
      const showEdit = ref(false)
      const name = ref('')
      const projects = ref([])
      const pageData = ref({ total: 0, page: 0 })
      const loading = ref(false)
      const editProjectId = ref(null)
      const router = useRouter()

      onMounted(() => {
        getProjects()
      })

      const getProjects = async () => {
        let res
        try {
          loading.value = true
          res = await get('/v1/project', { page: pageData.value.page, name: name.value }, { showBackend: true })
          projects.value = res.items
          pageData.value.total = res.total
          pageData.value.page = res.page
          loading.value = false
        } catch (error) {
          loading.value = false
          projects.value = []
        }
      }

      const handleDelete = id => {
        let res
        ElMessageBox.confirm('此操作将永久删除该项目及关联的所有用例, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(async () => {
          loading.value = true
          res = await _delete(`/v1/project/${id}`, { showBackend: true })
          pageData.value.page = 0
          getProjects()
          res.code < 9999 ? ElMessage.success(`${res.message}`) : 1
        }).catch(()=>{})
      }

      const handleCurrentChange = val => {
        pageData.value.page = val-1
        getProjects()
      }

      const editClose = () => {
        showEdit.value = false
        pageData.value.page = 0
        getProjects()
      }

      const handleEdit = id => {
        showEdit.value = true
        editProjectId.value = id
      }

      const handleCase = id => {
        router.push({ path: '/case/list', query: { projectId: id } }) 
      }

      const handleCreate = () => {
        showEdit.value = true
        editProjectId.value = null
      }
      
      const _debounce =Utils.debounce(()=>{
        pageData.value.page = 0
        getProjects()
      }, 800)

      watch(name, () => {
        _debounce()
      })

      return {
        projects,
        name,
        showEdit,
        pageData,
        loading,
        editProjectId,
        handleCurrentChange,
        handleDelete,
        editClose,
        handleEdit,
        handleCreate,
        handleCase,
    }

    },
  }
</script>

<style lang="scss" scoped>
.container {
  padding: 0 30px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 20px 0;

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

  ::v-deep .is-vertical {
      width: 0;
      top: 2px;
    }
}
</style>