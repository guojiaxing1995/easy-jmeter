package io.github.guojiaxing1995.easyJmeter.common.jmeter;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.MachineOnlineEnum;
import io.github.guojiaxing1995.easyJmeter.common.util.ThreadUtil;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Slf4j
public class JmeterExternal {

    private String version;

    private String path;

    private String address;

    private MachineOnlineEnum online;

    public JmeterExternal() {
        this.path = System.getenv("JMETER_HOME");
        this.version();
        this.ipAddress();
        this.online();
    }

    public void version() {
        Path jmeterPath = Paths.get(this.path, "/bin/jmeter");
        ProcessBuilder processBuilder = new ProcessBuilder( jmeterPath.toString(), "--version");
        processBuilder.environment().putAll(System.getenv());
        StringBuilder outputString = new StringBuilder();
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputString.append(line).append("\n");
            }
            int exitCode = process.waitFor();
        } catch (InterruptedException | IOException e) {
            log.error("获取jmeter版本失败", e);
        } finally {
            process.destroy();
        }
        String version_str = outputString.toString().replaceAll("[_|\\\\/\\s\\r\\n]", "");
        String prefix = Pattern.quote("<");
        String suffix = "Copyright";
        String regex = prefix + "(.*?)" + suffix;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(version_str);
        if (matcher.find()) {
            version = matcher.group(1).trim();
        }
        this.version = version;
    }

    public void ipAddress() {
        try{
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress addr = null;
            while (allNetInterfaces.hasMoreElements())
            {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                //System.out.println(netInterface.getName());
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements())
                {
                    InetAddress ipTmp = (InetAddress) addresses.nextElement();
                    if(ipTmp != null && ipTmp instanceof Inet4Address
                            && ipTmp.isSiteLocalAddress()
                            && !ipTmp.isLoopbackAddress()
                            && ipTmp.getHostAddress().indexOf(":")==-1){
                        addr = ipTmp;
                    }
                }
            }
            if(addr != null) {
                this.address = addr.getHostAddress();
            } else {
                log.error("获取本机ip异常");
            }
        }catch(SocketException e){
            log.error("获取本机ip异常", e);
        }
    }

    public void online() {
        if (!this.version.isEmpty()){
            this.online = MachineOnlineEnum.ONLINE;
        } else {
            this.online = MachineOnlineEnum.OFFLINE;
        }
    }

    public Boolean isPropertiesExist(String property) {
        File file = new File(Paths.get(this.path, "/bin/user.properties").toString());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(property)) {
                    return true;
                }
            }
        } catch (IOException e) {
            log.error("读取user.properties文件失败", e);
            throw new RuntimeException(e);
        }

        return false;
    }

    public void setProperties(String property) {
        if (!this.isPropertiesExist(property)) {
            File file = new File(Paths.get(this.path, "/bin/user.properties").toString());
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
                writer.println(property);
            } catch (IOException e) {
                log.error("写入user.properties失败，属性：" + property, e);
                throw new RuntimeException(e);
            }
        }
    }

    public void addProperties() {
        this.setProperties("plugin_dependency_paths=../tmp/dependencies/;");
    }

    public void initJMeterUtils() {
        JMeterUtils.setJMeterHome(this.path);
        JMeterUtils.loadJMeterProperties(Paths.get(this.path, "/bin/jmeter.properties").toString());
    }

    public void editJmxConfig(TaskDO taskDO) throws IOException {
        File directory = new File(this.path + "/tmp/");
        File[] files = directory.listFiles();
        File jmxfile = null;
        List<File> csvFiles = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".csv")) {
                    csvFiles.add(file);
                }
                if (file.isFile() && file.getName().toLowerCase().endsWith(".jmx")) {
                    jmxfile = file;
                }
            }
        }
        if (jmxfile != null) {
            HashTree testPlanTree = SaveService.loadTree(jmxfile);
            // 修改jmx中csv配置
            SearchByClass<CSVDataSet> csvDataSetSearch = new SearchByClass<>(CSVDataSet.class);
            testPlanTree.traverse(csvDataSetSearch);
            for (File csvFile : csvFiles) {
                for (CSVDataSet csvDataSet : csvDataSetSearch.getSearchResults()) {
                    String csvOldPathName = new File(csvDataSet.getProperty("filename").toString()).getName();
                    if (csvOldPathName.equals(csvFile.getName())) {
                        csvDataSet.setProperty("filename", csvFile.getAbsolutePath());
                    }

                }
            }
            // 修改jmx中压测参数
            SearchByClass<SetupThreadGroup> setupThreadGroupSearch = new SearchByClass<>(SetupThreadGroup.class);
            testPlanTree.traverse(setupThreadGroupSearch);
            for (SetupThreadGroup setupThreadGroup : setupThreadGroupSearch.getSearchResults()) {
                setupThreadGroup.setNumThreads(taskDO.getNumThreads()/taskDO.getMachineNum());
                setupThreadGroup.setDuration(taskDO.getDuration());
                setupThreadGroup.setRampUp(taskDO.getRampTime());
                setupThreadGroup.getSamplerController().setProperty("LoopController.loops", -1);
            }

            try (FileOutputStream outputStream = new FileOutputStream(new File(this.path + "/tmp/" +taskDO.getTaskId() +".jmx"))) {
                SaveService.saveTree(testPlanTree, outputStream);
            }

        }
    }

    public void runJmeter(TaskDO taskDO) {
        String jmeterPath = Paths.get(this.path, "/bin/jmeter").toString();
        String jmxPath = Paths.get(this.path, "/tmp/" + taskDO.getTaskId() + ".jmx").toString();
        String logPath = Paths.get(this.path, "/tmp/jmeter.log").toString();
        String jtlPath = Paths.get(this.path, "/tmp/result.jtl").toString();
        String reportPath = Paths.get(this.path, "/tmp/report").toString();
        ProcessBuilder processBuilder =
                new ProcessBuilder( jmeterPath,"-n","-t",jmxPath,"-j",logPath,"-L","all="+taskDO.getLogLevel().getDesc(),"-l",jtlPath,"-e","-o",reportPath);
        processBuilder.environment().putAll(System.getenv());
        StringBuilder outputString = new StringBuilder();
        try {
            Process process = processBuilder.start();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputString.append(line).append("\n");
                    if (Thread.currentThread().isInterrupted()) {
                        log.info("压测中的jmeter线程被中断");
                        process.destroy();
                    }
                    // 判断是否已经结束但由于开放了beanshell端口导致服务没有停止
                    File report = new File(reportPath);
                    if (report.exists() && ThreadUtil.isProcessContainingName(taskDO.getTaskId()+".jmx")) {
                        ThreadUtil.killProcessContainingName(taskDO.getTaskId()+".jmx");
                    }
                    log.info(line);
                }
            }
            int exitCode = process.waitFor();

        } catch (IOException | InterruptedException e) {
            log.error("执行jmeter失败", e);
        }
        log.info(outputString.toString());
    }
}
