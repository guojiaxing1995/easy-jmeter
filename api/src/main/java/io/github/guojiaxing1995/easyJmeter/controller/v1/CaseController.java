package io.github.guojiaxing1995.easyJmeter.controller.v1;

import io.github.guojiaxing1995.easyJmeter.dto.jcase.CreateOrUpdateCaseDTO;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import io.github.guojiaxing1995.easyJmeter.service.CaseService;
import io.github.guojiaxing1995.easyJmeter.vo.*;
import io.github.talelin.core.annotation.GroupRequired;
import io.github.talelin.core.annotation.LoginRequired;
import io.github.talelin.core.annotation.PermissionMeta;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.List;

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
    public CreatedVO creatCase(@RequestBody @Validated CreateOrUpdateCaseDTO validator) {
        caseService.createCase(validator);
        return new CreatedVO(1);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "用例更新", notes = "更新测试用例")
    @LoginRequired
    public UpdatedVO updateCase(@PathVariable("id") @Positive(message = "{id.positive}") Integer id, @RequestBody @Validated CreateOrUpdateCaseDTO validator) {
        CaseDO caseDO = caseService.getById(id);
        caseService.updateCase(caseDO, validator);
        return new UpdatedVO(2);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "用例删除", notes = "删除指定用例")
    @LoginRequired
    public DeletedVO deleteCase(@PathVariable("id") @Positive(message = "{id.positive}") Integer id){
        caseService.deleteCase(id);
        return new DeletedVO(3);
    }

    @GetMapping("")
    @ApiOperation(value = "用例列表", notes = "可根据id查询，id非必填")
    @LoginRequired
    @GroupRequired
    @PermissionMeta(value = "用例管理", module = "用例")
    public List<CaseInfoVO> getCases(@RequestParam(value = "id", required = false, defaultValue = "") Integer id) {
        return caseService.selectCase(id);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "用例详情", notes = "获取一个用例详情")
    @LoginRequired
    public CaseInfoPlusVO getCaseInfo(@PathVariable("id") @Positive(message = "{id.positive}") Integer id){
        return caseService.getCaseInfoById(id);
    }

}
