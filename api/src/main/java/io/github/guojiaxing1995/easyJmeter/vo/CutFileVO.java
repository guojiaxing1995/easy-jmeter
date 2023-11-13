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
public class CutFileVO {

    private Integer id;

    private String name;

    private String url;

    private Integer originId;

    private String taskId;

    private String originName;

    public CutFileVO(JFileDO fileDO, String originName) {
        BeanUtils.copyProperties(fileDO, this);
        this.originName = originName;
    }
}
