package io.github.guojiaxing1995.easyJmeter.dto.machine;

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

    private Boolean isOnline;
}
