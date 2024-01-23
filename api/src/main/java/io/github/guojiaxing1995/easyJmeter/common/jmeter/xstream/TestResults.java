package io.github.guojiaxing1995.easyJmeter.common.jmeter.xstream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

@Data
@XStreamAlias("testResults")
public class TestResults {

    @XStreamImplicit
    @XStreamAlias("httpSample")
    private List<HttpSample> httpSamples;
}
