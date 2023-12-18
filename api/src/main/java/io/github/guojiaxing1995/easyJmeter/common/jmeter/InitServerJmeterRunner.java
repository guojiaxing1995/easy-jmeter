package io.github.guojiaxing1995.easyJmeter.common.jmeter;

import io.github.guojiaxing1995.easyJmeter.common.util.ZipUtil;
import org.apache.commons.compress.archivers.ArchiveException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class InitServerJmeterRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws IOException, ArchiveException {
        ClassPathResource resource = new ClassPathResource("apache-jmeter.zip");
        InputStream inputStream = resource.getInputStream();
        String targetDirectory = System.getProperty("user.dir");
        ZipUtil.unzipFile(inputStream, targetDirectory);
    }
}
