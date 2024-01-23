package io.github.guojiaxing1995.easyJmeter.common.jmeter.xstream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

@Data
@XStreamAlias("assertionResult")
public class AssertionResult {

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private Boolean failure;

    @XStreamAsAttribute
    private Boolean error;

    @XStreamAsAttribute
    private String failureMessage;
}
