<template>
    <div class="container" v-if="!showEdit">
      <div class="header">
        <div class="newBtn"><el-button type="primary" @click="handleCreate">新 增</el-button></div>
        <div class="search">
          <el-select v-model="projectId" placeholder="请选择工程">
            <el-option v-for="item in projects" :key="item.value" :label="item.name" :value="item.id"/>
          </el-select>
          <el-input placeholder="请输入用例名称查询" v-model="name" clearable></el-input>
        </div>
      </div>
      <div class="list">
        <div class="case">
          <div class="line line-name">
            <div class="name">测试用例名称</div>
            <div class="last-run">2023-3-31 10:56:25  李淳罡</div>
          </div>
          <div class="line">1</div>
          <div class="line">1</div>
        </div>
        <div class="case">
          <div class="line line-name">
            <div class="name">测试用例名称</div>
            <div class="last-run">2023-3-31 10:56:25  李淳罡</div>
          </div>
          <div class="line">1</div>
          <div class="line">1</div>
        </div>
        <div class="case">
          <div class="line line-name">
            <div class="name">测试用例名称</div>
            <div class="last-run">2023-3-31 10:56:25  李淳罡</div>
          </div>
          <div class="line">1</div>
          <div class="line">1</div>
        </div>
        <div class="case">
          <div class="line line-name">
            <div class="name">测试用例名称</div>
            <div class="last-run">2023-3-31 10:56:25  李淳罡</div>
          </div>
          <div class="line">1</div>
          <div class="line">1</div>
        </div>
        <div class="case">
          <div class="line line-name">
            <div class="name">测试用例名称</div>
            <div class="last-run">2023-3-31 10:56:25  李淳罡</div>
          </div>
          <div class="line">1</div>
          <div class="line">1</div>
        </div>
        <div class="case">
          <div class="line line-name">
            <div class="name">测试用例名称</div>
            <div class="last-run">2023-3-31 10:56:25  李淳罡</div>
          </div>
          <div class="line">1</div>
          <div class="line">1</div>
        </div>
        <div class="case">
          <div class="line line-name">
            <div class="name">测试用例名称</div>
            <div class="last-run">2023-3-31 10:56:25  李淳罡</div>
          </div>
          <div class="line">1</div>
          <div class="line">1</div>
        </div>
      </div>

    </div>
  </template>
  
  <script>
    import Utils from 'lin/util/util'
    import { onMounted, ref ,watch } from 'vue'
    import { get,_delete } from '@/lin/plugin/axios'
  
    export default {
      components: {

      },
      setup() {
        const showEdit = ref(false)
        const name = ref('')
        const cases = ref([])
        const loading = ref(false)
        const editcaseId = ref(null)
        const projects = ref([])
        const projectId = ref(null)
  
        onMounted(() => {
            getProjects()
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

        
        const handleCreate = () => {
          showEdit.value = true
          editcaseId.value = null
        }
  
        return {
          cases,
          loading,
          editcaseId,
          projects,
          projectId,
          name,
          showEdit,
          handleCreate,
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

    .case {
      width: calc(50% - 32px);
      margin: 15px;
      border: 1px solid $parent-title-color;
      border-radius: 5px;

      .line{
        height: 45px;
        line-height: 45px;
      }
      .line-name {
        display: flex;
        justify-content: space-between;
        .name {
          margin: 0 0 0 8px;
          font-size: 20px;
        }
        .last-run {
          margin: 0 8px 0 0;
          font-size: 10px;
        }
      }
    }
  }
  </style>