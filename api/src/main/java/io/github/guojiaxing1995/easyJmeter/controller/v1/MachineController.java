package io.github.guojiaxing1995.easyJmeter.controller.v1;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.common.util.PageUtil;
import io.github.guojiaxing1995.easyJmeter.dto.machine.CreateOrUpdateMachineDTO;
import io.github.guojiaxing1995.easyJmeter.model.MachineDO;
import io.github.guojiaxing1995.easyJmeter.service.MachineService;
import io.github.guojiaxing1995.easyJmeter.vo.CreatedVO;
import io.github.guojiaxing1995.easyJmeter.vo.DeletedVO;
import io.github.guojiaxing1995.easyJmeter.vo.PageResponseVO;
import io.github.guojiaxing1995.easyJmeter.vo.UpdatedVO;
import io.github.talelin.autoconfigure.exception.NotFoundException;
import io.github.talelin.core.annotation.GroupRequired;
import io.github.talelin.core.annotation.LoginRequired;
import io.github.talelin.core.annotation.PermissionMeta;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.ArrayList;

@RestController
@RequestMapping("/v1/machine")
@Api(tags = "压力机管理")
@Validated
public class MachineController {

    @Autowired
    private MachineService machineService;

    @GetMapping("")
    @ApiOperation(value = "压力机查询", notes = "根据名称查询压力机")
    @LoginRequired
    @GroupRequired
    @PermissionMeta(value = "压力机管理", module = "压力机")
    public PageResponseVO<MachineDO> searchMachine(@RequestParam(value = "name", required = false, defaultValue = "") String name,
                                                       @RequestParam(value = "page", required = false, defaultValue = "0") Integer page){
        IPage<MachineDO> machines = machineService.getMachineByName(page, name);
        return PageUtil.build(machines);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "压力机查询-id", notes = "获取指定id的压力机")
    @LoginRequired
    public MachineDO getMachine(@PathVariable("id") @Positive(message = "{id.positive}") Integer id){
        MachineDO machine = machineService.getById(id);
        if (machine == null){
            throw new NotFoundException(12001);
        }
        return machine;
    }

    @GetMapping("/all")
    @ApiOperation(value = "获取所有压力机", notes = "包含在线、离线")
    @LoginRequired
    public ArrayList<MachineDO> getAll(){
        return machineService.getAll();
    }

    @PostMapping("")
    @ApiOperation(value = "压力机创建", notes = "输入名称、地址创建项目")
    @LoginRequired
    public CreatedVO creatMachine(@RequestBody @Validated CreateOrUpdateMachineDTO validator){
        machineService.createMachine(validator);
        return new CreatedVO(1);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "压力机更新", notes = "输入名称、地址更新项目")
    @LoginRequired
    public UpdatedVO updateMachine(@PathVariable("id") @Positive(message = "{id.positive}") Integer id, @RequestBody @Validated CreateOrUpdateMachineDTO validator){
        MachineDO machine = machineService.getById(id);
        if (machine == null){
            throw new NotFoundException(12001);
        }
        machineService.updateMachine(machine, validator);
        return new UpdatedVO(2);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "压力机删除", notes = "删除指定压力机")
    @LoginRequired
    public DeletedVO deleteMachine(@PathVariable("id") @Positive(message = "{id.positive}") Integer id){
        MachineDO machine = machineService.getById(id);
        if (machine == null){
            throw new NotFoundException(12001);
        }
        machineService.deleteMachine(id);
        return new DeletedVO(3);
    }
}
