package io.github.guojiaxing1995.easyJmeter.dto.file;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateFileDTO {

    private String name;

    private String type;

    private String path;

    private String url;

    private Long size;

}
