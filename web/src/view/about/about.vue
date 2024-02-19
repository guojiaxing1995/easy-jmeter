<template>
  <div class="container">
    <el-row :gutter="25">
      <el-col :span="6">
        <div class="first-head">
          <el-text type="primary" truncated class="title-hello">{{hello}}！ {{ name }}</el-text>
          <el-text type="info" class="title-info">欢迎使用性能测试平台</el-text>
        </div>
      </el-col>
      <el-col :span="18">
        <div class="first-head-total">
          <div class="total-item">
            <div class="total-item-title">项目数</div>
            <div class="total-item-value">{{totalCount.projectNum}}</div>
          </div>
          <div class="total-item">
            <div class="total-item-title">用例数</div>
            <div class="total-item-value">{{totalCount.caseNum}}</div>
          </div>
          <div class="total-item">
            <div class="total-item-title">压力机数</div>
            <div class="total-item-value">{{totalCount.machineNum}}</div>
          </div>
          <div class="total-item">
            <div class="total-item-title">测试次数</div>
            <div class="total-item-value">{{totalCount.taskNum}}</div>
          </div>
          <div class="total-item">
            <div class="total-item-title">压测总时长</div>
            <div class="total-item-value">{{timeDeal(totalCount.durationSum)}}</div>
          </div>
          <div class="total-item">
            <div class="total-item-title">总请求数</div>
            <div class="total-item-value">{{totalCount.totalSamples}}</div>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="25">
      <el-col :span="6">
        <div class="box">
          <div class="case-search"><el-input placeholder="输入用例名称进行过滤" v-model="caseName" clearable></el-input></div>
          <div class="case-list">
            <div class="case-item" v-for="item in cases" :key="item.id" v-bind:class="{'item-color': !item.is_choose, 'item-color-choose': item.is_choose}" @click="handleChooseCase(item)">{{ item.name }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="18">
        <div class="box">
          <div v-if="selected" class="statistics">
            <div class="first-head-total">
              <div class="total-item-case">
                <div class="total-item-title-case">平均响应时间</div>
                <div class="total-item-value-case" v-if="parseFloat(statisticsData.average_response_time)">{{parseFloat(statisticsData.average_response_time).toFixed(2)}}</div>
                <div class="total-item-value-case" v-else>--</div>
              </div>
              <div class="total-item-case">
                <div class="total-item-title-case">90th响应时间</div>
                <div class="total-item-value-case" v-if="parseFloat(statisticsData.average90th_response_time)">{{parseFloat(statisticsData.average90th_response_time).toFixed(2)}}</div>
                <div class="total-item-value-case" v-else>--</div>
              </div>
              <div class="total-item-case">
                <div class="total-item-title-case">平均错误率</div>
                <div class="total-item-value-case" v-if="parseFloat(statisticsData.average_error_rate)">{{parseFloat(statisticsData.average_error_rate).toFixed(2)}}%</div>
                <div class="total-item-value-case" v-else>--</div>
              </div>
              <div class="total-item-case">
                <div class="total-item-title-case">平均吞吐量</div>
                <div class="total-item-value-case" v-if="parseFloat(statisticsData.average_throughput)">{{parseFloat(statisticsData.average_throughput).toFixed(2)}}</div>
                <div class="total-item-value-case" v-else>--</div>
              </div>
              <div class="total-item-case">
                <div class="total-item-title-case">测试次数</div>
                <div class="total-item-value-case">{{statisticsData.task_num}}</div>
              </div>
              <div class="total-item-case">
                <div class="total-item-title-case">压测总时长</div>
                <div class="total-item-value-case">{{timeDeal(statisticsData.duration_sum)}}</div>
              </div>
              <div class="total-item-case">
                <div class="total-item-title-case">总请求数</div>
                <div class="total-item-value-case">{{statisticsData.samples_sum}}</div>
              </div>
            </div>
            <div id="respoonseTimesChart"  class="report-echarts"></div>
            <div id="throughputAndErrorChart"  class="report-echarts"></div>
          </div>
          <div v-else class="statistics"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, computed, watch, getCurrentInstance } from 'vue'
import { get } from '@/lin/plugin/axios'
import Utils from 'lin/util/util'

