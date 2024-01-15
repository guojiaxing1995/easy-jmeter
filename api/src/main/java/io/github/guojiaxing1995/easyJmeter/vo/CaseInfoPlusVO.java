package io.github.guojiaxing1995.easyJmeter.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaseInfoPlusVO {

    private Integer id;

    private String name;

    private Integer creator;

    private JmeterStatusEnum status;

    private Integer project;

    private String jmx;

    private String csv;

    private String jar;

    private String description;

    private List<JFileVO> jmxFileList;

    private List<JFileVO> csvFileList;

    private List<JFileVO> jarFileList;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Shanghai")
    private Date createTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Shanghai")
    private Date updateTime;

    @JsonIgnore
    private Date deleteTime;

    public CaseInfoPlusVO(CaseDO caseDO, List<JFileVO> jmxFileList, List<JFileVO> csvFileList, List<JFileVO> jarFileList) {
        BeanUtils.copyProperties(caseDO, this);
        this.setCsvFileList(csvFileList);
        this.setJmxFileList(jmxFileList);
        this.setJarFileList(jarFileList);
    }
}
