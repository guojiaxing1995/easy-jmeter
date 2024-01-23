package io.github.guojiaxing1995.easyJmeter.vo;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.DebugTypeEnum;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.xstream.TestResults;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaseDebugVO {
    private Integer caseId;
    private Long debugId;
    private DebugTypeEnum type;
    private TestResults result;
    private String log;
}
