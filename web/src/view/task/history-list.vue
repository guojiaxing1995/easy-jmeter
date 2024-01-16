<template>
  <div class="container">
    <el-row :gutter="35" class="search">
      <el-col class="search-item" :span="8"><el-form-item label="记录编号"><el-input v-model="search.task_id" placeholder="请输入测试编号" clearable /></el-form-item></el-col>
      <el-col class="search-item" :span="8"><el-form-item label="用例名称"><el-input v-model="search.jcase" placeholder="请输入用例名称" clearable /></el-form-item></el-col>
      <el-col class="search-item" :span="8"><el-form-item label="测试时间">
        <el-date-picker v-model="dateValue" type="datetimerange" range-separator="To" start-placeholder="开始时间" end-placeholder="结束时间" 
        clearable value-format="YYYY-MM-DD HH:mm:ss" @change="selectDate"/>
      </el-form-item></el-col>
    </el-row>
    <el-row :gutter="35" class="search">
      <el-col class="search-item" :span="8"><el-form-item label="测试结果">
        <el-select v-model="search.result"  placeholder="请选择测试结果" clearable style="width: 100%;">
          <el-option v-for="item in results" :key="item.value" :label="item.desc" :value="item.value"/>
        </el-select>
      </el-form-item></el-col>
      <el-col class="search-item" :span="8"></el-col>
      <el-col class="search-item search-btn" :span="8" >
        <el-button type="danger" @click="handleDelete" class="btn">批量删除</el-button>
        <el-button type="primary" @click="handleSearch" class="btn">查询</el-button>
      </el-col>
    </el-row>
    <el-table :data="historyData" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="task_id" label="测试编号" width="200" show-overflow-tooltip></el-table-column>
      <el-table-column prop="jmeter_case" label="用例名称" min-width="260" show-overflow-tooltip></el-table-column>
      <el-table-column prop="creator" label="测试人员" width="200" show-overflow-tooltip></el-table-column>
      <el-table-column prop="num_threads" label="并发数" width="150"></el-table-column>
      <el-table-column prop="duration" label="压测时长" width="150">
        <template #default="scope">
          {{ timeDeal(scope.row.duration) }}
        </template>
      </el-table-column>
      <el-table-column prop="create_time" label="测试时间" width="170"></el-table-column>
      <el-table-column prop="result.desc" label="测试结果" width="150">
        <template #default="scope">
          <el-text type="success" v-if="scope.row.result.value === 1">{{ scope.row.result.desc }}</el-text>
          <el-text type="danger" v-else>{{ scope.row.result.desc }}</el-text>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="100">
        <template #default="scope">
          <el-button plain size="small" type="primary" @click="handleEdit(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        background :hide-on-single-page=true @current-change="handleCurrentChange" layout="prev, pager, next" :current-page="pageData.page + 1" :page-size=10 :total="pageData.total">
      </el-pagination>
    </div>
  </div>
</template>

<script>
  import { reactive,ref,onMounted,onActivated,computed} from 'vue'
  import { get, post, _delete } from '@/lin/plugin/axios'
  import { useRouter } from "vue-router"
  import { ElMessageBox, ElMessage } from 'element-plus'

  export default {
    setup(props, context) {
      const results = ref([])
      const search = reactive({ task_id:'',jcase:'',result:'',start_time:'',end_time:'', page:0})
      const dateValue = ref('')
      const historyData = ref([])
      const loading = ref(false)
      const pageData = ref({ total: 0, page: 0 })
      const router = useRouter()
      const selectHistoryData = ref([])
      
      onMounted(() => {
        getResults()
      })

      onActivated(() => {
        if(history.state.case){
          search.jcase = history.state.case.caseName
        }
        handleSearch()
      })

      const getResults = async () => {
        const res = await get(`/v1/common/enum`, { showBackend: true })
        results.value = res.TaskResult
        for(let i=0;i<results.value.length;i++) {
            if(results.value[i].value == 0) {
                results.value.splice(i, 1)
                i--
            }
        }
      }

      const getHistory = async () => {
        loading.value = true
        search.page = pageData.value.page
        const res = await post(`/v1/task/history`, search, { showBackend: true })
        historyData.value = res.items
        pageData.value.total = res.total
        pageData.value.page = res.page
        loading.value = false
      }

      const handleCurrentChange = val => {
        pageData.value.page = val-1
        getHistory()
      }

      const handleSelectionChange = (val) => {
        selectHistoryData.value = val
      }

      const handleSearch = () => {
        pageData.value.page = 0
        getHistory()
      }

      const handleEdit = (data) => {
        router.push({
          path: '/case/detail',
          state: {detail: {caseId: data.case_id, taskId: data.task_id, latest: false}}
        })
      }

      const handleDelete = () => {
        let res
        ElMessageBox.confirm('此操作将永久删除所选记录, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(async () => {
          loading.value = true
          let ids = []
          for(let i=0;i<selectHistoryData.value.length;i++) {
            ids.push(selectHistoryData.value[i].id)
          }
          res = await _delete(`/v1/task/batch`, {ids: ids.toString()})
          pageData.value.page = 0
          getHistory()
          res.code < 9999 ? ElMessage.success(`${res.message}`) : 1
        }).catch(()=>{})
      }

      const selectDate = (date) => {
        if(date == null) {
            search.start_time = ''
            search.end_time = ''
          return
        }
        // 处理日期显示
        let originalDate = new Date(date[1])
        originalDate.setDate(originalDate.getDate() + 1)
        originalDate.setUTCHours(23, 59, 59)
        let modifiedDateTimeString = originalDate.toISOString().slice(0, 19).replace('T', ' ')
        dateValue.value[1] = modifiedDateTimeString

        search.start_time = dateValue.value[0]
        search.end_time = dateValue.value[1]
      }

      const timeDeal = computed(() => (time) => {
        const hours = Math.floor(time / 3600)
        const minutes = Math.floor((time % 3600) / 60)
        const seconds = time % 60
        let result = ""
        if (hours > 0) {
            result += `${hours} hours `
        }
        if (minutes > 0) {
            result += `${minutes} minutes `
        }
        if (seconds > 0) {
            result += `${seconds} seconds`
        }
        return result.replace(/hours/g, '小时').replace(/minutes/g, '分钟').replace(/seconds/g, '秒')
      })

      return {
        results,
        getResults,
        search,
        dateValue,
        selectDate,
        historyData,
        getHistory,
        loading,
        pageData,
        handleCurrentChange,
        handleSearch,
        handleEdit,
        handleSelectionChange,
        selectHistoryData,
        handleDelete,
        timeDeal,
      }
    }
  }
</script>

<style lang="scss" scoped>
.container {
    padding: 20px 30px 0 30px;
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