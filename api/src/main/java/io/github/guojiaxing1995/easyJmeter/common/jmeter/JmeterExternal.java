package io.github.guojiaxing1995.easyJmeter.common.jmeter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.guojiaxing1995.easyJmeter.common.util.ThreadUtil;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskProgressMachineDTO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.socket.client.Socket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.timers.ConstantThroughputTimer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
@Configuration
@ConfigurationProperties(prefix = "jmeter")
public class JmeterExternal {

    private String version;

    private String path;

    private String address;

    private Boolean isOnline;

    private final Socket socket;

    public JmeterExternal(Socket socket) {
        this.socket = socket;
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
        this.isOnline = !this.version.isEmpty();
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
        // 依赖jar
        this.setProperties("plugin_dependency_paths=../tmp/dependencies/;");
        // BeanShell Server properties
        this.setProperties("beanshell.server.port=38927");
        this.setProperties("beanshell.server.file=../extras/startup.bsh");
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

            // 添加常数吞吐量定时器
            int qpsLimit = taskDO.getQpsLimit() ==0 ?  999999999 : taskDO.getQpsLimit();
            ConstantThroughputTimer constantThroughputTimer = new ConstantThroughputTimer();
            constantThroughputTimer.setName("Constant Throughput Timer");
            constantThroughputTimer.setEnabled(true);
            constantThroughputTimer.setCalcMode(1);
            constantThroughputTimer.setProperty(new StringProperty(TestElement.GUI_CLASS, "TestBeanGUI"));
            constantThroughputTimer.setProperty(new StringProperty(TestElement.TEST_CLASS, "ConstantThroughputTimer"));
            String throughput = "${__jexl3(${__P(throughput, "+ qpsLimit +")}*60,)}";
            constantThroughputTimer.setProperty("throughput", throughput);

            testPlanTree.add(testPlanTree.getArray()[0], constantThroughputTimer);


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
                    // 向主节点发送进度
                    Integer machineProcess = this.getMachineProcess(line, taskDO);
                    if (machineProcess !=0 && !Thread.currentThread().isInterrupted()) {
                        TaskProgressMachineDTO taskProgressMachineDTO = new TaskProgressMachineDTO(taskDO.getTaskId(), this.getAddress(), machineProcess);
                        String taskProgressMachine = new ObjectMapper().writeValueAsString(taskProgressMachineDTO);
                        socket.emit("machineTaskProgress", taskProgressMachine);
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

    public Integer getMachineProcess(String line, TaskDO taskDO) {
        String pattern = "\\d{2}:(\\d{2}):(\\d{2})"; // 正则表达式用于匹配时分秒格式
        Pattern r = Pattern.compile(pattern);
        if (!line.contains("summary =")) {
            return 0;
        }
        Matcher m = r.matcher(line);
        if (m.find()) {
            String time = m.group(0); // 完整匹配结果，即 "00:00:05"
            String[] parts = time.split(":"); // 使用冒号分割时分秒
            int hour = Integer.parseInt(parts[0]); // 小时
            int minute = Integer.parseInt(parts[1]); // 分钟
            int second = Integer.parseInt(parts[2]); // 秒

            int totalSeconds = hour * 3600 + minute * 60 + second; // 转换为总秒数
            return Math.round(((float) totalSeconds /taskDO.getDuration())*100);

        } else {
            return 0;
        }
    }

    public String createBshFile() {
        String setPropStr = "import org.apache.jmeter.util.JMeterUtils;\n" +
                "setProp(p,v){\n" +
                "    JMeterUtils.getJMeterProperties().setProperty(p, v);\n" +
                "}\n" +
                "setProp(args[0], args[1]);";
        String filePath = Paths.get(this.path, "/tmp/setProp.bsh").toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(setPropStr);
        } catch (IOException e) {
            log.error("createBshFile", e);
            throw new RuntimeException(e);
        }
        return filePath;
    }

    public void modifyQPSLimit(TaskDO taskDO) {
        String clientJarPath = Paths.get(this.path, "/lib/bshclient.jar").toString();
        Integer qpsLimit = taskDO.getQpsLimit();
        if (taskDO.getQpsLimit() == 0) {
            qpsLimit = 999999999;
        }
        String bshPath = this.createBshFile();
        ProcessBuilder processBuilder =
                new ProcessBuilder("java", "-jar", clientJarPath, "localhost", "38927", bshPath, "throughput", String.valueOf(qpsLimit));
        processBuilder.environment().putAll(System.getenv());
        log.info("调节吞吐量:"+"java"+ "-jar"+ clientJarPath+ "localhost"+ 38927+ bshPath+ "throughput"+ qpsLimit.toString());
        try {
            Process process = processBuilder.start();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            }
            int exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            log.error("调节吞吐量失败", e);
        }
    }
}
