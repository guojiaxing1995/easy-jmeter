package io.github.guojiaxing1995.easyJmeter.dto.machine;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.MachineOnlineEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeartBeatMachineDTO implements Serializable {

    private static final long serialVersionUID = 877827989345852198L;
    private String clientId;

    private String address;

    private String path;

    private String version;

    private MachineOnlineEnum online;

    public HeartBeatMachineDTO(String clientId) {
        this.clientId = clientId;
    }
}
