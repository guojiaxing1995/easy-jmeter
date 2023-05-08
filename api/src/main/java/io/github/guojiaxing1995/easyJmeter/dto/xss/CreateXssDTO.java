package io.github.guojiaxing1995.easyJmeter.dto.xss;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class CreateXssDTO {

    @NotEmpty(message = "{xss.key.not-empty}")
    @Length(max = 500, message = "{xss.key.length}")
    private String key;

    @NotEmpty(message = "{xss.value.not-empty}")
    private String value;
}
