<template>
    <div  class="container">
      <el-row :gutter="35" class="search">
        <el-col class="search-item" :span="8"><el-form-item label="应用名称"><el-input v-model="search.application" placeholder="请输入application" clearable /></el-form-item></el-col>
        <el-col class="search-item" :span="8"><el-form-item label="标记名称"><el-input v-model="search.jcase" placeholder="请输入tags" clearable /></el-form-item></el-col>
        <el-col class="search-item" :span="8"><el-form-item label="测试时间">
            <el-date-picker v-model="dateValue" type="datetimerange" range-separator="To" start-placeholder="开始时间" end-placeholder="结束时间" 
            clearable value-format="YYYY-MM-DD HH:mm:ss" @change="selectDate"/></el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="35" class="search">
        <el-col class="search-item" :span="8"><el-form-item label="测试备注">
          <el-input v-model="search.application" placeholder="请输入textTitle" clearable />
        </el-form-item></el-col>
        <el-col class="search-item" :span="8"></el-col>
        <el-col class="search-item search-btn" :span="8" >
          <el-button type="success" @click="" class="btn">归档</el-button>
          <el-button type="primary" @click="getDatas" class="btn">查询</el-button>
        </el-col>
      </el-row>
      <el-table :data="datas" v-loading="loading" >
        <el-table-column type="selection"/>
        <el-table-column prop="application" label="application" width="110" show-overflow-tooltip></el-table-column>
        <el-table-column prop="tags" label="tags" width="100" show-overflow-tooltip></el-table-column>
        <el-table-column prop="sample" label="sample" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="avg" label="avg" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="median" label="median" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="pct90.0" label="90%pct" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="pct95.0" label="95%pct" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="pct99.0" label="99%pct" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="min" label="min" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="max" label="max" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="error" label="error%" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="tps" label="tps" width="90" show-overflow-tooltip></el-table-column>
        <el-table-column prop="rb" label="received B/sec" width="125" show-overflow-tooltip></el-table-column>
        <el-table-column prop="sb" label="sent B/sec" width="105" show-overflow-tooltip></el-table-column>
        <el-table-column prop="text" label="text" width="100" show-overflow-tooltip></el-table-column>
        <el-table-column prop="" label="测试时间" width="90" show-overflow-tooltip></el-table-column>
      </el-table>
    </div>
  </template>

  <script>
    import Utils from 'lin/util/util'
    import { onMounted, ref ,reactive, onActivated } from 'vue'
    import { post } from '@/lin/plugin/axios'
    import { ElMessageBox, ElMessage } from 'element-plus'
  
    export default {
      components: {
      },
      setup() {
        const datas = ref([])
        const loading = ref(false)
        const dateValue = ref([])
        const search = reactive({ application:'',tags:'',text:'',start_time:'',end_time:''})
  
        onMounted(() => {
          setDefaultDateRange()
          getDatas()
        })
  
        const getDatas = async () => {
          let res
          try {
            loading.value = true
            res = await post('/v1/task/aggregateReport/search', search, { showBackend: true })
            datas.value = res
            loading.value = false
          } catch (error) {
            loading.value = false
            datas.value = []
          }
        }

        const selectDate = (date) => {
            if(date == null) {
                search.start_time = ''
                search.end_time = ''
            return
            }
            // 处理日期显示
            let originalDate = new Date(date[1])
            originalDate.setDate(originalDate.getDate() + 1)
            originalDate.setUTCHours(23, 59, 59)
            let modifiedDateTimeString = originalDate.toISOString().slice(0, 19).replace('T', ' ')
            dateValue.value[1] = modifiedDateTimeString

            search.start_time = dateValue.value[0]
            search.end_time = dateValue.value[1]
        }

        const setDefaultDateRange = async () => {
            const today = new Date()
            const startOfDay = new Date(today)
            startOfDay.setUTCHours(0, 0, 0, 0) // 设置为今天的零点

            const endOfDay = new Date(today);
            endOfDay.setUTCHours(23, 59, 59, 999) // 设置为今天的23:59:59
            
            dateValue.value[0] = startOfDay.toISOString().slice(0, 19).replace('T', ' ')
            dateValue.value[1] = endOfDay.toISOString().slice(0, 19).replace('T', ' ')
            search.start_time = dateValue.value[0]
            search.end_time = dateValue.value[1]
        }

        return {
          datas,
          loading,
          getDatas,
          search,
          selectDate,
          dateValue,
          setDefaultDateRange,
        }
  
      },
    }
  </script>
  
  <style lang="scss" scoped>
  .container {
    padding: 20px 30px 0 30px;
      .search{
        margin: 0 10px;
      .search-item {
        height: 50px;
        line-height: 50px;
        vertical-align: middle;
      }
      .search-btn {
        text-align: right;
      }
      .btn {
        vertical-align: top;
      }
    }

    ::v-deep .is-vertical {
      width: 0;
      top: 2px;
    }
    
  }
  </style>