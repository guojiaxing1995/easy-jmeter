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
          <el-button type="success" @click="createChart" class="btn">趋势</el-button>
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
      <el-dialog v-model="chartVisible" title="数据趋势" width="80%"> 
        <div id="chart"  class="report-echarts"></div>
      </el-dialog>
    </div>
  </template>
  
  <script>
    import Utils from 'lin/util/util'
    import { onMounted, ref, reactive, getCurrentInstance, onBeforeUnmount } from 'vue'
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
        const chartVisible = ref(false)
        const {proxy} = getCurrentInstance()
  
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

        const createChart = () => {
          if (selectDate.value.length === 0) {
            ElMessage({
                message: '请选择数据',
                type: 'warning'
            })
            return
          }
          chartVisible.value = true
          let option = getOption(generateChartData(selectDate.value))
          console.log(option)
          setTimeout( function(){
            initChart()
            setChartOption(option)
          }, 500 )
        }

        const setChartOption = (option) => {
          let eChart = proxy.$echarts.getInstanceByDom(document.getElementById('chart'))
          eChart.setOption(option, true)
        }

        const getOption = (infos) => {
          const option = {
            title: null,
            grid: {
              left: '3%',
              right: '2%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: {
              type: 'category',
              boundaryGap: false,
              axisLabel: {
                interval: 'auto',
                rotate: 15,
              },
              data: []

            },
            yAxis: {
              name: '',
              scale:true
            },
            tooltip: {
              trigger: 'axis'
            },
            toolbox: {
              show: true,
              top: 20,
              feature: {
                dataZoom: {
                  yAxisIndex: 'none',
                  title: {
                    zoom: '区域缩放',
                    back: '缩放还原'
                  },
                },
                saveAsImage: {
                  title: '保存为图片',
                  type: 'png',
                  pixelRatio: 2,
                  backgroundColor: '#fff'
                },
              }
            },
            legend: {
              data: [],
              bottom: -5,
            },
            series: [],
          }
          option.series = infos.series
          option.legend.data = infos.labels
          option.xAxis.data = convertDateTimeFormat(infos.times)
          return option
        }
        const generateChartData = (data) => {
          data.sort((a, b) => new Date(a.start_time) - new Date(b.start_time))
          const times = data.map(item => item.start_time)
          const series = [
            {
              name: '平均响应时间',
              type: 'line',
              data: data.map(item => item.avg)
            },
            {
              name: '90th 响应时间',
              type: 'line',
              data: data.map(item => item.pct90)
            },
            {
              name: '错误率',
              type: 'line',
              data: data.map(item => (item.error !== null ? (item.error / item.sample) * 100 : 0))
            },
            {
              name: '吞吐量',
              type: 'line',
              data: data.map(item => item.tps)
            },
          ]
          return {
            times: times,
            series: series,
            labels: ['平均响应时间', '90th 响应时间', '错误率', '吞吐量']
          }
        }

        const initChart = () => {
          let eChart = proxy.$echarts.getInstanceByDom(document.getElementById('chart'))
          if (eChart == null) {
            eChart = proxy.$echarts.init(document.getElementById('chart'))
          }
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

        const convertDateTimeFormat = (dateStrings) =>{
          return dateStrings.map(dateString => {
            const date = new Date(dateString)
            const year = date.getFullYear()
            const month = String(date.getMonth() + 1).padStart(2, '0')
            const day = String(date.getDate()).padStart(2, '0')
            const hours = String(date.getHours()).padStart(2, '0')
            const minutes = String(date.getMinutes()).padStart(2, '0')
            const seconds = String(date.getSeconds()).padStart(2, '0')
            return `${month}-${day} ${hours}:${minutes}:${seconds}`
          })
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
          createChart,
          chartVisible,
          generateChartData,
          getOption,
          initChart,
          setChartOption,
          convertDateTimeFormat,
        }
      },
    }
  </script>
    
    <style lang="scss" scoped>
    ::v-deep .el-dialog__body {
      padding-top: 0;
    }
    .container {
      padding: 58px 30px 20px 30px;
        .report-echarts {
          width: 100%; 
          height: 50vh;
          margin: 1VH 0;
        }
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