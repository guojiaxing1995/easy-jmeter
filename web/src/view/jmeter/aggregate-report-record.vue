<template>
    <div class="container">
      <el-row :gutter="35" class="search">
        <el-col class="search-item" :span="8">
          <el-form-item label="项目" >
            <el-select v-model="search.project_id" style="width: 100%;" placeholder="请选择工程" filterable><el-option v-for="item in projects" :key="item.id" :label="item.name" :value="item.id"/></el-select>
          </el-form-item>
        </el-col>
        <el-col class="search-item" :span="8"><el-form-item label="事务"><el-input v-model="search.label" placeholder="请输入事务名称" clearable /></el-form-item></el-col>
        <el-col class="search-item" :span="8"><el-form-item label="备注"><el-input v-model="search.text" placeholder="请输入备注" clearable /></el-form-item></el-col>
      </el-row>
      <el-row :gutter="35" class="search">
        <el-col class="search-item" :span="8"></el-col>
        <el-col class="search-item" :span="8"></el-col>
        <el-col class="search-item search-btn" :span="8" >
          <el-button type="danger" @click="deleteDatas" class="btn">删除</el-button>
          <el-button type="primary" @click="getDatas" class="btn">查询</el-button>
        </el-col>
      </el-row>
      <el-table :data="datas" v-loading="loading" @selection-change="handleSelectionChange" :row-class-name="getRowClassName" :max-height="maxTableHeight" ref="tableRef">
        <el-table-column type="selection"/>
        <el-table-column label="工程" width="110" show-overflow-tooltip fixed="left">
            <template #default="scope">{{ getProjectName(scope.row.project_id) }}</template>
        </el-table-column>
        <el-table-column prop="label" label="事务" width="150" show-overflow-tooltip fixed="left"></el-table-column>
        <el-table-column prop="sample" label="sample" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="avg" label="avg" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="median" label="median" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="pct90" label="90%Line" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="pct95" label="95%Line" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="pct99" label="99%Line" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="min" label="min" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="max" label="max" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column label="error%" width="90" show-overflow-tooltip><template v-slot="scope">{{ (scope.row.error / scope.row.sample * 100).toFixed(2) }}%</template></el-table-column>
        <el-table-column prop="tps" label="tps" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="rb" label="received B/sec" width="125" show-overflow-tooltip></el-table-column>
        <el-table-column prop="sb" label="sent B/sec" width="105" show-overflow-tooltip></el-table-column>
        <el-table-column prop="text" label="备注" width="100" show-overflow-tooltip></el-table-column>
        <el-table-column label="测试时间" width="90" show-overflow-tooltip><template v-slot="scope">{{ scope.row.start_time }} {{ scope.row.end_time }}</template></el-table-column>
        <el-table-column label="归档时间" width="90" prop="create_time" show-overflow-tooltip></el-table-column>
      </el-table>
    </div>
  </template>
  
  <script>
    import Utils from 'lin/util/util'
    import { onMounted, ref, reactive, onActivated, onBeforeUnmount } from 'vue'
    import { get, post, _delete } from '@/lin/plugin/axios'
    import { ElMessageBox, ElMessage } from 'element-plus'
  
    export default {
      components: {
      },
      setup() {
        const datas = ref([])
        const loading = ref(false)
        const search = reactive({ project_id:'',label:'',text:''})
        const colorPool = ref(['#1abc9c', '#3498db', '#9b59b6', '#e74c3c', '#f1c40f'])
        const maxTableHeight = ref(0)
        const projects = ref([])
        const selectDate = ref([])
        const tableRef = ref(null);
        const ids = ref([]);
  
        const calculateMaxHeight = () => {
          const browserHeight = window.innerHeight || document.documentElement.clientHeight;
          const extraHeight = 285; 
          maxTableHeight.value = browserHeight - extraHeight;
        }
  
        onMounted(async () => {
          await getProjects()
          await getDatas()
          calculateMaxHeight()
          window.addEventListener('resize', calculateMaxHeight)
        })
  
        onBeforeUnmount(() => {
          window.removeEventListener('resize', calculateMaxHeight)
        })
  
        const handleSelectionChange = (selection) => {
            selectDate.value = selection
        }

        const deleteDatas = () => {
          if (selectDate.value.length === 0) {
            ElMessage({
                message: '请选择数据',
                type: 'warning'
            })
            return
          }
          let res
          ElMessageBox.confirm('此操作将永久删除数据, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }).then(async () => {
            loading.value = true
            ids.value = selectDate.value.map(item => item.id)
            res = await _delete(`/v1/task/aggregateReport/record/delete`, {ids: ids.value.toString()})
            getDatas()
            res.code < 9999 ? ElMessage.success(`${res.message}`) : 1
          }).catch(()=>{})
        }

        const getProjectName = (projectId) => {
            const project = projects.value.find(p => p.id === projectId);
            return project ? project.name : '未知项目';
        }
  
        const getProjects = async () => {
            let res
            try {
              res = await get('/v1/project/all', { showBackend: true })
              projects.value = res
              search.project_id = projects.value[0].id
            } catch (error) {
              projects.value = []
            }
          }
  
        const getDatas = async () => {
          let res
          try {
            loading.value = true
            res = await post('/v1/task/aggregateReport/record/search', search, { showBackend: true })
            datas.value = res
            loading.value = false
          } catch (error) {
            loading.value = false
            datas.value = []
          }
        }
  
        const getRowClassName = ({row, rowIndex}) => {
          const uniqueNames = Array.from(new Set(datas.value.map(r => r.project_id)))
          if (uniqueNames.length === 1) {
            const uniqueNames = Array.from(new Set(datas.value.map(r => r.label)))
            const colorIndex = uniqueNames.indexOf(row.label) % colorPool.value.length
            return `row-color-${colorIndex}`
          }
          const colorIndex = uniqueNames.indexOf(row.project_id) % colorPool.value.length
          return `row-color-${colorIndex}`
        }
  
        return {
          datas,
          loading,
          getDatas,
          search,
          colorPool,
          getRowClassName,
          maxTableHeight,
          getProjects,
          projects,
          handleSelectionChange,
          tableRef,
          getProjectName,
          deleteDatas,
          selectDate,
        }
      },
    }
  </script>
    
    <style lang="scss" scoped>
    .container {
      padding: 58px 30px 20px 30px;
        .search{
          margin: 0 10px;
        .search-item {
          height: 50px;
          line-height: 50px;
          vertical-align: middle;
        }
        .search-btn {
          text-align: right;
        }
        .btn {
          vertical-align: top;
        }
      }
  
      ::v-deep .is-vertical {
        width: 0;
        top: 2px;
      }
  
      ::v-deep .row-color-0 {
        background-color: hsla(160, 72%, 36%, 0.1);
      }
      ::v-deep tr.row-color-0 td {
        background-color: hsla(160, 72%, 36%, 0.1);
      }
      ::v-deep .row-color-1 {
        background-color: rgba(52, 152, 219, 0.1);
      }
      ::v-deep tr.row-color-1 td {
        background-color: rgba(52, 152, 219, 0.1);
      }
      ::v-deep .row-color-2 {
        background-color: rgba(155, 89, 182, 0.1);
      }
      ::v-deep tr.row-color-2 td {
        background-color: rgba(155, 89, 182, 0.1);
      }
      ::v-deep .row-color-3 {
        background-color: rgba(231, 76, 60, 0.1);
      }
      ::v-deep tr.row-color-3 td {
        background-color: rgba(231, 76, 60, 0.1);
      }
      ::v-deep .row-color-4 {
        background-color: rgba(241, 196, 15, 0.1);
      }
      ::v-deep tr.row-color-4 td {
        background-color: rgba(241, 196, 15, 0.1);
      }
      
    }
    </style>