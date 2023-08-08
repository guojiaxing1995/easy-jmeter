package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.vo.JFileVO;
import org.springframework.web.multipart.MultipartFile;

public interface JFileService {

    JFileVO createFile(MultipartFile file);

    Boolean setFileCut(Integer id, Boolean cut);
}
