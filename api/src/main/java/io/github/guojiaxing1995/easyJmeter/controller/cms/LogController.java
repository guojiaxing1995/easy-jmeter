package io.github.guojiaxing1995.easyJmeter.controller.cms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.common.util.PageUtil;
import io.github.guojiaxing1995.easyJmeter.dto.log.QueryLogDTO;
import io.github.guojiaxing1995.easyJmeter.dto.query.BasePageDTO;
import io.github.guojiaxing1995.easyJmeter.model.LogDO;
import io.github.guojiaxing1995.easyJmeter.service.LogService;
import io.github.guojiaxing1995.easyJmeter.vo.PageResponseVO;
import io.github.talelin.core.annotation.GroupRequired;
import io.github.talelin.core.annotation.PermissionMeta;
import io.github.talelin.core.annotation.PermissionModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pedro@TaleLin
 * @author Juzi@TaleLin
 */
@RestController
@RequestMapping("/cms/log")
@PermissionModule(value = "日志")
@Validated
public class LogController {
    @Autowired
    private LogService logService;

    @GetMapping("")
    @GroupRequired
    @PermissionMeta(value = "查询所有日志")
    public PageResponseVO<LogDO> getLogs(QueryLogDTO dto) {
        IPage<LogDO> iPage = logService.getLogPage(
                dto.getPage(), dto.getCount(),
                dto.getName(), dto.getStart(),
                dto.getEnd()
        );
        return PageUtil.build(iPage);
    }

    @GetMapping("/search")
    @GroupRequired
    @PermissionMeta(value = "搜索日志")
    public PageResponseVO<LogDO> searchLogs(QueryLogDTO dto) {
        IPage<LogDO> iPage = logService.searchLogPage(
                dto.getPage(), dto.getCount(),
                dto.getName(), dto.getKeyword(),
                dto.getStart(), dto.getEnd()
        );
        return PageUtil.build(iPage);
    }

    @GetMapping("/users")
    @GroupRequired
    @PermissionMeta(value = "查询日志记录的用户")
    public PageResponseVO<String> getUsers(@Validated BasePageDTO dto) {
        IPage<String> iPage = logService.getUserNamePage(dto.getPage(), dto.getCount());
        return PageUtil.build(iPage);
    }
}
