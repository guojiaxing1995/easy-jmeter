<template>
  <div>
    <el-dialog v-model="debugVisible" title="场景调试" width="65%" :close-on-click-modal=false @close="closeDialog">
      <el-tabs v-model="activeName" >
        <el-tab-pane label="采样日志" name="sample">
          <div class="body">
            <el-row :gutter="0">
              <el-col :span="6">
                <div class="list">
                  <div class="item" v-for="item in httpSamples" v-bind:class="{'item-color': !item.is_choose, 'item-color-choose': item.is_choose}" :key="item.id" @click="chooseSample(item)">
                    <i class="el-icon-check" v-if="item.s && item.is_choose"></i>
                    <i class="el-icon-success" v-if="item.s && !item.is_choose" style="color: #00C292"></i>
                    <i class="el-icon-error" v-if="!item.s && !item.is_choose" style="color: #E46A76"></i>
                    <i class="el-icon-close" v-if="!item.s && item.is_choose"></i>
                    {{ item.lb }}
                  </div>
                </div>
              </el-col>
              <el-col :span="18">
                <el-tabs v-model="activeNameSample" type="border-card" class="sampleTab">
                  <el-tab-pane label="响应体" name="responseBody">
                    <codemirror v-model="responseBodyCode" :style="{height:'calc(60vh - 39px)'}" :disabled="true" :extensions="extensions"/>
                  </el-tab-pane>
                  <el-tab-pane label="响应头" name="responseHeader">
                    <codemirror v-model="responseHeaderCode" :style="{height:'calc(60vh - 39px)'}" :disabled="true" :extensions="extensions"/>
                  </el-tab-pane>
                  <el-tab-pane label="请求体" name="requestBody">
                    <codemirror v-model="requestBodyCode" :style="{height:'calc(60vh - 39px)'}" :disabled="true" :extensions="extensions"/>
                  </el-tab-pane>
                  <el-tab-pane label="请求头" name="requestHeader">
                    <codemirror v-model="requestHeaderCode" :style="{height:'calc(60vh - 39px)'}" :disabled="true" :extensions="extensions"/>
                  </el-tab-pane>
                  <el-tab-pane label="断言" name="assert">
                    
                  </el-tab-pane>
                  <el-tab-pane label="取样器结果" name="sampleData">
                    <codemirror v-model="sampleResultCode" :style="{height:'calc(60vh - 39px)'}" :disabled="true" :extensions="extensions"/>
                  </el-tab-pane>
                </el-tabs>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
        <!-- <el-tab-pane label="引擎日志" name="log"></el-tab-pane> -->
      </el-tabs>
      <template #footer>
        <div class="footer">
          <el-text v-if="status=='CONFIG'" size="large" type="success">配置完成</el-text>
          <el-text v-if="status=='SAMPLE'" size="large" type="success">调试成功</el-text>
          <el-text v-if="status=='ERROR'" size="large" type="danger">调试异常 {{ errorStr }}</el-text>
          <el-text v-else size="large" type="info"></el-text>
          <div>
            <el-button @click="clear">清空</el-button>
            <el-button :loading="loading" type="primary" @click="startDebug">启动</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref,onBeforeUpdate,inject } from 'vue'
import { Codemirror } from 'vue-codemirror'
import { json } from '@codemirror/lang-json'
import {rosePineDawn} from 'thememirror';

