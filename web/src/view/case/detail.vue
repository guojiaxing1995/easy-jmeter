  <template>
    <div class="container" v-if="detailIds">
      <el-row :gutter="20" class="case-detail" v-loading="caseLoading" element-loading-background="rgb(249,250,251)">
        <el-col :span="16">
          <el-row :gutter="20" class="row">
            <el-col :span="8" class="text">用例名称：{{ jcase.name }}</el-col>
            <el-col :span="8" class="text">创建人：{{ jcase.creator }}</el-col>
            <el-col :span="8" class="text">创建时间：{{ jcase.create_time }}</el-col>
          </el-row>
          <el-row :gutter="20" class="row">
            <el-col :span="8" class="text">所属项目：{{ jcase.project_name }}</el-col>
            <el-col :span="16" class="text">场景描述：{{ jcase.description  }}</el-col>
          </el-row>
        </el-col>
        <el-col :span="8" class="progress-col" v-if="task.result && task.result.value === 0">
          <el-progress type="dashboard" :stroke-width="10" :width="130" class="progress"
          :percentage=getProgressNum(jcase.task_progress)
          :color=getProgressColor(jcase)
          >
          <template #default="{ percentage }">
            <span>{{jcase.status.desc}}</span>
          </template>
          </el-progress>
          <div class="btn">
            <el-button type="primary" plain circle @click.stop="modifyQPSLimit(detailIds.taskId, jcase.qps_limit)"><i class="iconfont icon-config" ></i></el-button>
            <el-button type="primary" plain circle @click.stop="handleStop(detailIds.taskId)"><i class="iconfont icon-stop" ></i></el-button>
          </div>
        </el-col>
      </el-row>
      <el-tabs v-model="activeName" type="border-card" class="tabs" @tab-click="handleTableClick">
        <el-tab-pane label="测试详情" name="testDetail" v-loading="taskLoading">
          <el-row :gutter="20" class="row-title">
            <el-col :span="24"><span class="title">{{task.task_id}} |  {{task.create_time}}</span></el-col>
          </el-row>
          <el-row :gutter="20" class="row-content">
            <el-col :span="6" class="text-content">并发线程数：{{ task.num_threads }}</el-col>
            <el-col :span="6" class="text-content">压测时长：{{ timeDeal(task.duration) }}</el-col>
            <el-col :span="6" class="text-content">过渡时间：{{ timeDeal(task.ramp_time) }}</el-col>
            <el-col :span="6" class="text-content">测试结果：
              <el-text type="primary" size="large" v-if="task.result.value === 0">{{ task.result.desc }}</el-text>
              <el-text type="success" size="large" v-else-if="task.result.value === 1">{{ task.result.desc }}</el-text>
              <el-text type="danger" size="large" v-else>{{ task.result.desc }}</el-text>
            </el-col>
          </el-row>
          <el-row :gutter="20" class="row-content">
            <el-col :span="6" class="text-content">测试人员：{{ task.creator_name }}</el-col>
            <el-col :span="6" class="text-content">测试用例：{{ task.case_name }}</el-col>
            <el-col :span="6" class="text-content">压测开始时间：{{ startTime }}</el-col>
            <el-col :span="6" class="text-content">压测结束时间：{{ endTime }}</el-col>
          </el-row>
          <el-row :gutter="20" class="row-content">
            <el-col :span="6" class="text-content">日志等级：{{ task.log_level }}</el-col>
            <el-col :span="6" class="text-content">jmeter日志：<el-button type="primary" plain size="small" @click="downloadJmeterLog()">下载</el-button></el-col>
            <el-col :span="12" class="text-content">压力机({{task.machine_num}}台)：{{ machineStr }}</el-col>
          </el-row>
          <el-row :gutter="20" class="row-content">
            <el-col :span="6" class="text-content">jmx文件：
              <el-link v-for="item in task.jmx_file_list" type="primary" :key="item.id" :underline="false" @click="downloadFile(item.url)">{{ item.name }}</el-link>
            </el-col>
            <el-col :span="6" class="text-content">jar文件：
              <el-link v-for="item in task.jar_file_list" type="primary" :key="item.id" :underline="false" @click="downloadFile(item.url)">{{ item.name }}</el-link>
            </el-col>
            <el-col :span="12" class="text-content">csv文件：
              <el-link v-for="item in task.csv_file_list" type="primary" :key="item.id" :underline="false" @click="downloadFile(item.url)">{{ item.name }}</el-link>
            </el-col>
          </el-row>
          <el-row :gutter="20" class="row-content">
            <el-col :span="6" class="text-content">实时数据展示：<span v-if="task.realtime">是</span><span v-else>否</span></el-col>
            <el-col :span="6" class="text-content">报告时间采样颗粒度：{{ task.granularity }}秒</el-col>
            <el-col :span="12" class="text-content">测试备注：{{ task.remark }}</el-col>
          </el-row>
          <div class="row-report" v-if="task.result && task.result.value === 1">
            <div class="report-title">聚合报告</div>
            <el-table :data="taskReport.dash_board_data.statisticsTable"
                      :header-row-style="{height: '35px'}"
                      :header-cell-style="{background: '#ecf5ff', 'border-color':'#d3c7c7'}"
                      width="100%"
                      v-loading="reportLoading"
                      :row-style="{background: '#ffffff', 'border-color':'#d3c7c7'}"
                      stripe>
              <el-table-column label="请求">
                <el-table-column prop="label" label="label" width="220" show-overflow-tooltip />
              </el-table-column>
              <el-table-column label="执行">
                <el-table-column prop="samples" label="样本" width="130" show-overflow-tooltip />
                <el-table-column prop="fail" label="失败" width="100" show-overflow-tooltip />
                <el-table-column prop="error" label="错误率" width="100" show-overflow-tooltip>
                  <template #default="scope">
                    <el-text v-if="scope.row.error!='0.0'" type="danger">{{parseFloat(scope.row.error).toFixed(2)}}%</el-text>
                    <el-text v-else type="info">{{parseFloat(scope.row.error).toFixed(2)}}%</el-text>
                  </template>
                </el-table-column>
              </el-table-column>
              <el-table-column label="响应时间（毫秒）">
                <el-table-column prop="average" label="平均" width="100" :formatter="toFixed" show-overflow-tooltip />
                <el-table-column prop="min" label="最小" width="100" show-overflow-tooltip />
                <el-table-column prop="max" label="最大" width="100" show-overflow-tooltip />
                <el-table-column prop="median" label="中位数" width="100" show-overflow-tooltip />
                <el-table-column prop="90th" label="90th pct" width="100" show-overflow-tooltip />
                <el-table-column prop="95th" label="95th pct" width="100" show-overflow-tooltip />
                <el-table-column prop="99th" label="99th pct" width="100" show-overflow-tooltip />
              </el-table-column>
              <el-table-column label="吞吐量">
                <el-table-column prop="transactions" label="事务/s" width="100" :formatter="toFixed" show-overflow-tooltip />
              </el-table-column>
              <el-table-column label="网络（kb/s）">
                <el-table-column prop="received" label="接收数据" width="100" :formatter="toFixed" show-overflow-tooltip />
                <el-table-column prop="Sent" label="发送数据" width="100" :formatter="toFixed" show-overflow-tooltip />
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane label="环节日志" name="linkLog">
          <el-timeline>
            <el-timeline-item v-for="(item, index) in taskLog" :key="index" :timestamp="item.create_time" placement="top">
              <el-card>
                <p>{{ item.status.desc }}</p>
                <p v-for="(log, index) in item.logs" :key="index" class="log" >
                  <span v-if="log.result !== null && log.result!== undefined">
                    压力机{{ log.address }} 在 {{ log.update_time }}{{ log.updateTime }} 环节结束，结果为：
                    <el-text type="success" v-if="log.result">成功</el-text>
                    <el-text type="danger" v-else>失败</el-text>
                </span>
                </p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>
        <el-tab-pane label="实时信息" name="realTimeInformation" v-if="detailIds.latest">
          <div class="realtime">
            <div v-if="realtimeData.times.startTime" class="times"><el-text size="small" type="primary">{{ realtimeData.times.start}}~{{ realtimeData.times.end}}</el-text></div>
            <div class="count" v-if="realtimeData.times.startTime">
              <div class="count-item"><div class="count-label">总请求数</div><div class="count-value">{{ realtimeData.count.count.all }}</div></div> 
              <div class="count-item"><div class="count-label">总错误数</div><div class="count-value">{{ realtimeData.count.countError.all }}</div></div>
              <div class="count-item"><div class="count-label">成功率</div><div class="count-value">{{ getErrorRate(realtimeData.count.count.all,realtimeData.count.countError.all) }}%</div></div>
            </div>
            <div id="tpsRealChart" class="report-echarts"></div>
            <div id="errorRealChart" class="report-echarts"></div>
            <el-table :data="realtimeData.errorInfo.count" v-show="realtimeData.times.startTime" size="small" 
              :header-row-style="{height: '25px'}"
              :header-cell-style="{background: '#ecf5ff', 'border-color':'#d3c7c7'}"
              width="100%"
              :row-style="{background: '#ffffff', 'border-color':'#d3c7c7', height: '25px'}"
              class="errorTable">
              <el-table-column prop="transaction" label="transaction" min-width="500" show-overflow-tooltip></el-table-column>
              <el-table-column prop="sum" label="错误数" width="380"></el-table-column>
            </el-table>
            <div v-if="realtimeData.times.startTime" class="times">
              <el-select @change="transactionChange" v-model="transaction" placeholder="select transaction"><el-option v-for="item in realtimeData.transaction" :key="item.value" :label="item.label" :value="item.value"/></el-select>
            </div>
            <div class="count" v-show="transaction">
              <div class="count-item"><div class="count-label">请求数</div><div class="count-value">{{ transactionData.count }}</div></div> 
              <div class="count-item"><div class="count-label">错误数</div><div class="count-value">{{ transactionData.countError }}</div></div>
              <div class="count-item"><div class="count-label">成功率</div><div class="count-value">{{ getErrorRate(transactionData.count,transactionData.countError) }}%</div></div>
            </div>
            <div id="tpsTransactionRealChart" class="report-echarts"></div>
            <div id="errorTransactionRealChart" class="report-echarts"></div>
            <el-table :data="transactionData.errorInfo" v-show="transaction" size="small" 
              :header-row-style="{height: '25px'}"
              :header-cell-style="{background: '#ecf5ff', 'border-color':'#d3c7c7'}"
              width="100%"
              :row-style="{background: '#ffffff', 'border-color':'#d3c7c7', height: '25px'}"
              class="errorTable">
              <el-table-column prop="responseCode" label="responseCode" width="300" show-overflow-tooltip></el-table-column>
              <el-table-column prop="responseMessage" label="responseMessage" min-width="500" show-overflow-tooltip></el-table-column>
              <el-table-column prop="count" label="错误数" width="380"></el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane label="图表报告" name="chartInformation">
          <div v-if="task.result && task.result.value === 1">
            <div class="report-html">
              <el-tooltip content="下载报告" placement="top">
                <div class="report-icon" @click="downloadFile(taskReport.file.url)"><l-icon name="HTMLreport" height="1.9em" width="1.9em" /></div>
              </el-tooltip>
            </div>
            <el-row v-loading="chartInfoLoading" style="height: 500px;" v-if="chartInfoLoading"></el-row>
            <div id="transactionsPerSecondChart" class="report-echarts"></div>
            <div id="respoonseTimesOverTimeChart" class="report-echarts"></div>
            <div id="totalTPSChart" class="report-echarts"></div>
            <div id="responseTimePercentilesOverTimeChart" class="report-echarts"></div>
            <div id="activeThreadsOverTimeChart" class="report-echarts"></div>
            <div class="row-report">
              <div class="report-title">压测错误</div>
              <el-table :data="taskReport.dash_board_data.errorsTable"
                        :header-row-style="{height: '35px'}"
                        :header-cell-style="{background: '#ecf5ff', 'border-color':'#d3c7c7'}"
                        width="100%"
                        :row-style="{background: '#ffffff', 'border-color':'#d3c7c7'}"
                        stripe>
                <el-table-column prop="type" label="错误类型" show-overflow-tooltip />
                <el-table-column prop="number" label="错误数" width="150" show-overflow-tooltip />
                <el-table-column prop="currentPercent" label="错误占比" width="100" :formatter="toFixedPercent" show-overflow-tooltip />
                <el-table-column prop="allPercent" label="总数占比" width="100" :formatter="toFixedPercent" show-overflow-tooltip />
              </el-table>
            </div>
            <div class="row-report">
              <div class="report-title">场景错误排行榜top5</div>
              <el-table :data="taskReport.dash_board_data.top5ErrorsBySamplerTable"
                        :header-row-style="{height: '35px'}"
                        :header-cell-style="{background: '#ecf5ff', 'border-color':'#d3c7c7'}"
                        width="100%"
                        :row-style="{background: '#ffffff', 'border-color':'#d3c7c7'}"
                        stripe>
                <el-table-column prop="sample" label="sample"  show-overflow-tooltip />
                <el-table-column prop="samples" label="samples" width="100" show-overflow-tooltip />
                <el-table-column prop="errors" label="errors" width="80" show-overflow-tooltip />
                <el-table-column prop="errorA" label="errorA" show-overflow-tooltip />
                <el-table-column prop="errorsA" label="errorsA" width="80" show-overflow-tooltip />
                <el-table-column prop="errorB" label="errorB" show-overflow-tooltip />
                <el-table-column prop="errorsB" label="errorsB" width="80" show-overflow-tooltip />
                <el-table-column prop="errorC" label="errorC" show-overflow-tooltip />
                <el-table-column prop="errorsC" label="errorsC" width="80" show-overflow-tooltip />
                <el-table-column prop="errorD" label="errorD" show-overflow-tooltip />
                <el-table-column prop="errorsD" label="errorsD" width="80" show-overflow-tooltip />
                <el-table-column prop="errorE" label="errorE" show-overflow-tooltip />
                <el-table-column prop="errorsE" label="errorsE" width="80" show-overflow-tooltip />
              </el-table>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      <qps-limit :qpsLimitVisible="qpsLimitVisible" :taskId="taskId" @qpsLimitDialogClose="closeQPSLimit"></qps-limit>
  </div>
  <div v-else class="logo" ref="logo">
    <img class="page-logo" src="../../assets/image/error-page/logo.png" alt="" />
  </div>
  </template>
  
  <script>
    import { inject,ref,onMounted,getCurrentInstance,onActivated,onBeforeUnmount,onDeactivated,computed,watch } from 'vue'
    import { ElMessageBox, ElMessage } from 'element-plus'
    import { get,put,post } from '@/lin/plugin/axios'
    import QpsLimit from './qps-limit'
  
    export default {
      components: {
        QpsLimit
      },

      setup(props, context) {
        const jcase = ref({ name: '', creator:'',create_time:'', project_name:'', description:'', task_progress:null, status:{value:''}, task_result: {value:''}})
        const detailIds = ref(null)
        const socketio = inject('socketio')
        const qpsLimitVisible = ref(false)
        const taskId = ref('')
        const task = ref({ jmx_file_list:[{ name:'' }],jar_file_list:[{ name:'' }],csv_file_list:[{ name:'' }],machine_list:[],result:{value:''} })
        const caseLoading = ref(false)
        const taskLoading = ref(false)
        const activeName = ref('testDetail')
        const taskLog = ref([{ logs: [], status: { value: '' }, create_time: '' }])
        const taskReport = ref({"graph_data":{},"file": {"url": ""},"dash_board_data": {"statisticsTable": [],"errorsTable": [],"top5ErrorsBySamplerTable": []}})
        const {proxy} = getCurrentInstance()
        const chartInfoLoading = ref(false)
        const reportLoading = ref(false)
        const realtimeData = ref({errorInfo:{count:[]},transaction:{},times: {startTime:'',endTime:'',start:'',end:''},throughput:[],count:{count:{all:0},countError:{all:0}}})
        const timer = ref(null)
        const transaction = ref('')
        const transactionData = ref({count:0, countError:0, throughput:[], errorInfo:[]})
        
        onMounted(() => {
          detailIds.value = history.state.detail
          if (!detailIds.value){
            const headerHeight = 72
            const { clientHeight } = document.body
            const ctx = getCurrentInstance()
            ctx.refs.logo.style.height = `${clientHeight - headerHeight}px`
          } else {
            getCase()
            getTaskInfo()
            getTaskReport()
            getTaskLog()
            timer.value = window.setInterval(()=>{getRealtimeData()},5000)
          }
        })

        onBeforeUnmount(() => {
          window.removeEventListener('resize', resizeHandler)
          if (timer.value) {
            window.clearInterval(timer.value)
            timer.value = null
          }
        })

        onDeactivated(() => {
          window.removeEventListener('resize', resizeHandler)
          if (timer.value) {
            window.clearInterval(timer.value)
            timer.value = null
          }
        })

        onActivated(() => {
          console.log(history.state.detail)
          if (history.state.detail && detailIds.value && detailIds.value.caseId !== history.state.detail.caseId){
            detailIds.value = history.state.detail
            activeName.value = 'testDetail'
            getCase()
            getTaskInfo()
            getTaskReport()
            getTaskLog()
            realtimeData.value.times.startTime = ''
            transaction.value = ''
            uninitTransactionRealChart()
            uninitRealChart()
          }
          if (history.state.detail && detailIds.value && detailIds.value.taskId !== history.state.detail.taskId){
            detailIds.value = history.state.detail
            activeName.value = 'testDetail'
            getCase()
            getTaskInfo()
            getTaskReport()
            getTaskLog()
          }
          if (history.state.detail && !detailIds.value) {
            detailIds.value = history.state.detail
            getCase()
            getTaskInfo()
            getTaskReport()
            getTaskLog()
          }
          if (!timer.value) {
            timer.value = window.setInterval(()=>{getRealtimeData()},5000)
          }
        })

        socketio.on('taskProgress', (data) => {
          if (detailIds.value.taskId === data.taskId) {
            jcase.value.status = data.status
            jcase.value.task_result = data.taskResult
            if (task.value.result.value === 0) {
              task.value.result = data.taskResult
            }
            if (data.status.value === 2) {
              jcase.value.task_progress = data.taskProgress
            } else {
              jcase.value.task_progress = {'':100}
            }
          }
        })

        socketio.on('taskLogs', (data) => {
          taskLog.value = JSON.parse(JSON.stringify(data))
        })

        const getCase = async () => {
          let res
          caseLoading.value = true
          try {
            res = await get('/v1/case', {id: detailIds.value.caseId}, { showBackend: true })
            jcase.value = res[0]
            caseLoading.value = false
          } catch (error) {
            jcase.value = {}
            caseLoading.value = false
          }
        }

        const getTaskInfo = async () => {
          taskLoading.value = true
          const res = await get(`/v1/task/info/${detailIds.value.taskId}`, { showBackend: true })
          task.value = JSON.parse(JSON.stringify(res))
          taskLoading.value = false
        }

        const getTaskLog = async () => {
          const res = await get(`/v1/task/log/${detailIds.value.taskId}`, { showBackend: true })
          taskLog.value = JSON.parse(JSON.stringify(res))
        }

        const getTaskReport = async () => {
          if (detailIds.value.taskId) {
            reportLoading.value = true
            const res = await get(`/v1/task/report/${detailIds.value.taskId}`, { showBackend: true })
            taskReport.value = JSON.parse(JSON.stringify(res))
            reportLoading.value = false
          }
        }

        const downloadJmeterLog = async () => {
          let url = process.env.VUE_APP_BASE_URL + `v1/file/jmeterLog/${detailIds.value.taskId}`
          let logLink = document.createElement('a')
          logLink.style.display = 'none'
          logLink.href = url
          document.body.appendChild(logLink)
          logLink.click()
          ElMessage.success("启动下载成功")
        }

        const getProgressNum = progress => {
          if (progress == null) {
            return 100
          } else {
            return Math.min(...Object.values(progress))
          }
        }

        const toFixed = (row, column, cellValue) => {
          return parseFloat(cellValue).toFixed(2)
        }

        const toFixedPercent = (row, column, cellValue) => {
          return parseFloat(cellValue).toFixed(2) + '%'
        }

        const machineStr = computed(() => {
          let str = ''
          for(var index in task.value.machine_list) {
              str += task.value.machine_list[index].address + ' '
          }
          return str
        })

        const startTime = computed(() => {
          for(var index in taskLog.value) {
              if (taskLog.value[index].status.value === 2) {
                  return taskLog.value[index].create_time
              }
          }
        })

        const endTime = computed(() => {
          for(var index in taskLog.value) {
              if (taskLog.value[index].status.value === 3) {
                  return taskLog.value[index].create_time
              }
          }
        })

        const getProgressColor = c => {
            switch (c.status.value) {
            case 0:
              return '#5698c3'
            case 1:
              return '#f1ca17'
            case 2:
              return '#96c24e'
            case 3:
              return '#ffa60f'
            case 4:
              return '#5e616d'
            case 5:
              return '#ce5777'
          }
        }

        const handleTableClick = (tab, event) => {
          if (tab.paneName === 'chartInformation') {
            if (document.getElementById('respoonseTimesOverTimeChart') == null) {
              return
            }
            setTimeout( function(){
              initChart()
              setChartOption()
            }, 500 )
          }
          if (tab.paneName === 'realTimeInformation') {
            setTimeout( function(){
              initRealChart()
            }, 500 )
          }
         }

        const handleStop = (taskId) => {
          let res
          ElMessageBox.confirm('此操作将停止该用例的执行, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }).then(async () => {
            res = await put(`/v1/task/stop/`, { task_id: taskId }, { showBackend: true })
            res.code < 9999 ? ElMessage.success(`${res.message}`) : 1
          }).catch(()=>{})
        }

        const modifyQPSLimit = (id) => {
          qpsLimitVisible.value = true
          taskId.value = id
        }

        const closeQPSLimit = () => {
          qpsLimitVisible.value = false
          taskId.value = null
        }

        const downloadFile = (url) => {
          let link = document.createElement('a')
          link.style.display = 'none'
          link.href = url
          document.body.appendChild(link)
          link.click()
          ElMessage.success("启动下载成功")
        }

        watch( () => jcase.value.task_result.value,() => {
          getTaskReport()
        })

        const initRealChart = () => {
          let tpsRealEChart = proxy.$echarts.getInstanceByDom(document.getElementById('tpsRealChart'))
          if (tpsRealEChart == null) {
            tpsRealEChart= proxy.$echarts.init(document.getElementById('tpsRealChart'))
          }
          let errorRealEChart = proxy.$echarts.getInstanceByDom(document.getElementById('errorRealChart'))
          if (errorRealEChart == null) {
            errorRealEChart= proxy.$echarts.init(document.getElementById('errorRealChart'))
          }
        }

        const initTransactionRealChart = () => {
          let tpsRealTranEChart = proxy.$echarts.getInstanceByDom(document.getElementById('tpsTransactionRealChart'))
          if (tpsRealTranEChart == null) {
            tpsRealTranEChart= proxy.$echarts.init(document.getElementById('tpsTransactionRealChart'))
          }
          let errorRealTranEChart = proxy.$echarts.getInstanceByDom(document.getElementById('errorTransactionRealChart'))
          if (errorRealTranEChart == null) {
            errorRealTranEChart= proxy.$echarts.init(document.getElementById('errorTransactionRealChart'))
          }
        }

        const uninitTransactionRealChart = () => {
          let tpsRealTranEChart = proxy.$echarts.getInstanceByDom(document.getElementById('tpsTransactionRealChart'))
          if (tpsRealTranEChart != null) {
            tpsRealTranEChart.dispose()
          }
          let errorRealTranEChart = proxy.$echarts.getInstanceByDom(document.getElementById('errorTransactionRealChart'))
          if (errorRealTranEChart != null) {
            errorRealTranEChart.dispose()
          }
        }

        const uninitRealChart = () => {
          let tpsRealEChart = proxy.$echarts.getInstanceByDom(document.getElementById('tpsRealChart'))
          if (tpsRealEChart != null) {
            tpsRealEChart.dispose()
          }
          let errorRealEChart = proxy.$echarts.getInstanceByDom(document.getElementById('errorRealChart'))
          if (errorRealEChart != null) {
            errorRealEChart.dispose()
          }
        }
        
        const initChart = () => {
          let respoonseTimesOverTimeEChart = proxy.$echarts.getInstanceByDom(document.getElementById('respoonseTimesOverTimeChart'))
          if (respoonseTimesOverTimeEChart == null) {
            respoonseTimesOverTimeEChart= proxy.$echarts.init(document.getElementById('respoonseTimesOverTimeChart'))
          }
          
          let transactionsPerSecondEChart = proxy.$echarts.getInstanceByDom(document.getElementById('transactionsPerSecondChart'))
          if (transactionsPerSecondEChart == null) {
            transactionsPerSecondEChart= proxy.$echarts.init(document.getElementById('transactionsPerSecondChart'))
          }
          
          let responseTimePercentilesOverTimeEChart = proxy.$echarts.getInstanceByDom(document.getElementById('responseTimePercentilesOverTimeChart'))
          if (responseTimePercentilesOverTimeEChart == null) {
            responseTimePercentilesOverTimeEChart= proxy.$echarts.init(document.getElementById('responseTimePercentilesOverTimeChart'))
          }

          let totalTPSEChart = proxy.$echarts.getInstanceByDom(document.getElementById('totalTPSChart'))
          if (totalTPSEChart == null) {
            totalTPSEChart= proxy.$echarts.init(document.getElementById('totalTPSChart'))
          }
          
          let activeThreadsOverTimeEChart = proxy.$echarts.getInstanceByDom(document.getElementById('activeThreadsOverTimeChart'))
          if (activeThreadsOverTimeEChart == null) {
            activeThreadsOverTimeEChart= proxy.$echarts.init(document.getElementById('activeThreadsOverTimeChart'))
          }

          // window.addEventListener("resize", resizeHandler)
        }

        const resizeHandler = () => {
          let respoonseTimesOverTimeEChart = proxy.$echarts.getInstanceByDom(document.getElementById('respoonseTimesOverTimeChart'))
          if (respoonseTimesOverTimeEChart !==null) {
            respoonseTimesOverTimeEChart.resize()
          }
          let transactionsPerSecondEChart = proxy.$echarts.getInstanceByDom(document.getElementById('transactionsPerSecondChart'))
          if (transactionsPerSecondEChart !==null) {
            transactionsPerSecondEChart.resize()
          }
          let totalTPSEChart = proxy.$echarts.getInstanceByDom(document.getElementById('totalTPSChart'))
          if (totalTPSEChart !==null) {
            totalTPSEChart.resize()
          }
          let responseTimePercentilesOverTimeEChart = proxy.$echarts.getInstanceByDom(document.getElementById('responseTimePercentilesOverTimeChart'))
          if (responseTimePercentilesOverTimeEChart !==null) {
            responseTimePercentilesOverTimeEChart.resize()
          }
          let activeThreadsOverTimeEChart = proxy.$echarts.getInstanceByDom(document.getElementById('activeThreadsOverTimeChart'))
          if (activeThreadsOverTimeEChart !==null) {
            activeThreadsOverTimeEChart.resize()
          }
        }

        const setChartOption = () => {
          chartInfoLoading.value = true
          if (Object.keys(taskReport.value.graph_data).length === 0) {
            setTimeout(setChartOption, 1000)
          } else {
            let responseTimesOverTimeOption = getOption("responseTimesOverTimeInfos")
            let respoonseTimesOverTimeEChart = proxy.$echarts.getInstanceByDom(document.getElementById('respoonseTimesOverTimeChart'))
            respoonseTimesOverTimeEChart.setOption(responseTimesOverTimeOption, true)

            let transactionsPerSecondOption = getOption("transactionsPerSecondInfos")
            let transactionsPerSecondEChart = proxy.$echarts.getInstanceByDom(document.getElementById('transactionsPerSecondChart'))
            transactionsPerSecondEChart.setOption(transactionsPerSecondOption, true)

            let totalTPSOption = getOption("totalTPSInfos")
            let totalTPSEChart = proxy.$echarts.getInstanceByDom(document.getElementById('totalTPSChart'))
            totalTPSEChart.setOption(totalTPSOption, true)
            
            let responseTimePercentilesOverTimeOption = getOption("responseTimePercentilesOverTimeInfos")
            let responseTimePercentilesOverTimeEChart = proxy.$echarts.getInstanceByDom(document.getElementById('responseTimePercentilesOverTimeChart'))
            responseTimePercentilesOverTimeEChart.setOption(responseTimePercentilesOverTimeOption, true)

            let activeThreadsOverTimeOption = getOption("activeThreadsOverTimeInfos")
            let activeThreadsOverTimeEChart = proxy.$echarts.getInstanceByDom(document.getElementById('activeThreadsOverTimeChart'))
            activeThreadsOverTimeEChart.setOption(activeThreadsOverTimeOption, true)
            chartInfoLoading.value = false
          }
        }

        const getOption = (infos) => {
          const option = {
            title: {
              text: "",
              left: "center",
            },
            grid: {
              left: '2%',
              right: '3%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: {
              type: 'time',
              axisLabel: {
                interval: 'auto',
                rotate: 35,
                formatter: function (value, index) {
                  var date = new Date(value)
                  return date.getHours()+':'+date.getMinutes()+':'+date.getSeconds()
                }
              },
              name: '时间',
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
              }
            },
            legend: {
              data: [],
              bottom: -5,
            },
            series: [],
          }
          option.series = taskReport.value.graph_data[infos].series
          option.title.text = taskReport.value.graph_data[infos].titleCN
          option.legend.data = taskReport.value.graph_data[infos].labels
          option.yAxis.name = taskReport.value.graph_data[infos].yName
          return option
        }

        const setRealChartOption = (type) => {
          const option = {
            title: {
              text: "",
              left: "center",
            },
            grid: {
              left: '2%',
              right: '3.5%',
              bottom: '3%',
              containLabel: true
            },
            xAxis: {
              type: 'time',
              axisLabel: {
                interval: 'auto',
                rotate: 35,
                formatter: function (value, index) {
                  var date = new Date(value)
                  return date.getHours()+':'+date.getMinutes()+':'+date.getSeconds()
                }
              },
              name: '时间',
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
            },
            legend: {
              data: [],
              bottom: -5,
            },
            series: [],
          }
          if (type==='tps_all') {
            let series = {}
            series.data = realtimeData.value.throughput.all
            series.type = 'line'
            series.name = 'TPS'
            option.series.push(series)
            option.title.text = 'TPS'
            option.legend.data.push('TPS')
            option.yAxis.name = '事务数/秒'
            let tpsRealEChart = proxy.$echarts.getInstanceByDom(document.getElementById('tpsRealChart'))
            if (tpsRealEChart != null) {
              tpsRealEChart.setOption(option, true)
              // 第一次渲染成功后判断是不是可以停止定时任务
              if (jcase.value.status.value != 2) {
                if (timer.value) {
                  window.clearInterval(timer.value)
                  timer.value = null
                }
              }
            }
          }
          if (type==='tps_transaction') {
            let series = {}
            series.data = transactionData.value.throughput
            series.type = 'line'
            series.name = 'TPS'
            option.series.push(series)
            option.title.text = 'TPS'
            option.legend.data.push('TPS')
            option.yAxis.name = '事务数/秒'
            let tpsTransactionRealChart = proxy.$echarts.getInstanceByDom(document.getElementById('tpsTransactionRealChart'))
            if (tpsTransactionRealChart != null) {
              tpsTransactionRealChart.setOption(option, true)
            }
          }
          if (type==='error') {
            let series = {}
            series.data = realtimeData.value.error.all
            series.type = 'line'
            series.name = '错误数'
            option.series.push(series)
            option.title.text = '总错误趋势'
            option.legend.data.push('错误数')
            option.yAxis.name = '个'
            let errorRealChart = proxy.$echarts.getInstanceByDom(document.getElementById('errorRealChart'))
            if (errorRealChart != null) {
              errorRealChart.setOption(option, true)
            }
          }
          if (type==='error_transaction') {
            let series = {}
            series.data = transactionData.value.error
            series.type = 'line'
            series.name = '错误数'
            option.series.push(series)
            option.title.text = '错误趋势'
            option.legend.data.push('错误数')
            option.yAxis.name = '个'
            let errorRealTranEChart = proxy.$echarts.getInstanceByDom(document.getElementById('errorTransactionRealChart'))
            if (errorRealTranEChart != null) {
              errorRealTranEChart.setOption(option, true)
            }
          }
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

        const getErrorRate = (all,error) => {
          if (all === 0) {
            return 0
          }
          if (all === undefined) {
            return 0
          }
          return ((all-error)/all*100).toFixed(2)
        }

        const transactionChange = (value) => {
          initTransactionRealChart()
          // 获取请求数
          for (let key in realtimeData.value.count.count) {
            if (key === value) {
              transactionData.value.count = realtimeData.value.count.count[key]
              break
            }
          }
          for (let key in realtimeData.value.count.countError) {
            if (key === value) {
              transactionData.value.countError = realtimeData.value.count.countError[key]
              break
            } else {
              transactionData.value.countError = 0
            }
          }
          // 获取tps
          for (let key in realtimeData.value.throughput) {
            if (key === value) {
              transactionData.value.throughput = realtimeData.value.throughput[key]
              // 渲染tps图表
              setRealChartOption('tps_transaction')
              break
            }
          }
          // 获取错误趋势
          for (let key in realtimeData.value.error) {
            if (key === value) {
              transactionData.value.error = realtimeData.value.error[key]
              // 渲染错误趋势图表
              setRealChartOption('error_transaction')
              break
            } else {
              transactionData.value.error = []
              if (transaction.value != '') {
                setRealChartOption('error_transaction')
              }
            }
          }
          // 获取错误信息
          if (realtimeData.value.errorInfo.transaction) {
            realtimeData.value.errorInfo.transaction.forEach(element => {
              if (element.transaction === value) {
                transactionData.value.errorInfo = element.count
              } else {
                transactionData.value.errorInfo = []
              }
            })
          } else {
            transactionData.value.errorInfo = []
          }
        }

        const getRealtimeData = async() => {
          let res
          res = await post(`/v1/task/realTimeData`, { task_id: detailIds.value.taskId, type: 'TIMES' }, { showBackend: true })
          realtimeData.value.times = res
          if (realtimeData.value.times.startTime) {
            // 获取实时数据
            const promise1 = getCount()
            const promise2 = getThroughput()
            const promise3 = getError()
            const promise4 = getErrorInfo()

            await Promise.all([promise1, promise2, promise3, promise4])
          } else {
            uninitRealChart()
            transaction.value = ''
            uninitTransactionRealChart()
          }
        }

        const getCount = async() => {
          let res
          res = await post(`/v1/task/realTimeData`, { task_id: detailIds.value.taskId, type: 'COUNT' }, { showBackend: true })
          realtimeData.value.count = res
        }

        const getThroughput = async() => {
          let res
          res = await post(`/v1/task/realTimeData`, { task_id: detailIds.value.taskId, type: 'THROUGHPUT' }, { showBackend: true })
          realtimeData.value.throughput = res
          setRealChartOption('tps_all')
          getTransaction(res)
        }

        const getError = async() => {
          let res
          res = await post(`/v1/task/realTimeData`, { task_id: detailIds.value.taskId, type: 'ERROR' }, { showBackend: true })
          realtimeData.value.error = res
          setRealChartOption('error')
        }

        const getErrorInfo = async() => {
          let res
          res = await post(`/v1/task/realTimeData`, { task_id: detailIds.value.taskId, type: 'ERROR_INFO' }, { showBackend: true })
          realtimeData.value.errorInfo = res
        }

        const getTransaction = (data) => {
          let set = new Set()
          for (let key in data) {
            if (key!='all') {
              set.add(key)
            }
          }
          let arr = []
          for (const value of set) {
            let obj = {
              value: value,
              label: value
            }
            arr.push(obj)
          }
          realtimeData.value.transaction = arr
        }
  
        return {
          getCase,
          jcase,
          detailIds,
          getProgressNum,
          getProgressColor,
          handleStop,
          modifyQPSLimit,
          qpsLimitVisible,
          closeQPSLimit,
          taskId,
          caseLoading,
          taskLoading,
          activeName,
          getTaskInfo,
          task,
          machineStr,
          downloadFile,
          taskLog,
          taskReport,
          getTaskLog,
          startTime,
          endTime,
          downloadJmeterLog,
          getTaskReport,
          toFixed,
          toFixedPercent,
          handleTableClick,
          proxy,
          setChartOption,
          initChart,
          resizeHandler,
          getOption,
          chartInfoLoading,
          reportLoading,
          timeDeal,
          realtimeData,
          getRealtimeData,
          timer,
          getCount,
          getThroughput,
          getTransaction,
          getErrorRate,
          initRealChart,
          setRealChartOption,
          uninitRealChart,
          transaction,
          transactionData,
          transactionChange,
          initTransactionRealChart,
          uninitTransactionRealChart,
          getErrorInfo,
        }
      },
  
    }
  
  </script>
  
  <style lang="scss" scoped>
  .container {
    padding: 0 30px;
    .case-detail {
      height: 120px;
      .row {
        height: 60px;
        line-height: 60px;
        vertical-align: middle;
        .text {
          white-space:nowrap;
          text-overflow:ellipsis;
          overflow:hidden;
        }
      }
      .progress-col {
        height: 120px;
        display: flex;
        
        .progress{
          margin-left: 50px;
          margin-top: 8px;
        }
        .btn {
          margin-top: 50px;
          margin-left: 50px;
        }
      }
    }
    .tabs {
      margin-top: 15px;
      margin-bottom: 8px;
      ::v-deep .el-tabs__item {
        font-weight: 600;
      }
      .row-title {
        height: 70px;
        line-height: 70px;
        vertical-align: middle;
        text-align: center;
        .title {
          font-weight: 600;
          font-size: 1.5rem;
          color: #3963bc;
        }
      }
      .row-content {
        height: 60px;
        line-height: 60px;
        vertical-align: middle;
        .text-content {
          white-space:nowrap;
          text-overflow:ellipsis;
          overflow:hidden;
          font-weight: 500;
          ::v-deep .el-link{
            margin-right: 8px;
            font-size: 1.1rem;
          }
        }
      }
      .row-report {
        padding: 15px 0 0 0;
        .report-title {
          text-align: center;
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 8px;
        }
      }
      .report-html {
        position: relative;
        height: 30px;
        z-index: 9999999;
        .report-icon {
          cursor: pointer;
          right: 30px;
          position: absolute;
        }
      }
      .report-echarts {
        width: 100%; 
        height: 35vh;
        margin: 20px 0;
      }
      .log {
        margin: 10px 0;
      }
      .realtime {
        .times {
          text-align: right;
        }
        .errorTable {
          margin-bottom: 10px;
        }
        .count {
          height: 10vh;
          width: 100%;
          display: flex;
          flex-direction: row;
          justify-content: space-between;
          .count-item {
            height: 100%;
            width: 33%;
            text-align: center;
            display: flex;
            justify-content: center;
            align-items: center;
            flex-direction: column;
            .count-label {
              font-size: 12px;
              color: #6e6d6d;
            }
            .count-value {
              font-size: 28px;
              margin-top: 8px;
              color: #4577ff;
            }
          }
        }
      }
    }
    .logo {
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
      .page-logo {
        width: 125px;
        height: 350px;
      }
    }
  }
  
  </style>
  