package io.github.guojiaxing1995.easyJmeter.dto.project;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class CreateOrUpdateProjectDTO {

    @NotEmpty(message = "{project.name.not-empty}")
    @Length(max = 50, message = "{project.name.length}")
    private String name;

    @Length(max = 1000, message = "{project.description.length}")
    private String description;
}
