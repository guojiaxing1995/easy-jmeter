package io.github.guojiaxing1995.easyJmeter.controller.v1;

import io.github.guojiaxing1995.easyJmeter.model.JFileDO;
import io.github.guojiaxing1995.easyJmeter.service.JFileService;
import io.github.talelin.core.annotation.LoginRequired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/file")
@Api(tags = "文件操作")
@Validated
public class JFileController {

    @Autowired
    private JFileService fileService;

    @PostMapping("/uploadFile")
    @ApiOperation(value = "文件上传", notes = "上传文件至文件服务器")
    @LoginRequired
    public JFileDO uploadFile(@RequestPart("file") MultipartFile file) {
        return fileService.createFile(file);
    }
}
