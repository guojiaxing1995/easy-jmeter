package io.github.guojiaxing1995.easyJmeter.service.impl;

import io.github.guojiaxing1995.easyJmeter.dto.xss.CreateXssDTO;
import io.github.guojiaxing1995.easyJmeter.mapper.XssMapper;
import io.github.guojiaxing1995.easyJmeter.model.XssDO;
import io.github.guojiaxing1995.easyJmeter.service.XssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class XssServiceImpl implements XssService {

    @Autowired
    private XssMapper xssMapper;

    @Override
    public boolean createXss(CreateXssDTO validator) {
        XssDO xss = new XssDO();
        xss.setXssKey(validator.getKey());
        xss.setXssValue(validator.getValue());
        return xssMapper.insert(xss) > 0;
    }

    @Override
    public XssDO getByKey(String k) {
        return xssMapper.selectByKey(k);
    }
}
