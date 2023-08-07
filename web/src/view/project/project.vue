<template>
    <div class="container">
      <div class="title" v-if="!editProjectId">
        <span>新建项目</span> <span class="back" @click="back"> <i class="iconfont icon-fanhui"></i> 返回 </span>
      </div>
      <div class="title" v-else>
        <span>修改项目</span> <span class="back" @click="back"> <i class="iconfont icon-fanhui"></i> 返回 </span>
      </div>
  
      <div class="wrap">
        <el-row v-loading="loading">
          <el-col :lg="16" :md="20" :sm="24" :xs="24">
            <el-form :model="project" status-icon ref="form" label-width="100px" @submit.prevent :rules="rules">
              <el-form-item label="项目名称" prop="name">
                <el-input v-model="project.name" placeholder="请填写项目名称"></el-input>
              </el-form-item>
              <el-form-item label="项目描述" prop="description">
                <el-input
                  type="textarea"
                  :autosize="{ minRows: 4, maxRows: 8 }"
                  placeholder="请输入项目描述"
                  v-model="project.description"
                >
                </el-input>
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
  
  export default {
    props: {
      editProjectId: {
        type: Number,
        default: null,
      },
    },
    setup(props, context) {
      const form = ref(null)
      const loading = ref(false)
      const project = reactive({ name: '', description: ''})
  
      const listAssign = (a, b) => Object.keys(a).forEach(key => {
        a[key] = b[key] || a[key]
      })
  
      /**
       * 表单规则验证
       */
      const { rules } = getRules()
  
      onMounted(() => {
        if (props.editProjectId) {
          getproject()
        }
      })
  
      const getproject = async () => {
        loading.value = true
        const res = await get(`/v1/project/${props.editProjectId}`, { showBackend: true })
        console.log(res)
        listAssign(project, res)
        loading.value = false
      }
  
      // 重置表单
      const resetForm = () => {
        form.value.resetFields()
      }
  
      const submitForm = async formName => {
        form.value.validate(async valid => {
          if (valid) {
            let res = {}
            if (props.editProjectId) {
              res = await put(`/v1/project/${props.editProjectId}`, project, { showBackend: true })
            } else {
              res = await post('/v1/project', project, { showBackend: true })
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
  
      const back = () => {
        context.emit('editClose')
      }
  
      return {
        back,
        project,
        form,
        rules,
        resetForm,
        submitForm,
        getproject,
        loading,
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
      description: [{ max: 1000, message: '长度在1000个字符内', trigger: 'blur' }],
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
  }
  </style>
  