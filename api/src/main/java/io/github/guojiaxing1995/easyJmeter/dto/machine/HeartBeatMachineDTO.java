package io.github.guojiaxing1995.easyJmeter.dto.machine;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.MachineOnlineEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HeartBeatMachineDTO {

    private String clientId;

    private String address;

    private String path;

    private String version;

    private MachineOnlineEnum online;

    private JmeterStatusEnum jmeterStatus;

    public HeartBeatMachineDTO(String clientId) {
        this.clientId = clientId;
    }
}
