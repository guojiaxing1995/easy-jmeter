package io.github.guojiaxing1995.easyJmeter.common.jmeter.xstream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JmeterLogDeal {
    public static String[] cutContentFromFile(String inputFile, String startPattern, String endPattern) throws IOException {
        // 读取输入文件
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
            String content = contentBuilder.toString();

            // 定义并应用正则表达式
            Pattern pattern = Pattern.compile(startPattern + "(.*?)" + endPattern, Pattern.DOTALL | Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                String[] result = matcher.group(1).split("\n");
                String[] logs= new String[result.length-1];
                System.arraycopy(result, 0, logs, 0, result.length - 1);
                return logs;
            } else {
                return new String[]{};
            }
        }
    }
}