export default {
  setup() {
    const hello = ref('')
    const name = ref('')
    const totalCount = ref({"caseNum": 0,"machineNum": 0,"durationSum": 0,"projectNum": 0,"taskNum": 0})
    const caseName = ref('')
    const cases = ref([])
    const casesOriginal = ref([])
    const selected = ref(null)
    const statisticsData = ref([])
    const {proxy} = getCurrentInstance()

    onMounted(() => {
      getGreeting()
      getUserInfo()
      getTotalCount()
      getCases()
    })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', resizeHandler)
    })

    const getUserInfo = async () => {
      let res
      try {
        res = await get('/cms/user/information', { showBackend: true })
        if (res.nickname !== null) {
          name.value = res.nickname
        } else {
          name.value = res.username
        }
      } catch (error) {
      }
    }

    const getTotalCount = async () => {
      let res
      try {
        res = await get('/v1/common/total', { showBackend: true })
        totalCount.value = res
      } catch (error) {
      }
    }

    const getCases = async () => {
      let res
      try {
        res = await get('/v1/case', { showBackend: true })
        for (let i = 0; i < res.length; i++) {
          res[i].is_choose = false
        }
        casesOriginal.value = res
        cases.value = res
        if (cases.value) {
          handleChooseCase(cases.value[0])
        }
      } catch (error) {
        casesOriginal.value = []
      }
    }

    const handleChooseCase = (item) => {
      selected.value = item.id
      for (let i = 0; i < cases.value.length; i++) {
        if (cases.value[i].id === item.id) {
          cases.value[i].is_choose = true
        } else {
          cases.value[i].is_choose = false
        }
      }
      for (let i = 0; i < casesOriginal.value.length; i++) {
        if (casesOriginal.value[i].id === item.id) {
          casesOriginal.value[i].is_choose = true
        } else {
          casesOriginal.value[i].is_choose = false
        }
      }
    }

    const searchCases = () => {
      cases.value = []
      for (let i = 0; i < casesOriginal.value.length; i++) {
        if (casesOriginal.value[i].name.includes(caseName.value, { ignoreCase: true })) {
          cases.value.push(casesOriginal.value[i])
        }
      }
    }

    const getStatisticsById = async () => {
      let res
      try {
        res = await get(`/v1/common/statistics/${selected.value}` , { showBackend: true })
        statisticsData.value = res
        setTimeout( function(){
          initChart()
          setChartOption()
        }, 500 )
      } catch (error) {
      }
    }

    const initChart = () => {
      let respoonseTimesEChart = proxy.$echarts.getInstanceByDom(document.getElementById('respoonseTimesChart'))
      if (respoonseTimesEChart == null) {
        respoonseTimesEChart= proxy.$echarts.init(document.getElementById('respoonseTimesChart'))
      }
      let throughputAndErrorEChart = proxy.$echarts.getInstanceByDom(document.getElementById('throughputAndErrorChart'))
      if (throughputAndErrorEChart == null) {
        throughputAndErrorEChart= proxy.$echarts.init(document.getElementById('throughputAndErrorChart'))
      }
      window.addEventListener("resize", resizeHandler)
    }

    const resizeHandler = () => {
      let respoonseTimesEChart = proxy.$echarts.getInstanceByDom(document.getElementById('respoonseTimesChart'))
      if (respoonseTimesEChart !== null) {
        respoonseTimesEChart.resize()
      }
      let throughputAndErrorEChart = proxy.$echarts.getInstanceByDom(document.getElementById('throughputAndErrorChart'))
      if (throughputAndErrorEChart !== null) {
        throughputAndErrorEChart.resize()
      }
    }

    const setChartOption = () => {
      let responseTimesOption = getOption("responseTimeInfos")
      let respoonseTimesEChart = proxy.$echarts.getInstanceByDom(document.getElementById('respoonseTimesChart'))
      respoonseTimesEChart.setOption(responseTimesOption, true)

      let throughputAndErrorOption = getOption("throughputAndErrorInfos")
      let throughputAndErrorEChart = proxy.$echarts.getInstanceByDom(document.getElementById('throughputAndErrorChart'))
      throughputAndErrorEChart.setOption(throughputAndErrorOption, true)
    }

    const getOption = (infos) => {
      const option = {
        title: {
          text: "",
          left: "center",
        },
        grid: {
          left: '3%',
          right: '2%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          axisLabel: {
            interval: 'auto',
            rotate: 15,
          },
          data: []

        },
        yAxis: {
          name: '',
          scale:true
        },
        tooltip: {
          trigger: 'axis'
        },
        toolbox: {
          show: true,
          top: 20,
          feature: {
            dataZoom: {
              yAxisIndex: 'none',
              title: {
                zoom: '区域缩放',
                back: '缩放还原'
              },
            },
          }
        },
        legend: {
          data: [],
          bottom: -5,
        },
        series: [],
      }
      option.series = statisticsData.value.graph_data[infos].series
      option.title.text = statisticsData.value.graph_data[infos].titleCN
      option.legend.data = statisticsData.value.graph_data[infos].labels
      option.yAxis.name = statisticsData.value.graph_data[infos].yName
      option.xAxis.data = statisticsData.value.graph_data[infos].times
      return option
    }

    const _debounce =Utils.debounce(()=>{
      searchCases()
    }, 800)

    watch(caseName, () => {
      _debounce()
    })

    watch(selected, () => {
      getStatisticsById()
    })

    const getGreeting = () => {
      const currentTime = new Date();
      const currentHour = currentTime.getHours();
      if (currentHour < 6) {
        hello.value = '凌晨好'
      } else if (currentHour < 9) {
        hello.value = '早上好'
      } else if (currentHour < 12) {
        hello.value = '上午好'
      } else if (currentHour < 14) {
        hello.value = '中午好'
      } else if (currentHour < 18) {
        hello.value = '下午好'
      } else if (currentHour < 24) {
        hello.value = '晚上好'
      } else {
        hello.value = '你好'
      }
    }

    const timeDeal = computed(() => (time) => {
      const hours = Math.floor(time / 3600)
      const minutes = Math.floor((time % 3600) / 60)
      const seconds = time % 60
      let result = ""
      if (hours > 0) {
        result += `${hours}小时`
      }
      if (hours < 1) {
        if (minutes > 0) {
          result += `${minutes}分钟`
        }
        if (seconds > 0 || result === "") {
          result += `${seconds}秒`
        }
      } else {
        if (minutes > 0) {
          result += `${minutes}分钟`
        }
      }
      return result.trim() || '0秒'
    })


    return {
      getGreeting,
      hello,
      getUserInfo,
      name,
      getTotalCount,
      totalCount,
      timeDeal,
      caseName,
      getCases,
      cases,
      casesOriginal,
      handleChooseCase,
      searchCases,
      getStatisticsById,
      statisticsData,
      selected,
      setChartOption,
      initChart,
      resizeHandler,
      getOption,
    }
  },
}
</script>

