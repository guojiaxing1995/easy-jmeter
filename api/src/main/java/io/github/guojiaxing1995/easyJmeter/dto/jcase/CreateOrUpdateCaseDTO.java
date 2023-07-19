package io.github.guojiaxing1995.easyJmeter.dto.jcase;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class CreateOrUpdateCaseDTO {

    @NotEmpty(message = "{case.name.not-empty}")
    @Length(max = 50, message = "{case.name.length}")
    private String name;

    @NotNull(message = "{case.project.not-empty}")
    private Integer project;

    private String jmx;

    private String csv;

    private String jar;

    @NotEmpty(message = "{case.description.not-empty}")
    @Length(max = 500, message = "{case.description.length}")
    private String description;

}
