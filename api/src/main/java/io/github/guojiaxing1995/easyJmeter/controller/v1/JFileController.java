package io.github.guojiaxing1995.easyJmeter.controller.v1;

import io.github.guojiaxing1995.easyJmeter.service.JFileService;
import io.github.guojiaxing1995.easyJmeter.vo.JFileVO;
import io.github.guojiaxing1995.easyJmeter.vo.UpdatedVO;
import io.github.talelin.core.annotation.LoginRequired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.util.Objects;

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
    public JFileVO uploadFile(@RequestPart("file") MultipartFile file) {
        return fileService.createFile(file);
    }

    @PutMapping("/cut/{id}")
    @ApiOperation(value = "文件切分", notes = "传入文件id和切分状态")
    @LoginRequired
    public UpdatedVO cutFile(@PathVariable("id") @Positive(message = "{id.positive}") Integer id, Boolean cut) {
        fileService.setFileCut(id, cut);
        return new UpdatedVO(2);
    }

    @GetMapping("/jmeterLog/{taskId}")
    @ApiOperation(value = "下载jmeter日志", notes = "传入taskId,下载对应的jmeter日志")
    public void downloadJmeterLogZip(HttpServletResponse response, @PathVariable String taskId) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", taskId + ".zip");
        response.setContentType(Objects.requireNonNull(headers.getContentType()).toString());
        response.setHeader("Content-Disposition", headers.getContentDisposition().toString());
        fileService.downLoadJmeterLogZip(taskId, response.getOutputStream());
    }
}
