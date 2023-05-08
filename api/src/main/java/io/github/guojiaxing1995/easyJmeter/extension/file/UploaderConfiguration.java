package io.github.guojiaxing1995.easyJmeter.extension.file;

import io.github.guojiaxing1995.easyJmeter.module.file.Uploader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 文件上传配置类
 *
 * @author Juzi@TaleLin
 * @author colorful@TaleLin
 */
@Configuration(proxyBeanMethods = false)
public class UploaderConfiguration {
    /**
     * @return 本地文件上传实现类
     */
    @Bean
    @Order
    @ConditionalOnMissingBean
    public Uploader uploader(){
        return new LocalUploader();
    }
}
