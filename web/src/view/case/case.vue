<template>
  <div class="container">
    <div class="title" v-if="!editCaseId">
      <span>新建用例</span> <span class="back" @click="back"> <i class="iconfont icon-fanhui"></i> 返回 </span>
    </div>
    <div class="title" v-else>
      <span>修改用例</span> <span class="back" @click="back"> <i class="iconfont icon-fanhui"></i> 返回 </span>
    </div>
  
    <div class="wrap">
      <el-row v-loading="loading">
        <el-col :lg="16" :md="20" :sm="24" :xs="24">
          <el-form :model="jcase" status-icon ref="form" label-width="100px" @submit.prevent :rules="rules">
            <el-form-item label="用例名称" prop="name">
              <el-input v-model="jcase.name" placeholder="请填写用例名称"></el-input>
            </el-form-item>
            <el-form-item label="项目" prop="project">
              <el-select v-model="jcase.project" placeholder="请选择工程" clearable filterable style="width: 100%;">
                <el-option v-for="item in projects" :key="item.value" :label="item.name" :value="item.id"/>
              </el-select>
            </el-form-item>
            <el-form-item label="jmx文件" prop="jmx">
              <el-table :data="jmxTable" border stripe show-overflow-tooltip 
                        :header-row-style="{height: '20px'}"
                        :row-style="{height: '20px'}">
                <el-table-column :resizable="false" prop="name" label="文件名" />
                <el-table-column :resizable="false" prop="size" label="文件大小" width="160" />
                <el-table-column :resizable="false" label="操作" width="255">
                  <template #default="scope">
                    <el-button plain size="small" type="danger" @click="deleteFile(scope.row.id,'jmx')">删除</el-button>
                    <el-button plain size="small" type="primary" @click="downloadFile(scope.row.url)" style="margin-left: 30px;">下载</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-upload
                :action="actionUrl"
                :headers="myHeaders"
                :show-file-list="false"
                :on-error="uploadFileError"
                :on-success="(file,fileList)=>{return uploadFileSuccess(file,fileList,'jmx')}"
                :on-change="(file,fileList)=>{return uploadFileChange(file,fileList,'jmx')}"
              >
                <template #default>
                  <div class="upload-btn">
                    <el-button plain type="primary" v-loading="uploadLoading.jmx">
                      上传<el-icon class="el-icon--right"><Upload /></el-icon>
                    </el-button>
                  </div>
                </template>
              </el-upload>
            </el-form-item>
            <el-form-item label="csv文件" prop="csv">
              <el-table :data="csvTable" border stripe show-overflow-tooltip 
                        :header-row-style="{height: '20px'}"
                        :row-style="{height: '20px'}">
                <el-table-column :resizable="false" prop="name" label="文件名" />
                <el-table-column :resizable="false" prop="size" label="文件大小" width="160" />
                <el-table-column :resizable="false" label="操作" width="255">
                  <template #default="scope">
                    <el-button plain size="small" type="danger" @click="deleteFile(scope.row.id,'csv')">删除</el-button>
                    <el-button plain size="small" type="primary" @click="downloadFile(scope.row.url)" style="margin-left: 30px;">下载</el-button>
                    <el-switch v-model="scope.row.cut" @change="(val) => cutFile(val, scope.row.id)" inline-prompt active-text="切分" inactive-text="不切分"/>
                  </template>
                </el-table-column>
              </el-table>
              <el-upload
                :action="actionUrl"
                :headers="myHeaders"
                :show-file-list="false"
                :on-error="uploadFileError"
                :on-success="(file,fileList)=>{return uploadFileSuccess(file,fileList,'csv')}"
                :on-change="(file,fileList)=>{return uploadFileChange(file,fileList,'csv')}"
              >
                <template #default>
                  <div class="upload-btn">
                    <el-button plain type="primary" v-loading="uploadLoading.csv">
                      上传<el-icon class="el-icon--right"><Upload /></el-icon>
                    </el-button>
                  </div>
                </template>
              </el-upload>
            </el-form-item>
            <el-form-item label="jar文件" prop="jar">
              <el-table :data="jarTable" border stripe show-overflow-tooltip 
                        :header-row-style="{height: '20px'}"
                        :row-style="{height: '20px'}">
                <el-table-column :resizable="false" prop="name" label="文件名" />
                <el-table-column :resizable="false" prop="size" label="文件大小" width="160" />
                <el-table-column :resizable="false" label="操作" width="255">
                  <template #default="scope">
                    <el-button plain size="small" type="danger" @click="deleteFile(scope.row.id,'jar')">删除</el-button>
                    <el-button plain size="small" type="primary" @click="downloadFile(scope.row.url)" style="margin-left: 30px;">下载</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-upload
                :action="actionUrl"
                :headers="myHeaders"
                :show-file-list="false"
                :on-error="uploadFileError"
                :on-success="(file,fileList)=>{return uploadFileSuccess(file,fileList,'jar')}"
                :on-change="(file,fileList)=>{return uploadFileChange(file,fileList,'jar')}"
              >
                <template #default>
                  <div class="upload-btn">
                    <el-button plain type="primary" v-loading="uploadLoading.jar">
                      上传<el-icon class="el-icon--right"><Upload /></el-icon>
                    </el-button>
                  </div>
                </template>
              </el-upload>
            </el-form-item>
            <el-form-item label="用例描述" prop="description">
              <el-input placeholder="请输入用例描述" v-model="jcase.description" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }"></el-input>
            </el-form-item>
            <el-form-item class="submit">
              <el-button type="primary" @click="submitForm">保 存</el-button>
              <el-button @click="resetForm">重 置</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </div>
  </div>
