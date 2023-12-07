  <template>
    <div class="container" v-if="detailIds">
      <el-row :gutter="20" class="case-detail">
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
      <qps-limit :qpsLimitVisible="qpsLimitVisible" :taskId="taskId" @qpsLimitDialogClose="closeQPSLimit"></qps-limit>
  </div>
  <div v-else class="logo" ref="logo">
    <img class="page-logo" src="../../assets/image/error-page/logo.png" alt="" />
  </div>
  </template>
  
  <script>
    import { inject,ref,onMounted,getCurrentInstance,onActivated } from 'vue'
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
        })

        socketio.on('taskProgress', (data) => {
          console.log('Received data:', data)
          if (jcase.value.task_id === data.taskId) {
            jcase.value.status = data.status
            jcase.value.task_result = data.taskResult
            if (data.status.value === 2) {
              jcase.value.task_progress = data.taskProgress
            } else {
              jcase.value.task_progress = {'':100}
            }
          }
        })

        const getCase = async () => {
          let res
          try {
            res = await get('/v1/case', {id: detailIds.value.caseId}, { showBackend: true })
            jcase.value = res[0]
          } catch (error) {
            jcase.value = {}
          }
        }

        const getProgressNum = progress => {
          if (progress == null) {
            return 100
          } else {
            return Math.min(...Object.values(progress))
          }
        }

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
  