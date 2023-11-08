package io.github.guojiaxing1995.easyJmeter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pedro@TaleLin
 */
@EnableScheduling
@RestController
@EnableCaching
@MapperScan(basePackages = {"io.github.guojiaxing1995.easyJmeter.mapper"})
@SpringBootApplication(scanBasePackages = {"io.github.guojiaxing1995.easyJmeter"})
public class EasyJmeterApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyJmeterApplication.class, args);
    }

    @RequestMapping("/")
    public String index() {
        return "<style type=\"text/css\">*{ padding: 0; margin: 0; } div{ padding: 4px 48px;} a{color:#2E5CD5;cursor:" +
                "pointer;text-decoration: none} a:hover{text-decoration:underline; } body{ background: #fff; font-family:" +
                "\"Century Gothic\",\"Microsoft yahei\"; color: #333;font-size:18px;} h1{ font-size: 100px; font-weight: normal;" +
                "margin-bottom: 12px; } p{ line-height: 1.6em; font-size: 42px }</style><div style=\"padding: 24px 48px;\"><p>" +
                "<span style=\"font-size:30px\">遇事不决 可问春风</span></p></div> ";
    }
}