</template>
  
  <script>
  import { reactive, ref, onMounted } from 'vue'
  import { ElMessage } from 'element-plus'
  import { get,put,post } from '@/lin/plugin/axios'
  import { Upload } from '@element-plus/icons-vue'
  
  export default {
    components: {
      Upload,
    },
    props: {
      editCaseId: {
        type: Number,
        default: null,
      },
      projects: {
        type: Array,
        required: true,
      },
    },
    setup(props, context) {
      const form = ref(null)
      const loading = ref(false)
      const jcase = reactive({ name: '', project:'',jmx:'', csv:'', jar:'', description: ''})
      const actionUrl = process.env.VUE_APP_BASE_URL + 'v1/file/uploadFile'
      const myHeaders = {Authorization: localStorage.getItem('access_token')}
      const jmxTable = ref([])
      const csvTable = ref([])
      const jarTable = ref([])
      const uploadLoading = reactive({ jmx: false, csv:false, jar: false})
  
      const listAssign = (a, b) => Object.keys(a).forEach(key => {
        a[key] = b[key] || a[key]
      })
  
      /**
       * 表单规则验证
       */
      const { rules } = getRules()
  
      onMounted(() => {
        if (props.editCaseId) {
          getjcase()
        }
      })

      const uploadFileError = (error) => {
        ElMessage.error(JSON.parse(error.message).message)
      }

      const uploadFileSuccess = (file,fileList,type) => {
        if(type == 'jmx') {
          if(jmxTable.value.length>=1){
            ElMessage.warning("只允许上传一个脚本文件")
          } else {
            jmxTable.value.push(file)
          }
        } else if(type == 'csv') {
          csvTable.value.push(file)
        } else if(type == 'jar') {
          jarTable.value.push(file)
        }
      }

      const uploadFileChange = (file,fileList,type) => {
        if(type == 'jmx') {
          uploadLoading.jmx = true
          if(file.percentage == 100){
            uploadLoading.jmx= false
          }
        } else if(type == 'csv') {
          uploadLoading.csv = true
          if(file.percentage == 100){
            uploadLoading.csv= false
          }
        } else if(type == 'jar') {
          uploadLoading.jar = true
          if(file.percentage == 100){
            uploadLoading.jar= false
          }
        }
      }

      const deleteFile = (id, type) => {
        if(type == 'jmx') {
          for (let i = 0; i < jmxTable.value.length; i++) {
            if(jmxTable.value[i].id == id) {
              jmxTable.value.splice(i, 1)
            }
          }
        } else if(type == 'csv') {
          for (let i = 0; i < csvTable.value.length; i++) {
            if(csvTable.value[i].id == id) {
              csvTable.value.splice(i, 1)
            }
          }
        } else if(type == 'jar') {
          for (let i = 0; i < jarTable.value.length; i++) {
            if(jarTable.value[i].id == id) {
              jarTable.value.splice(i, 1)
            }
          }
        }
      }

      const downloadFile = (url) => {
        let link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        document.body.appendChild(link)
        link.click()
      }


      const setFileStr = () => {
        jcase.jmx = jmxTable.value.map(element => element.id).join(',')
        jcase.csv = csvTable.value.map(element => element.id).join(',')
        jcase.jar = jarTable.value.map(element => element.id).join(',')
      }
  
      const getjcase = async () => {
        loading.value = true
        const res = await get(`/v1/case/${props.editCaseId}`, { showBackend: true })
        jmxTable.value = res.jmx_file_list
        csvTable.value = res.csv_file_list
        jarTable.value = res.jar_file_list
        console.log(res)
        listAssign(jcase, res)
        loading.value = false
      }
  
      // 重置表单
      const resetForm = () => {
        form.value.resetFields()
        jmxTable.value = []
        csvTable.value = []
        jarTable.value = []
      }
  
      const submitForm = async formName => {
        setFileStr()
        form.value.validate(async valid => {
          if (valid) {
            let res = {}
            if (props.editCaseId) {
              res = await put(`/v1/case/${props.editCaseId}`, jcase, { showBackend: true })
            } else {
              res = await post('/v1/case', jcase, { showBackend: true })
              resetForm(formName)
            }
            context.emit('editClose')
            if (res.code < window.MAX_SUCCESS_CODE) {
              ElMessage.success(`${res.message}`)
            }
          } else {
            console.error('error submit!!')
            ElMessage.error('请检查输入信息')
          }
        })
      }
      
      const cutFile = async (val, id) => {
        await put('/v1/file/cut/' + id + '?cut=' + val, { showBackend: true })
      }

      const back = () => {
        context.emit('editClose')
      }
  
      return {
        back,
        jcase,
        actionUrl,
        myHeaders,
        form,
        rules,
        resetForm,
        submitForm,
        getjcase,
        uploadFileError,
        jmxTable,
        csvTable,
        jarTable,
        uploadFileSuccess,
        uploadFileChange,
        uploadLoading,
        deleteFile,
        setFileStr,
        loading,
        cutFile,
        downloadFile,
      }
    },
  }
  
  /**
   * 表单验证规则
   */
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
      name: [{ validator: checkInfo, trigger: 'blur', required: true },{ max: 50, message: '长度在50个字符内', trigger: 'blur' }],
      project: [{ validator: checkInfo, trigger: 'blur', required: true }],
      description: [{ max: 500, message: '长度在500个字符内', trigger: 'blur' }],
      jmx: [{ validator: checkInfo, trigger: 'blur', required: true }],
    }
    return { rules }
  }
  </script>
  
  <style lang="scss" scoped>
  .container {
    .title {
      height: 59px;
      line-height: 59px;
      color: $parent-title-color;
      font-size: 16px;
      font-weight: 500;
      text-indent: 40px;
      border-bottom: 1px solid #dae1ec;
  
      .back {
        float: right;
        margin-right: 40px;
        cursor: pointer;
      }
    }
  
    .wrap {
      padding: 20px;
    }
  
    .submit {
      float: left;
    }

    .el-table--border::after {
      top: -1px;
      right: 0;
      width: 2px;
      height: 100%;
    }

    .upload-btn {
      margin-top: 6px;
    }

    .el-switch {
      margin-left: 30px;
    }

    .el-button {
      margin: auto 0;
    }
    ::v-deep .el-table__body-wrapper .el-scrollbar__bar {
      height: 0;
    }
  }
  </style>
  