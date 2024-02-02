<template>
    <div class="container" v-if="!showEdit">
      <div class="header">
        <div class="newBtn"><el-button type="primary" @click="handleCreate">新 增</el-button></div>
        <div class="header-right">
          <el-icon :size="25" @click="getMachines"><Refresh /></el-icon>
          <div class="search">
            <el-input placeholder="请输入压力机名称查询" v-model="name" clearable></el-input>
          </div>
        </div>
      </div>
      <el-table :data="machines" v-loading="loading" >
        <el-table-column prop="name" label="名称" width="300" show-overflow-tooltip></el-table-column>
        <el-table-column prop="address" label="地址" width="180"></el-table-column>
        <el-table-column prop="path" label="路径" width="350" show-overflow-tooltip></el-table-column>
        <el-table-column prop="version" label="版本" width="160" show-overflow-tooltip></el-table-column>
        <el-table-column label="压力机状态" width="130">
          <template #default="scope">
              <el-tag v-if="scope.row.is_online===false" type="danger">离线</el-tag>
              <el-tag v-else type="info">在线</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="jmeter_status.desc" label="jmeter状态" width="115"></el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
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
    <machine v-else @editClose="editClose" :editMachineId="editMachineId"></machine>
  </template>
  
  <script>
    import Utils from 'lin/util/util'
    import { onMounted, ref ,watch } from 'vue'
    import { get,_delete } from '@/lin/plugin/axios'
    import { ElMessageBox, ElMessage } from 'element-plus'
    import { Refresh } from '@element-plus/icons-vue'
    import Machine from './machine'
  
    export default {
      components: {
        Machine,
        Refresh,
      },
      setup() {
        const showEdit = ref(false)
        const name = ref('')
        const machines = ref([])
        const pageData = ref({ total: 0, page: 0 })
        const loading = ref(false)
        const editMachineId = ref(null)
  
        onMounted(() => {
          getMachines()
        })
  
        const getMachines = async () => {
          let res
          try {
            loading.value = true
            res = await get('/v1/machine', { page: pageData.value.page, name: name.value }, { showBackend: true })
            machines.value = res.items
            pageData.value.total = res.total
            pageData.value.page = res.page
            loading.value = false
          } catch (error) {
            loading.value = false
            machines.value = []
          }
        }
  
        const handleDelete = id => {
          let res
          ElMessageBox.confirm('此操作将永久删除该压力机, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }).then(async () => {
            loading.value = true
            res = await _delete(`/v1/machine/${id}`, { showBackend: true })
            pageData.value.page = 0
            getMachines()
            res.code < 9999 ? ElMessage.success(`${res.message}`) : 1
          }).catch(()=>{})
        }
  
        const handleCurrentChange = val => {
          pageData.value.page = val-1
          getMachines()
        }
  
        const editClose = () => {
          showEdit.value = false
          pageData.value.page = 0
          getMachines()
        }
  
        const handleEdit = id => {
          showEdit.value = true
          editMachineId.value = id
        }
  
        const handleCreate = () => {
          showEdit.value = true
          editMachineId.value = null
        }
        
        const _debounce =Utils.debounce(()=>{
          pageData.value.page = 0
          getMachines()
        }, 800)
  
        watch(name, () => {
          _debounce()
        })
  
        return {
          machines,
          name,
          showEdit,
          pageData,
          loading,
          editMachineId,
          handleCurrentChange,
          handleDelete,
          editClose,
          handleEdit,
          handleCreate,
          getMachines,
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
      
      .header-right{
        display: flex;
        align-items: center;

        .el-icon {
          cursor: pointer;
        }
        .search {
          height: 59px;
          line-height: 59px;
          color: $parent-title-color;
          font-size: 16px;
          font-weight: 500;
          margin-left: 15px;
        }
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