export default {
  props: {
    caseId: {
     type: Number,
     required: true,
    },
    debugVisible: {
      type: Boolean,
      required: true,
    },
  },
  components: {
    Codemirror
  },
  setup(props, context) {
    const activeName = ref('sample')
    const activeNameSample = ref('responseBody')
    const debugId = ref(null)
    const socketio = inject('socketio')
    const status = ref('')
    const loading = ref(false)
    const httpSamples = ref([])
    const errorStr = ref('')
    const responseBodyCode = ref('')
    const responseHeaderCode = ref('')
    const requestBodyCode = ref('')
    const requestHeaderCode = ref('')
    const sampleResultCode = ref('')
    const extensions = [json(), rosePineDawn]

    onBeforeUpdate(() => {
      if (props.debugVisible){
        debugId.value = Number(new Date());
      }
      status.value = ''
      errorStr.value = ''
      httpSamples.value = []
      responseBodyCode.value = ''
      responseHeaderCode.value = ''
      requestBodyCode.value = ''
      requestHeaderCode.value = ''
      sampleResultCode.value = ''
    })

    socketio.on('caseDebugResult', (data) => {
      console.log(data)
      if (data.caseId==props.caseId && data.debugId==debugId.value){
        status.value = data.type
        if (data.type !== 'CONFIG'){
          loading.value = false
        }
        if (data.type == 'SAMPLE'){
          let result = data.result.httpSamples
          for (let i = 0; i < result.length; i++) {
            result[i].is_choose = false
            result[i].id = Math.random().toString(36).substring(2, 36)
          }
          httpSamples.value.push(...result)
        }
        if (data.type == 'ERROR'){
          errorStr.value = data.log
        }
      }
    })

    const closeDialog = () => {
      context.emit('debugDialogClose')
    }

    const startDebug = () => {
      loading.value = true
      status.value = ''
      socketio.emit('caseDebug', JSON.stringify({
        caseId: props.caseId,
        debugId: debugId.value,
      }))
    }

    const chooseSample = (item) => {
      for (let i = 0; i < httpSamples.value.length; i++) {
        if (httpSamples.value[i].id == item.id){
          httpSamples.value[i].is_choose = true
        } else {
          httpSamples.value[i].is_choose = false
        }
      }
      try {
        responseBodyCode.value = JSON.stringify(JSON.parse(item.responseData), null, '\t')
      } catch (error) {
        responseBodyCode.value = item.responseData
      }
      responseHeaderCode.value = item.responseHeader
      try {
        let requestUrl = item.method + '  ' + item.javaNetURL + '\n'
        requestBodyCode.value = requestUrl + JSON.stringify(JSON.parse(item.queryString), null, '\t') + '\n' +item.cookies
      } catch (error) {
        let requestUrl = item.method + '  ' + item.javaNetURL + '\n'
        requestBodyCode.value = requestUrl + item.queryString + '\n' +item.cookies
      }
      requestHeaderCode.value = item.requestHeader
      sampleResultCode.value = 'Thread Name:'+item.tn+'\n'+'Sample Start:'+new Date(item.ts)+'\n'+'Load time:'+item.lt+'\n'+
      'Connect Time:'+item.ct+'\n'+'Latency:'+item.t+'\n'+'Response code:'+item.rc+'\n'+'Response message:'+item.rm+'\n'+
      'Data type ("text"|"bin"|""):'+item.dt+'\n'
    }

    const clear = () => {
      httpSamples.value = []
      responseBodyCode.value = ''
      responseHeaderCode.value = ''
      requestBodyCode.value = ''
      requestHeaderCode.value = ''
      sampleResultCode.value = ''
  
    }
    
    return {
      closeDialog,
      activeName,
      startDebug,
      status,
      debugId,
      loading,
      httpSamples,
      errorStr,
      chooseSample,
      activeNameSample,
      responseBodyCode,
      responseHeaderCode,
      requestBodyCode,
      requestHeaderCode,
      sampleResultCode,
      extensions,
      clear,
    }
  },
}
</script>

<style lang="scss" scoped>
::v-deep .el-dialog__body {
  padding-top: 0;
  padding-bottom: 0;
}
::v-deep .el-tabs__content{
  padding: 0;
}

.body{
  height: 60vh;
  margin-bottom: 3px;
  ::v-deep .el-tabs__item.is-top.is-active {
    background: #faf4ed;
  }
  .list{
    height: 60vh;
    overflow: auto;
    background-color: #f5f7fa;
    border: 1px solid #e4e7ed;
    .item {
      height: 35px;
      line-height: 35px;
      font-weight: 500;
      cursor: pointer;
      border-radius: 4px;
      padding: 0 5px;
      margin: 2px;
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
    }
    .item-color {
      color: #409eff;
      background: #ecf5ff;
      border: 1px solid #b3d8ff;
    }
    .item-color-choose {
      color: #ecf5ff;
      background: #409eff;
      border: 1px solid #b3d8ff;
    }
  }
  .list::-webkit-scrollbar{
    display: none;
  }
  .list:hover::-webkit-scrollbar{
    width:6px;
    height:6px;
    display: block;
  }
  .list::-webkit-scrollbar-track{
    background: rgb(239, 239, 239);
    border-radius:2px;
  }
  .list::-webkit-scrollbar-thumb{
    background: #dad4d4;
    border-radius:10px;
  }
  .list::-webkit-scrollbar-thumb:hover{
    background: rgb(175, 173, 173);
  }
  .sampleTab{
    height: 100%;
  }
}
.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

</style>