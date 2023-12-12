package io.github.guojiaxing1995.easyJmeter.common.util;


import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class CSVUtil {

    private final String csvPath;

    private final Integer splitNum;

    private final Integer fid;

    public CSVUtil(String csvPath, Integer splitNum, Integer fid){
        this.csvPath = csvPath;
        this.splitNum = splitNum;
        this.fid = fid;
    }

    public Map<Integer, List<String>> splitCSVFile(){
        List<String> files = new ArrayList<>();
        // 读取CSV文件
        List<String[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.csvPath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                records.add(line.split(","));
            }
        } catch (Exception e) {
            log.error("读取CSV文件失败", e);
        }
        // 分割CSV文件
        List<List<String[]>> parts = new ArrayList<>();
        if (records.size() > this.splitNum) {
            int chunkSize = (int)Math.ceil((double)records.size() / this.splitNum);
            for (int i = 0; i < records.size(); i += chunkSize) {
                parts.add(new ArrayList<>(records.subList(i, Math.min(i + chunkSize, records.size()))));
            }
        } else {
            for (int i = 0; i < this.splitNum; i++) {
                parts.add(records);
            }
        }
        // 写入csv文件
        File file = new File(this.csvPath);
        String parentDir = file.getParent();
        for (int i = 0; i < parts.size(); i++) {
            String filePath = String.valueOf(Paths.get(parentDir, i + "_" + file.getName()));
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
                for (String[] record : parts.get(i)) {
                    StringBuilder sb = new StringBuilder(Arrays.toString(record).length()-2);
                    for(int j=1;j<Arrays.toString(record).length()-1;j++) {
                        sb.append(Arrays.toString(record).charAt(j));
                    }
                    bw.write(sb.toString().replaceAll(",\\s+", ","));
                    bw.newLine();
                }
            } catch (Exception e) {
                log.error("写入CSV文件失败", e);
            }
            files.add(filePath);
        }
        log.info(files.toString());
        Map<Integer, List<String>> map = new HashMap<>();
        map.put(this.fid, files);
        return map;
    }
}
