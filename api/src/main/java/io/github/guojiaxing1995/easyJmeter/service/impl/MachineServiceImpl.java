package io.github.guojiaxing1995.easyJmeter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.MachineOnlineEnum;
import io.github.guojiaxing1995.easyJmeter.common.mybatis.Page;
import io.github.guojiaxing1995.easyJmeter.dto.machine.CreateOrUpdateMachineDTO;
import io.github.guojiaxing1995.easyJmeter.dto.machine.HeartBeatMachineDTO;
import io.github.guojiaxing1995.easyJmeter.mapper.MachineMapper;
import io.github.guojiaxing1995.easyJmeter.model.MachineDO;
import io.github.guojiaxing1995.easyJmeter.service.MachineService;
import io.github.talelin.autoconfigure.exception.ParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MachineServiceImpl implements MachineService {

    @Autowired
    private MachineMapper machineMapper;

    @Override
    public IPage<MachineDO> getMachineByName(Integer current, String name) {
        Page page = new Page(current, 10);
        IPage<MachineDO> machines = machineMapper.selectByName(page, name);
        return machines;
    }

    @Override
    public boolean createMachine(CreateOrUpdateMachineDTO validator) {
        if (machineMapper.selectByAddress(validator.getAddress()) != null){
            throw new ParameterException(12002);
        }
        MachineDO machineDO = new MachineDO();
        machineDO.setName(validator.getName());
        machineDO.setAddress(validator.getAddress());
        return machineMapper.insert(machineDO) > 0;
    }

    @Override
    public boolean updateMachine(MachineDO machine, CreateOrUpdateMachineDTO validator) {
        if (!validator.getAddress().equals(machine.getAddress())) {
            if (machineMapper.selectByAddress(validator.getAddress()) != null){
                throw new ParameterException(12002);
            }
        }
        machine.setName(validator.getName());
        machine.setAddress(validator.getAddress());
        return machineMapper.updateById(machine) > 0;
    }

    @Override
    public MachineDO getById(Integer id) {
        return machineMapper.selectById(id);
    }

    @Override
    public boolean deleteMachine(Integer id) {
        return machineMapper.deleteById(id) > 0;
    }

    @Override
    public void setMachineStatus(HeartBeatMachineDTO heartBeatMachineDTO, MachineOnlineEnum onlineEnum) {
        MachineDO machine;
        if (onlineEnum == MachineOnlineEnum.OFFLINE) {
            machine = machineMapper.selectByClientId(heartBeatMachineDTO.getClientId());
            machine.setOnline(MachineOnlineEnum.OFFLINE);
            machine.setJmeterStatus(JmeterStatusEnum.IDLE);
            machine.setClientId("");
            machineMapper.updateById(machine);
        } else {
            machine = machineMapper.selectByAddress(heartBeatMachineDTO.getAddress());
            if (machine != null) {
                machine.setPath(heartBeatMachineDTO.getPath());
                machine.setVersion(heartBeatMachineDTO.getVersion());
                machine.setOnline(heartBeatMachineDTO.getOnline());
                machine.setClientId(heartBeatMachineDTO.getClientId());
                machineMapper.updateById(machine);
            } else {
                log.info("压力机地址未在web端维护：" + heartBeatMachineDTO.getAddress());
            }
        }

    }

    @Override
    public MachineDO getByClientId(String clientId) {
        return machineMapper.selectByClientId(clientId);
    }
}
