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
        <el-col :span="8" class="progress-col" v-if="jcase.task_result &&jcase.task_result.value === 0">
          <el-progress type="dashboard" :stroke-width="10" :width="130" class="progress"
          :percentage=getProgressNum(jcase.task_progress)
          :color=getProgressColor(jcase)
          >
          <template #default="{ percentage }">
            <span>{{jcase.status.desc}}</span>
          </template>
          </el-progress>
          <div class="btn">
            <el-button type="primary" plain circle @click.stop="modifyQPSLimit(jcase.task_id, jcase.qps_limit)"><i class="iconfont icon-config" ></i></el-button>
            <el-button type="primary" plain circle @click.stop="handleStop(jcase.task_id)"><i class="iconfont icon-stop" ></i></el-button>
          </div>
        </el-col>
      </el-row>
      <el-tabs v-model="activeName" type="border-card" class="tabs">
        <el-tab-pane label="测试详情" name="testDetail" v-loading="taskLoading">
          <el-row :gutter="20" class="row-title">
            <el-col :span="24"><span class="title">{{task.task_id}} |  {{task.create_time}}</span></el-col>
          </el-row>
          <el-row :gutter="20" class="row-content">
            <el-col :span="6" class="text-content">并发线程数：{{ task.num_threads }}</el-col>
            <el-col :span="6" class="text-content">压测时长：{{ task.duration }}</el-col>
            <el-col :span="6" class="text-content">过渡时间：{{ task.ramp_time }}</el-col>
            <el-col :span="6" class="text-content">测试结果：
              <el-text type="primary" size="large" v-if="task.result.value === 0">{{ task.result.desc }}</el-text>
              <el-text type="success" size="large" v-else-if="task.result.value === 1">{{ task.result.desc }}</el-text>
              <el-text type="danger" size="large" v-else>{{ task.result.desc }}</el-text>
            </el-col>
          </el-row>
          <el-row :gutter="20" class="row-content">
            <el-col :span="6" class="text-content">测试人员：{{ task.creator_name }}</el-col>
            <el-col :span="6" class="text-content">测试用例：{{ task.case_name }}</el-col>
            <el-col :span="6" class="text-content">压测开始时间：</el-col>
            <el-col :span="6" class="text-content">压测结束时间：</el-col>
          </el-row>
          <el-row :gutter="20" class="row-content">
            <el-col :span="6" class="text-content">日志等级：{{ task.log_level }}</el-col>
            <el-col :span="6" class="text-content">jmeter日志：</el-col>
            <el-col :span="12" class="text-content">压力机({{task.machine_num}}台)：{{ machine_str }}</el-col>
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
            <el-col :span="6" class="text-content">运行状态监控：<span v-if="task.monitor">是</span><span v-else>否</span></el-col>
            <el-col :span="12" class="text-content">测试备注：{{ task.remark }}</el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="环节日志" name="linkLog">
          <el-timeline>
            <el-timeline-item timestamp="2018/4/12" placement="top">
              <el-card>
                <h4>Update Github template</h4>
                <p>Tom committed 2018/4/12 20:46</p>
              </el-card>
            </el-timeline-item>
            <el-timeline-item timestamp="2018/4/3" placement="top">
              <el-card>
                <h4>Update Github template</h4>
                <p>Tom committed 2018/4/3 20:46</p>
              </el-card>
            </el-timeline-item>
            <el-timeline-item timestamp="2018/4/2" placement="top">
              <el-card>
                <h4>Update Github template</h4>
                <p>Tom committed 2018/4/2 20:46</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>
        <el-tab-pane label="实时信息" name="realTimeInformation"></el-tab-pane>
        <el-tab-pane label="图表报告" name="chartInformation"></el-tab-pane>
      </el-tabs>
      <qps-limit :qpsLimitVisible="qpsLimitVisible" :taskId="taskId" @qpsLimitDialogClose="closeQPSLimit"></qps-limit>
  </div>
  <div v-else class="logo" ref="logo">
    <img class="page-logo" src="../../assets/image/error-page/logo.png" alt="" />
  </div>
  </template>
  
  <script>
    import { inject,ref,onMounted,getCurrentInstance,onActivated, computed } from 'vue'
    import { ElMessageBox, ElMessage } from 'element-plus'
    import { get,put } from '@/lin/plugin/axios'
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


        onMounted(() => {
          detailIds.value = history.state.detail
          if (!detailIds.value){
            const headerHeight = 72
            const { clientHeight } = document.body
            const ctx = getCurrentInstance()
            ctx.refs.logo.style.height = `${clientHeight - headerHeight}px`
          }
        })

        onActivated(() => {
          console.log(history.state.detail)
          if (history.state.detail){
            detailIds.value = history.state.detail
          }
          if (detailIds.value && detailIds.value.caseId !== jcase.value.id){
            getCase()
          }
          if (detailIds.value && detailIds.value.taskId){
            getTaskInfo()
          }
        })

        socketio.on('taskProgress', (data) => {
          console.log('Received data:', data)
          if (jcase.value.task_id === data.taskId) {
            jcase.value.status = data.status
            jcase.value.task_result = data.taskResult
            task.value.result = data.taskResult
            if (data.status.value === 2) {
              jcase.value.task_progress = data.taskProgress
            } else {
              jcase.value.task_progress = {'':100}
            }
          }
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
          const res = await get(`/v1/task/info/${history.state.detail.taskId}`, { showBackend: true })
          task.value = JSON.parse(JSON.stringify(res))
          taskLoading.value = false
        }

        const getProgressNum = progress => {
          if (progress == null) {
            return 100
          } else {
            return Math.min(...Object.values(progress))
          }
        }

        const machine_str = computed(() => {
          let str = ''
          for(var index in task.value.machine_list) {
              str += task.value.machine_list[index].address + ' '
          }
          return str
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

        const handleStop = taskId => {
          let res
          ElMessageBox.confirm('此操作将停止该用例的执行, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }).then(async () => {
            res = await put(`/v1/task/stop/`, { task_id: taskId }, { showBackend: true })
            res.code < 9999 ? ElMessage.success(`${res.message}`) : 1
          })
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
          machine_str,
          downloadFile,
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
  