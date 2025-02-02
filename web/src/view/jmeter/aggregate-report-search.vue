<template>
    <div class="container">
      <el-row :gutter="35" class="search">
        <el-col class="search-item" :span="8"><el-form-item label="应用名称"><el-input v-model="search.application" placeholder="请输入 application" clearable /></el-form-item></el-col>
        <el-col class="search-item" :span="8"><el-form-item label="标记名称"><el-input v-model="search.tags" placeholder="请输入 tags" clearable /></el-form-item></el-col>
        <el-col class="search-item" :span="8"><el-form-item label="测试时间">
            <el-date-picker v-model="dateValue" type="datetimerange" range-separator="To" start-placeholder="开始时间" end-placeholder="结束时间" 
            clearable value-format="YYYY-MM-DD HH:mm:ss" @change="selectDate"/></el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="35" class="search">
        <el-col class="search-item" :span="8"><el-form-item label="测试备注">
          <el-input v-model="search.text" placeholder="请输入 textTitle" clearable />
        </el-form-item></el-col>
        <el-col class="search-item" :span="8"></el-col>
        <el-col class="search-item search-btn" :span="8" >
          <el-button type="success" @click="archiveDialogShow" class="btn">归档</el-button>
          <el-button type="primary" @click="getDatas" class="btn">查询</el-button>
        </el-col>
      </el-row>
      <el-table :data="datas" v-loading="loading" @selection-change="handleSelectionChange" :row-class-name="getRowClassName" :max-height="maxTableHeight" ref="tableRef">
        <el-table-column type="selection"/>
        <el-table-column prop="application" label="application" width="110" show-overflow-tooltip fixed="left"></el-table-column>
        <el-table-column prop="tags" label="tags" width="150" show-overflow-tooltip fixed="left"></el-table-column>
        <el-table-column prop="transaction" label="label" width="150" show-overflow-tooltip fixed="left"></el-table-column>
        <el-table-column prop="sample" label="sample" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="avg" label="avg" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="median" label="median" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="pct90.0" label="90%Line" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="pct95.0" label="95%Line" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="pct99.0" label="99%Line" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="min" label="min" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="max" label="max" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column label="error%" width="90" show-overflow-tooltip><template v-slot="scope">{{ (scope.row.error / scope.row.sample * 100).toFixed(2) }}%</template></el-table-column>
        <el-table-column prop="tps" label="tps" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="rb" label="received B/sec" width="125" show-overflow-tooltip></el-table-column>
        <el-table-column prop="sb" label="sent B/sec" width="105" show-overflow-tooltip></el-table-column>
        <el-table-column prop="text" label="text" width="100" show-overflow-tooltip></el-table-column>
        <el-table-column label="time" width="90" show-overflow-tooltip><template v-slot="scope">{{ scope.row.startTime }} {{ scope.row.endTime }}</template></el-table-column>
      </el-table>
      <el-dialog v-model="archiveVisible" title="数据归档" width="30%"> 
        <el-form :model="archiveModel" ref="form">
          <el-form-item label="项目" label-width="60px" prop="text">
            <el-select v-model="archiveModel.project_id" placeholder="请选择工程" filterable style="width: 100%;">
                <el-option v-for="item in projects" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="备注" label-width="60px" prop="text">
            <el-input placeholder="请输入备注" v-model="archiveModel.text" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer"> <el-button type="primary" @click="submitForm">确定</el-button></span>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script>
    import Utils from 'lin/util/util'
    import { onMounted, ref, reactive, onActivated, onBeforeUnmount } from 'vue'
    import { get, post } from '@/lin/plugin/axios'
    import { ElMessageBox, ElMessage } from 'element-plus'
  
    export default {
      components: {
      },
      setup() {
        const datas = ref([])
        const loading = ref(false)
        const dateValue = ref([])
        const search = reactive({ application:'',tags:'',text:'',start_time:'',end_time:''})
        const colorPool = ref(['#1abc9c', '#3498db', '#9b59b6', '#e74c3c', '#f1c40f'])
        const maxTableHeight = ref(0)
        const archiveVisible = ref(false)
        const archiveModel = reactive({ text:'', project_id: '',aggregate_reports:[]})
        const projects = ref([])
        const tableRef = ref(null);
  
        const calculateMaxHeight = () => {
          const browserHeight = window.innerHeight || document.documentElement.clientHeight;
          const extraHeight = 285; 
          maxTableHeight.value = browserHeight - extraHeight;
        }
  
        onMounted(() => {
          setDefaultDateRange()
          getDatas()
          calculateMaxHeight()
          window.addEventListener('resize', calculateMaxHeight)
        })
  
        onBeforeUnmount(() => {
          window.removeEventListener('resize', calculateMaxHeight)
        })
  
        const submitForm = async() => {
          for (let i = 0; i < archiveModel.aggregate_reports.length; i++) {
            archiveModel.aggregate_reports[i].pct90=archiveModel.aggregate_reports[i]["pct90.0"];
            archiveModel.aggregate_reports[i].pct95=archiveModel.aggregate_reports[i]["pct95.0"];
            archiveModel.aggregate_reports[i].pct99=archiveModel.aggregate_reports[i]["pct99.0"];
            archiveModel.aggregate_reports[i].start_time=archiveModel.aggregate_reports[i].startTime;
            archiveModel.aggregate_reports[i].end_time=archiveModel.aggregate_reports[i].endTime;
            archiveModel.aggregate_reports[i].label=archiveModel.aggregate_reports[i].transaction;
          }
          let res
          try {
            res = await post('/v1/task/aggregateReport/archive', archiveModel , { showBackend: true })
            archiveVisible.value = false
            archiveModel.aggregate_reports = []
            ElMessage.success(`${res.message}`)
            tableRef.value.clearSelection()
          
          } catch (error) {
            console.log(error);
          }
  
        }
  
        const handleSelectionChange = (selection) => {
          archiveModel.aggregate_reports = selection
        }
  
        const archiveDialogShow = () => {
          if (archiveModel.aggregate_reports.length === 0) {
            ElMessage({
              message: '请选择数据',
              type: 'warning'
            })
            return
          }
          archiveVisible.value = true
          archiveModel.text = ''
          getProjects()
        }
  
        const getProjects = async () => {
            let res
            try {
              res = await get('/v1/project/all', { showBackend: true })
              projects.value = res
              archiveModel.project_id = projects.value[0].id
            } catch (error) {
              projects.value = []
            }
          }
  
        const getDatas = async () => {
          let res
          try {
            loading.value = true
            res = await post('/v1/task/aggregateReport/search', search, { showBackend: true })
            datas.value = res
            loading.value = false
          } catch (error) {
            loading.value = false
            datas.value = []
          }
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
  
        const setDefaultDateRange = async () => {
            const today = new Date()
            const startOfDay = new Date(today)
            startOfDay.setUTCHours(0, 0, 0, 0) // 设置为今天的零点
  
            const endOfDay = new Date(today);
            endOfDay.setUTCHours(23, 59, 59, 999) // 设置为今天的23:59:59
            
            dateValue.value[0] = startOfDay.toISOString().slice(0, 19).replace('T', ' ')
            dateValue.value[1] = endOfDay.toISOString().slice(0, 19).replace('T', ' ')
            search.start_time = dateValue.value[0]
            search.end_time = dateValue.value[1]
        }
  
        const getRowClassName = ({row, rowIndex}) => {
          const uniqueNames = Array.from(new Set(datas.value.map(r => r.application)))
          if (uniqueNames.length === 1) {
            const uniqueNames = Array.from(new Set(datas.value.map(r => r.tags)))
            if (uniqueNames.length === 1) {
              const uniqueNames = Array.from(new Set(datas.value.map(r => r.transaction)))
              const colorIndex = uniqueNames.indexOf(row.transaction) % colorPool.value.length
              return `row-color-${colorIndex}`
              }
            const colorIndex = uniqueNames.indexOf(row.tags) % colorPool.value.length
            return `row-color-${colorIndex}`
          }
          const colorIndex = uniqueNames.indexOf(row.application) % colorPool.value.length
          return `row-color-${colorIndex}`
        }
  
        return {
          datas,
          loading,
          getDatas,
          search,
          selectDate,
          dateValue,
          setDefaultDateRange,
          colorPool,
          getRowClassName,
          maxTableHeight,
          archiveVisible,
          archiveModel,
          archiveDialogShow,
          getProjects,
          projects,
          submitForm,
          handleSelectionChange,
          tableRef,
        }
      },
    }
  </script>
    
    <style lang="scss" scoped>
    ::v-deep .el-dialog__body {
      padding-bottom: 0;
    }
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