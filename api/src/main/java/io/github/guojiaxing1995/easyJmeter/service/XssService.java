package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.dto.xss.CreateXssDTO;
import io.github.guojiaxing1995.easyJmeter.model.XssDO;

public interface XssService {

    boolean createXss(CreateXssDTO validator);

    XssDO getByKey(String k);
}
