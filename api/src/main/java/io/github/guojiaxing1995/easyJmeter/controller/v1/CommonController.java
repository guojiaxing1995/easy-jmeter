package io.github.guojiaxing1995.easyJmeter.controller.v1;

import io.github.guojiaxing1995.easyJmeter.model.StatisticsDO;
import io.github.guojiaxing1995.easyJmeter.service.CommonService;
import io.github.talelin.core.annotation.LoginRequired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/common")
@Api(tags = "公用接口")
@Validated
public class CommonController {

    @Autowired
    private CommonService commonService;

    @GetMapping("/enum")
    @ApiOperation(value = "", notes = "返回枚举类型")
    @LoginRequired
    public Map<String, Object> getAllEnum() {
        return commonService.getEnum();
    }

    @GetMapping("/total")
    @ApiOperation(value = "获取统计数据", notes = "获取系统的 项目数量、用例数量、压力机数量、测试次数、测试时间数据")
    @LoginRequired
    public Map<String, Object> getVersion() {
        return commonService.getTotal();
    }

    @GetMapping("/statistics/{id}")
    @ApiOperation(value = "获取任务统计数据", notes = "根据id获取任务的 统计数据")
    @LoginRequired
    public StatisticsDO getStatisticsById(@PathVariable(value = "id") String id) {
        return commonService.getStatisticsById(id);
    }
}