<style scoped lang="scss">
.container {
  padding: 20px;
  .first-head {
    height: 10vh;
    width: 100%;
    vertical-align: middle;
    text-align: center;
    .title-hello {
      font-size: 18px;
      margin-top: 3vh;
      display: block;
    }
    .title-info {
      display: block;
      font-size: 14px;
      margin-top: 8px;
    }
  }
  .first-head-total {
    height: 10vh;
    width: 100%;
    display: flex;
    flex-direction: row;
    justify-content: flex-end;
    .total-item {
      height: 100%;
      width: 165px;
      margin-left: 10px;
      text-align: center;
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
      .total-item-title {
        font-size: 16px;
        color: #6e6d6d;
      }
     .total-item-value {
        font-size: 18px;
        margin-top: 8px;
        color: #4577ff;
      }

    }
    .total-item-case {
      height: 100%;
      width: 135px;
      margin-left: 8px;
      text-align: center;
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
      .total-item-title-case {
        font-size: 14px;
        color: #6e6d6d;
      }
     .total-item-value-case {
        font-size: 16px;
        margin-top: 8px;
        color: #4577ff;
      }

    }
  }
  .report-echarts {
    width: 100%; 
    height: 28vh;
    margin: 1VH 0;
  }
  .case-list {
    width: 100%;
    overflow: auto;
    height: calc(70vh - 35px);
    .case-item {
      height: 35px;
      line-height: 35px;
      font-weight: 500;
      cursor: pointer;
      border-radius: 4px;
      padding: 0 5px;
      margin: 3px;
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
    }
    .item-color {
      color: #88A1D7;
      background: #EBEFF8;
      border:1px solid #88A1D7;
    }
    .item-color-choose {
      color: #b1c6f5;
      background: #4b7ff0;
      border:1px solid #b1c6f5;
    }
  }
  .case-list::-webkit-scrollbar{
    display: none;
  }
  .case-list:hover::-webkit-scrollbar{
    width:6px;
    height:6px;
    display: block;
  }
  .case-list::-webkit-scrollbar-track{
    background: rgb(239, 239, 239);
    border-radius:2px;
  }
  .case-list::-webkit-scrollbar-thumb{
    background: #dad4d4;
    border-radius:10px;
  }
  .case-list::-webkit-scrollbar-thumb:hover{
    background: rgb(175, 173, 173);
  }
  .case-search {
    padding: 3px 3px 0 3px;
  }
  .box {
    width: 100%;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 14px 0 #f3f3f3;
    height: 70vh;
    .statistics {
      height: 100%;
    }
  }
}

</style>
