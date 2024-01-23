<template>
  <el-dialog v-model="qpsLimitVisible" title="动态控量" width="35%" :close-on-click-modal=false @close="closeDialog"> 
    <el-form :model="qpsLimitModel" ref="form" @submit.prevent :rules="rules">
      <el-form-item label="QPS限制" label-width="180px" prop="qps_limit">
        <el-input-number v-model="qpsLimitModel.qps_limit" :min="0" size="large"/>
          <div class="unit">0代表不限</div>
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
  import { get,put } from '@/lin/plugin/axios'
  import { ElMessage } from 'element-plus'

  export default {
    props: {
      taskId: {
        type: String,
        default: null,
      },
      qpsLimitVisible: {
        type: Boolean,
        required: true,
      },
    },
    emits: ['qpsLimitDialogClose'],
    setup(props, context) {
      const form = ref(null)
      const task = ref({})
      const qpsLimitModel = reactive({ qps_limit:0,task_id:'' })
      const closeDialog = () => {
        context.emit('qpsLimitDialogClose')
        qpsLimitModel.qps_limit = 0
        qpsLimitModel.task_id = ''
      }

      onUpdated(() => {
        if (props.taskId) {
          getTask()
        }
      })

      const { rules } = getRules()

      const submitForm = async formName => {
        form.value.validate(async valid => {
          if (valid) {
              qpsLimitModel.task_id = props.taskId;
            let res = {}
            res = await put('/v1/task/modifyQPSLimit', qpsLimitModel, { showBackend: true })
            context.emit('qpsLimitDialogClose')
            if (res.code < window.MAX_SUCCESS_CODE) {
              ElMessage.success(`${res.message}`)
            }
          } else {
            ElMessage.error('请检查输入信息')
          }
        })
      }

      const getTask = async () => {
        const res = await get(`/v1/task/${props.taskId}`, { showBackend: true })
        task.value = res
        if(res.result.value == 0) {
          qpsLimitModel.qps_limit = res.qps_limit
        }
      }

      return {
        form,
        closeDialog,
        rules,
        submitForm,
        qpsLimitModel,
        getTask,
        task,
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
      qps_limit: [{"pattern": /^(0|[1-9][0-9]*)$/,"message": "只能输入大于等于0的整数"}],
    }
    return { rules }
  }
</script>

<style lang="scss" scoped>
.unit {
  margin-left: 15px;
}
::v-deep .el-form-item__content {
  margin-bottom: 0;
}

</style>
  