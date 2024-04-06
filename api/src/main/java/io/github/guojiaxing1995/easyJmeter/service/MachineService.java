package io.github.guojiaxing1995.easyJmeter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.dto.machine.CreateOrUpdateMachineDTO;
import io.github.guojiaxing1995.easyJmeter.dto.machine.HeartBeatMachineDTO;
import io.github.guojiaxing1995.easyJmeter.model.MachineDO;

import java.util.ArrayList;

public interface MachineService {

    IPage<MachineDO> getMachineByName(Integer current, String name);

    boolean createMachine(CreateOrUpdateMachineDTO validator);

    boolean updateMachine(MachineDO machineDO, CreateOrUpdateMachineDTO validator);

    MachineDO getById(Integer id);

    boolean deleteMachine(Integer id);

    void setMachineStatus(HeartBeatMachineDTO heartBeatMachineDTO, Boolean online);

    MachineDO getByClientId(String clientId);

    ArrayList<MachineDO> getAll();

    boolean updateMachineStatus(MachineDO machineDO, JmeterStatusEnum status);

    MachineDO getByAddress(String address);

    void setMachineOffline();
}
