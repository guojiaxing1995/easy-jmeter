package io.github.guojiaxing1995.easyJmeter.vo;

import io.github.guojiaxing1995.easyJmeter.model.JFileDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JFileVO {

    private Integer id;

    private String name;

    private String type;

    private String path;

    private String url;

    private String size;

    private Boolean cut;

    public JFileVO(JFileDO fileDO, String size) {
        BeanUtils.copyProperties(fileDO, this);
        this.size = size;
    }

}
