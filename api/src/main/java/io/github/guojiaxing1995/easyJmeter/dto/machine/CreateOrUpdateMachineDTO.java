package io.github.guojiaxing1995.easyJmeter.dto.machine;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class CreateOrUpdateMachineDTO {

    @NotEmpty(message = "{machine.name.not-empty}")
    @Length(max = 50, message = "{machine.name.length}")
    private String name;

    @NotEmpty(message = "{machine.address.not-empty}")
    @Length(max = 100, message = "{machine.address.length}")
    private String address;
}
