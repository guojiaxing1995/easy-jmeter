package io.github.guojiaxing1995.easyJmeter.controller.v1;

import io.github.guojiaxing1995.easyJmeter.dto.jcase.CreateOrUpdateCaseDTO;
import io.github.guojiaxing1995.easyJmeter.service.CaseService;
import io.github.guojiaxing1995.easyJmeter.vo.CreatedVO;
import io.github.talelin.core.annotation.LoginRequired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/case")
@Api(tags = "用例管理")
@Validated
public class CaseController {

    @Autowired
    private CaseService caseService;

    @PostMapping("")
    @ApiOperation(value = "用例新增", notes = "创建测试用例")
    @LoginRequired
    public CreatedVO creatCase(@RequestBody @Validated CreateOrUpdateCaseDTO validator){
        caseService.createCase(validator);
        return new CreatedVO(1);
    }
}
