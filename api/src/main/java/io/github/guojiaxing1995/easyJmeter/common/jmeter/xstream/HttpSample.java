package io.github.guojiaxing1995.easyJmeter.common.jmeter.xstream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

@Data
@XStreamAlias("httpSample")
public class HttpSample {
    @XStreamAsAttribute
    private String t;

    @XStreamAsAttribute
    private String it;

    @XStreamAsAttribute
    private String lt;

    @XStreamAsAttribute
    private String ct;

    @XStreamAsAttribute
    private Long ts;

    @XStreamAsAttribute
    private Boolean s;

    @XStreamAsAttribute
    private String lb;

    @XStreamAsAttribute
    private String rc;

    @XStreamAsAttribute
    private String rm;

    @XStreamAsAttribute
    private String tn;

    @XStreamAsAttribute
    private String dt;

    @XStreamAsAttribute
    private String by;

    @XStreamAsAttribute
    private String sby;

    @XStreamAsAttribute
    private String ng;

    @XStreamAsAttribute
    private String na;

    private String responseHeader;
    private String requestHeader;
    private String responseData;

    private String cookies;
    private String method;
    private String queryString;
    @XStreamAlias("java.net.URL")
    private String javaNetURL;

    @XStreamImplicit
    @XStreamAlias("assertionResult")
    private List<AssertionResult> assertionResults;

    private String redirectLocation;

    @XStreamImplicit
    @XStreamAlias("httpSample")
    private List<HttpSample> httpSamples;

}

