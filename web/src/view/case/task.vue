<template>
  <el-dialog v-model="taskVisible" title="用例执行" width="35%" :close-on-click-modal=false @close="closeDialog"> 
    <el-form :model="task" ref="form">
      <el-form-item label="总线程数" label-width="180px">
        <el-input-number v-model="task.threads" :min="1" :max="10000" size="large"/>
        <div class="unit">个</div>
      </el-form-item>
      <el-form-item label="持续时间" label-width="180px">
        <el-input-number v-model="task.duration" :min="1" :max="1209600" size="large"/>
        <div class="unit">秒</div>
      </el-form-item>
      <el-form-item label="预热时间" label-width="180px">
        <el-input-number v-model="task.warmupTime" :min="0" :max="1209600" size="large"/>
        <div class="unit">秒</div>
      </el-form-item>
      <el-form-item label="QPS限制" label-width="180px">
        <el-input-number v-model="task.limit" :min="0" size="large"/>
        <div class="unit">0代表不限</div>
      </el-form-item>
      <el-form-item label="压力机" label-width="180px">
        <el-select v-model="task.machine" multiple collapse-tags placeholder="请选择压力机" size="small" style="width: 180px">
          <el-option v-for="item in machines" :key="item.id" :label="item.name" :value="item.id" :disabled="item.disabled"/>
        </el-select>
        <div class="unit">{{availableMachine}}台可用</div>
        <el-icon :size="18" @click="refreshMachines"><Refresh /></el-icon>
      </el-form-item>
      <el-form-item label="压力机数" label-width="180px">
        <el-input-number v-model="task.limit" :min="1" size="large"/>
        <div class="unit">个</div>
      </el-form-item>
      <el-form-item label="运行状态监测" label-width="180px">
        <el-switch v-model="task.monitor" inline-prompt active-text="开启" inactive-text="关闭"/>
      </el-form-item>
      <el-form-item label="实时数据展示" label-width="180px">
        <el-switch v-model="task.realtime" inline-prompt active-text="开启" inactive-text="关闭"/>
      </el-form-item>
      <el-form-item label="详细日志记录" label-width="180px">
        <el-switch v-model="task.log" inline-prompt active-text="开启" inactive-text="关闭"/>
      </el-form-item>
      <el-form-item label="备注" label-width="180px">
        <el-input v-model="task.remark" placeholder=""  size="small" style="width: 280px"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
  import { reactive,ref,onUpdated } from 'vue'
  import { get,post } from '@/lin/plugin/axios'
  import { Refresh } from '@element-plus/icons-vue'

  export default {
    components: {
        Refresh,
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
      const availableMachine = ref(0)
      const form = ref(null)
      const task = reactive({ threads:10,duration:60,warmupTime:0,case:'',limit:0,monitor:false,log:false,realtime:false,remark:''})
      const closeDialog = () => {
        context.emit('taskClose')
      }

      onUpdated(() => {
        getMachine()
      })

      const getMachine = async () => {
        const res = await get(`/v1/machine/all`, { showBackend: true })
        machines.value = setMachine(res)
      }

      const setMachine = (arr) => {
        availableMachine.value = 0
        for (var i = 0; i < arr.length; i++) {
          if(arr[i].online=="离线"){
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

      return {
        form,
        task,
        closeDialog,
        machines,
        availableMachine,
        refreshMachines,
      }
    },

  }
</script>

<style lang="scss" scoped>
.unit {
  margin-left: 15px;
}
.el-icon {
  cursor: pointer;
  margin-left: 5px;
}
::v-deep .el-form-item__content {
  margin-bottom: 0;
}
::v-deep .el-tag {
  color: black;
}
::v-deep .el-tag__close {
  color: black;
}
</style>
