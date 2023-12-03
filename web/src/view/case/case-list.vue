<template>
    <div class="container" v-if="!showEdit">
      <div class="header">
        <div class="newBtn"><el-button type="primary" @click="handleCreate">新 增</el-button></div>
        <div class="search">
          <el-select v-model="projectId" placeholder="请选择工程" clearable filterable>
            <el-option v-for="item in projects" :key="item.value" :label="item.name" :value="item.id"/>
          </el-select>
          <el-input placeholder="请输入用例名称查询" v-model="name" clearable></el-input>
        </div>
      </div>
      <el-main v-if="loading" v-loading = "loading" element-loading-text="Loading..." element-loading-background="#F9FAFB" style="height: 600px;"/>
      <div class="list" v-else>
        <div class="case" v-for="(item,index) in casesRes">
          <div class="line line-name">
            <div class="name">{{item.name}}</div>
            <div class="last-run">{{item.creator}}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{{item.create_time}}</div>
          </div>
          <div class="line">
            <div class="progress">
              <el-progress :text-inside="true" :stroke-width="23" :duration="60"
              :percentage=getProgressNum(item.task_progress)
              :color=getProgressColor(item) 
              :striped=setProgressStriped(item)
              striped-flow>
                <span>{{item.status.desc}}</span>
              </el-progress>
            </div>
            <div class="line-icon">
              <i class="iconfont icon-start" @click.stop="executeCase(item.id)"></i>
              <i class="iconfont icon-config" @click.stop="modifyQPSLimit(item.task_id, item.qps_limit)"></i>
            </div>
          </div>
          <div class="line">
            <div class="line-icon">
              <i class="iconfont icon-stop" @click.stop="handleStop(item.task_id)"></i>
              <i class="iconfont icon-debug"></i>
              <i class="iconfont icon-modify" @click.stop="handleEdit(item.id)"></i>
              <i class="iconfont icon-remove" @click.stop="handleDelete(item.id)"></i>
              <i class="iconfont icon-history"></i>
            </div>
          </div>
        </div>
      </div>
      <task :taskVisible="taskVisible" :caseId="taskCaseId" @taskClose="closeTask"></task>
      <qps-limit :qpsLimitVisible="qpsLimitVisible" :taskId="taskId" @qpsLimitDialogClose="closeQPSLimit"></qps-limit>
    </div>
    <case v-else @editClose="editClose" :editCaseId="editCaseId" :projects="projects"></case>
  </template>
  
  <script>
    import Utils from 'lin/util/util'
    import { onMounted, ref, watch, inject } from 'vue'
    import { get,put,_delete } from '@/lin/plugin/axios'
    import { ElMessageBox, ElMessage } from 'element-plus'
    import Case from './case'
    import Task from './task'
    import QpsLimit from './qps-limit'
  
    export default {
      components: {
        Case,
        Task,
        QpsLimit
      },
      setup() {
        const showEdit = ref(false)
        const name = ref('')
        const cases = ref([])
        const loading = ref(false)
        const editCaseId = ref(null)
        const projects = ref([])
        const projectId = ref('')
        const casesRes = ref([])
        const taskVisible = ref(false)
        const qpsLimitVisible = ref(false)
        const taskCaseId = ref(null)
        const taskId = ref('')
        const socketio = inject('socketio')
  
        onMounted(() => {
            getProjects()
            getCases()
        })

        socketio.on('taskProgress', (data) => {
          console.log('Received data:', data)
          for (let i = 0; i < cases.value.length; i++) {
            if (cases.value[i].task_id === data.taskId) {
              cases.value[i].status = data.status
              if (data.status.value === 2) {
                cases.value[i].task_progress = data.taskProgress
              } else {
                cases.value[i].task_progress = {'':100}
              }
            }
          }
          searchCases()
        })

        const getProjects = async () => {
          let res
          try {
            res = await get('/v1/project/all', { showBackend: true })
            projects.value = res
          } catch (error) {
            projects.value = []
          }
        }

        const getCases = async () => {
          loading.value = true
          let res
          try {
            res = await get('/v1/case', { showBackend: true })
            cases.value = res
          } catch (error) {
            cases.value = []
          }
          searchCases()
        }

        const editClose = () => {
          showEdit.value = false
          getCases()
        }
        const searchCases = () => {
          loading.value = true
          casesRes.value = []
          for (let i = 0; i < cases.value.length; i++) {
            if (cases.value[i].name.includes(name.value, { ignoreCase: true }) && (cases.value[i].project == projectId.value || projectId.value=='')) {
              casesRes.value.push(cases.value[i])
            }
          }
          loading.value = false
        }
        
        const handleCreate = () => {
          showEdit.value = true
          editCaseId.value = null
        }

        const handleEdit = id => {
          showEdit.value = true
          editCaseId.value = id
        }

        const executeCase = id => {
          taskVisible.value = true
          taskCaseId.value = id
        }

        const modifyQPSLimit = (id) => {
          qpsLimitVisible.value = true
          taskId.value = id
        }

        const closeTask = () => {
          taskVisible.value = false
          taskCaseId.value = null
          getCases()
        }

        const closeQPSLimit = () => {
          qpsLimitVisible.value = false
          taskId.value = null
        }

        const handleDelete = id => {
          let res
          ElMessageBox.confirm('此操作将永久删除该用例, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }).then(async () => {
            loading.value = true
            res = await _delete(`/v1/case/${id}`, { showBackend: true })
            getCases()
            res.code < 9999 ? ElMessage.success(`${res.message}`) : 1
          })
        }

        const handleStop = taskId => {
          let res
          ElMessageBox.confirm('此操作将停止该用例的执行, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }).then(async () => {
            res = await put(`/v1/task/stop/`, { taskId: taskId }, { showBackend: true })
            res.code < 9999 ? ElMessage.success(`${res.message}`) : 1
          })
        }

        const _debounce =Utils.debounce(()=>{
          searchCases()
        }, 800)
  
        watch(name, () => {
          _debounce()
        })

        watch(projectId, () => {
          _debounce()
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

        const getProgressNum = progress => {
          if (progress == null) {
            return 100
          } else {
            return Math.min(...Object.values(progress))
          }
        }

        const setProgressStriped = c => {
          switch (c.status.value) {
            case 0:
              return false
            case 1:
              return true
            case 2:
              return true
            case 3:
              return true
            case 4:
              return true
            case 5:
              return true
          }
        }

        return {
          cases,
          loading,
          editCaseId,
          projects,
          projectId,
          name,
          showEdit,
          handleCreate,
          casesRes,
          searchCases,
          editClose,
          handleEdit,
          handleDelete,
          taskVisible,
          qpsLimitVisible,
          taskCaseId,
          taskId,
          executeCase,
          modifyQPSLimit,
          closeTask,
          getProgressColor,
          getProgressNum,
          setProgressStriped,
          handleStop,
          closeQPSLimit,
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
        display: flex;
        align-items: center;
        height: 59px;
        line-height: 59px;
        color: $parent-title-color;
        font-size: 16px;
        font-weight: 500;
        .el-select {
          margin-right: 20px;
          width: 350px;
        }
      }
    }

    .list {
      display: flex;
      flex-wrap: wrap;
    }
    .case:hover {
      background-color: #ecf5ff;
      cursor: pointer;
    }
    .case {
      width: calc(50% - 32px);
      margin: 15px;
      border-radius: 5px;
      box-shadow: 0px 0px 12px rgba(0, 0, 0, .12);
      background-color: #f5faff;
      border: 1px solid #f5faff;

      .line {
        height: 45px;
        line-height: 45px;
        align-items: center;
        position: relative;
        .progress {
          width: 75%;
          transform: translate(0, -50%);  
          position: absolute;  
          top: 50%;
          margin-left: 8px;
          .el-progress {
            width: 100%;
          }
        }
        .line-icon {
          transform: translate(0, -50%);  
          position: absolute;
          top: 50%;
          right: 0;
          .iconfont {
            margin-right: 25px;
            font-size: 1.5rem;
          }
        }
      }
      .line-name {
        display: flex;
        justify-content: space-between;
        .name {
          margin: 0 0 0 8px;
          font-size: 20px;
          white-space:nowrap;
          text-overflow:ellipsis;
          overflow:hidden;
          width: 72%;
        }
        .last-run {
          margin: 0 8px 0 0;
          font-size: 10px;
        }
      }
    }
  }
  </style>