<template>
  <el-dialog v-model="taskVisible" title="用例执行" width="35%" :close-on-click-modal=false @close="closeDialog"> 
    <el-form :model="task" ref="form" @submit.prevent :rules="rules">
      <el-form-item label="总线程数" label-width="180px" prop="num_threads">
        <el-input-number v-model="task.num_threads" :min="1" :max="10000" size="large"/>
        <div class="unit">个</div>
      </el-form-item>
      <el-form-item label="持续时间" label-width="180px" prop="duration">
        <el-input-number v-model="task.duration" :min="1" :max="1209600" size="large"/>
        <div class="unit">秒</div>
      </el-form-item>
      <el-form-item label="过渡时间" label-width="180px" prop="ramp_time">
        <el-input-number v-model="task.ramp_time" :min="0" :max="1209600" size="large"/>
        <div class="unit">秒</div>
      </el-form-item>
      <el-form-item label="QPS限制" label-width="180px" prop="qps_limit">
        <el-input-number v-model="task.qps_limit" :min="0" size="large"/>
        <div class="unit">0代表不限</div>
      </el-form-item>
      <el-form-item label="压力机" label-width="180px" prop="machine">
        <el-select v-model="task.machine" multiple collapse-tags placeholder="请选择压力机" size="small" style="width: 180px" :teleported="false">
          <el-option v-for="item in machines" :key="item.id" :label="item.name" :value="item.id" :disabled="item.disabled"/>
        </el-select>
        <div class="unit">{{availableMachine}}台可用</div>
        <el-icon :size="18" @click="refreshMachines"><Refresh /></el-icon>
      </el-form-item>
      <el-form-item label="压力机数" label-width="180px" prop="machine_num">
        <el-input-number v-model="task.machine_num" :min="1" size="large"/>
        <div class="unit">个</div>
      </el-form-item>
      <el-form-item label="日志等级" label-width="180px" prop="log_level">
        <el-select v-model="task.log_level" placeholder="请选择日志级别" size="small" style="width: 180px" :teleported="false">
          <el-option v-for="item in logLevels" :key="item.value" :label="item.desc" :value="item.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="报告时间颗粒度" label-width="180px" prop="granularity">
        <el-input-number v-model="task.granularity" :min="0" size="large"/>
        <div class="unit">秒</div>
        <el-tooltip content="设置生成报告图表的时间采样频率，默认0为系统内置采样率，为0时系统会根据压测时间来设置采样间隔" placement="top">
          <el-icon :size="18" ><InfoFilled /></el-icon>
        </el-tooltip>
      </el-form-item>
      <el-form-item label="实时数据展示" label-width="180px" prop="realtime">
        <el-switch v-model="task.realtime" inline-prompt active-text="开启" inactive-text="关闭"/>
      </el-form-item>
      <el-form-item label="备注" label-width="180px" prop="remark">
        <el-input v-model="task.remark" placeholder=""  size="small" style="width: 280px"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="submitForm">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
  import { reactive,ref,onUpdated } from 'vue'
  import { get,post } from '@/lin/plugin/axios'
  import { ElMessage } from 'element-plus'
  import { Refresh, InfoFilled } from '@element-plus/icons-vue'

  export default {
    components: {
        Refresh,
        InfoFilled,
      },
    props: {
      caseId: {
        type: Number,
        default: null,
      },
      taskVisible: {
        type: Boolean,
        required: true,
      },
    },
    emits: ['taskClose'],
    setup(props, context) {
      const machines = ref([])
      const logLevels = ref([])
      const availableMachine = ref(0)
      const form = ref(null)
      const task = reactive({ num_threads:10,duration:60,ramp_time:0,jcase:'',qps_limit:0,granularity:0,machine_num:1,machine:[],log_level:null,realtime:true,remark:''})
      const closeDialog = () => {
        context.emit('taskClose')
      }

      onUpdated(() => {
        getMachine()
        getLogLevel()
      })

      const getMachine = async () => {
        const res = await get(`/v1/machine/all`, { showBackend: true })
        machines.value = setMachine(res)
      }

      const getLogLevel = async () => {
        const res = await get(`/v1/common/enum`, { showBackend: true })
        logLevels.value = res.LogLevel
        task.log_level = logLevels.value[3].value
      }

      const setMachine = (arr) => {
        availableMachine.value = 0
        for (var i = 0; i < arr.length; i++) {
          if(arr[i].is_online==false || arr[i].jmeter_status.value!==0){
            arr[i].disabled=true
          } else {
            availableMachine.value += 1
          }
        }
        return arr
      }

      const refreshMachines = () => {
        getMachine()
      }

      const { rules } = getRules()

      // 重置表单
      const resetForm = () => {
        form.value.resetFields()
      }

      const submitForm = async formName => {
        form.value.validate(async valid => {
          if (valid) {
            task.jcase = props.caseId;
            let res = {}
            res = await post('/v1/task/', task, { showBackend: true })
            resetForm(formName)
            context.emit('taskClose')
            if (res.code < window.MAX_SUCCESS_CODE) {
              ElMessage.success(`${res.message}`)
            }
          } else {
            ElMessage.error('请检查输入信息')
          }
        })
      }

      return {
        form,
        task,
        closeDialog,
        machines,
        logLevels,
        availableMachine,
        refreshMachines,
        rules,
        submitForm,
        resetForm,
      }
    },

  }

  function getRules() {
    /**
     * 验证回调函数
     */
    const checkInfo = (rule, value, callback) => {
      if (!value) {
        callback(new Error('信息不能为空'))
      }
      callback()
    }
    const rules = {
      num_threads: [{ validator: checkInfo, trigger: 'blur', required: true },{"pattern": /^[0-9]*$/,"message": "只能输入正整数"}],
      duration: [{ validator: checkInfo, trigger: 'blur', required: true },{"pattern": /^[0-9]*$/,"message": "只能输入正整数"}],
      ramp_time: [{"pattern": /^(0|[1-9][0-9]*)$/,"message": "只能输入大于等于0的整数"}],
      qps_limit: [{"pattern": /^(0|[1-9][0-9]*)$/,"message": "只能输入大于等于0的整数"}],
      machine_num: [{ validator: checkInfo, trigger: 'blur', required: true },{"pattern": /^[0-9]*$/,"message": "只能输入正整数"}],
    }
    return { rules }
  }
</script>

<style lang="scss" scoped>
.unit {
  margin-left: 15px;
}
.el-icon {
  cursor: pointer;
  margin-left: 5px;
  color: #3963bc;
}
::v-deep .el-form-item__content {
  margin-bottom: 0;
}
::v-deep .el-tag {
  color: #3963bc;
}
::v-deep .el-tag__close {
  color: black;
}

::v-deep .el-select-dropdown.is-multiple .el-select-dropdown__item.selected {
  color: #3963bc;
}

</style>
