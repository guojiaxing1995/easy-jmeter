package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.model.JFileDO;
import org.springframework.web.multipart.MultipartFile;

public interface JFileService {

    JFileDO createFile(MultipartFile file);
}
