package io.github.guojiaxing1995.easyJmeter.common.jmeter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.guojiaxing1995.easyJmeter.common.configuration.InfluxDBProperties;
import io.github.guojiaxing1995.easyJmeter.common.util.ThreadUtil;
import io.github.guojiaxing1995.easyJmeter.common.util.ZipUtil;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskProgressMachineDTO;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import io.github.guojiaxing1995.easyJmeter.model.JFileDO;
import io.github.guojiaxing1995.easyJmeter.model.ReportDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.repository.ReportRepository;
import io.github.guojiaxing1995.easyJmeter.service.JFileService;
import io.github.guojiaxing1995.easyJmeter.vo.CutFileVO;
import io.github.guojiaxing1995.easyJmeter.vo.MachineCutFileVO;
import io.github.talelin.autoconfigure.exception.ParameterException;
import io.socket.client.Socket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.dashboard.GenerationException;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.timers.ConstantThroughputTimer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.BackendListener;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Locale.ENGLISH;
import static org.apache.jmeter.JMeter.JMETER_REPORT_OUTPUT_DIR_PROPERTY;

@Data
@Slf4j
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

    public JmeterExternal() {
        this.socket = null;
        this.path = System.getProperty("user.dir") + "/apache-jmeter";
        this.serverVersion();
        this.ipAddress();
        this.online();
    }

    public void serverVersion() {
        JMeterUtils.setJMeterHome(this.path);
        JMeterUtils.loadJMeterProperties(Paths.get(this.path, "/bin/jmeter.properties").toString());
        this.version = JMeterUtils.getJMeterVersion();
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

    public void initServerJmeterUtils(TaskDO taskDO) {
        JMeterUtils.setJMeterHome(this.path);
        JMeterUtils.loadJMeterProperties(Paths.get(this.path, "/bin/jmeter.properties").toString());
        if (taskDO!=null){
            Integer granularity = taskDO.getGranularity();
            if (granularity == 0) {
                Integer duration = taskDO.getDuration();
                if (duration/60 <= 15) {
                    granularity = 3;
                } else if (duration/60 > 15 && duration/60 <= 30) {
                    granularity = 6;
                } else if (duration/60 > 30 && duration/60 <= 60) {
                    granularity = 10;
                } else {
                    granularity = 30;
                }
            }
            JMeterUtils.setProperty("jmeter.reportgenerator.overall_granularity", String.valueOf(granularity*1000));
        }
        JMeterUtils.setProperty("jmeter.save.saveservice.output_format", "xml");
        JMeterUtils.setProperty("jmeter.save.saveservice.response_data", "true");
        JMeterUtils.setProperty("jmeter.save.saveservice.response_data.on_error", "true");
        JMeterUtils.setProperty("jmeter.save.saveservice.samplerData", "true");
        JMeterUtils.setProperty("jmeter.save.saveservice.responseHeaders", "true");
        JMeterUtils.setProperty("jmeter.save.saveservice.requestHeaders", "true");
        JMeterUtils.setLocale(ENGLISH);
    }

    public void editJmxConfig(TaskDO taskDO, InfluxDBProperties influxDBProperties) throws IOException {
        File directory = new File(Paths.get(this.path, "/tmp/").toString());
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
            SearchByClass<ThreadGroup> threadGroupSearch = new SearchByClass<>(org.apache.jmeter.threads.ThreadGroup.class);
            testPlanTree.traverse(threadGroupSearch);
            for (ThreadGroup threadGroup : threadGroupSearch.getSearchResults()) {
                threadGroup.setNumThreads(taskDO.getNumThreads()/taskDO.getMachineNum());
                threadGroup.setDuration(taskDO.getDuration());
                threadGroup.setScheduler(true);
                threadGroup.setRampUp(taskDO.getRampTime());
                threadGroup.getSamplerController().setProperty("LoopController.loops", -1);
            }

            // 添加常数吞吐量定时器
            int qpsLimit = taskDO.getQpsLimit() ==0 ?  999999999 : Math.round((float) taskDO.getQpsLimit() /taskDO.getMachineNum());
            ConstantThroughputTimer constantThroughputTimer = new ConstantThroughputTimer();
            constantThroughputTimer.setName("Constant Throughput Timer");
            constantThroughputTimer.setEnabled(true);
            constantThroughputTimer.setCalcMode(1);
            constantThroughputTimer.setProperty(new StringProperty(TestElement.GUI_CLASS, "TestBeanGUI"));
            constantThroughputTimer.setProperty(new StringProperty(TestElement.TEST_CLASS, "ConstantThroughputTimer"));
            String throughput = "${__jexl3(${__P(throughput, "+ qpsLimit +")}*60,)}";
            constantThroughputTimer.setProperty("throughput", throughput);

            testPlanTree.add(testPlanTree.getArray()[0], constantThroughputTimer);

            //添加influxdb后置监听器
            if (taskDO.getRealtime()) {
                String database = influxDBProperties.getDatabase();
                String url = influxDBProperties.getUrl();
                String taskId = taskDO.getTaskId();
                Arguments arguments = new Arguments();
                arguments.setProperty(new StringProperty(TestElement.GUI_CLASS, "ArgumentsPanel"));
                arguments.setProperty(new StringProperty(TestElement.TEST_CLASS, "Arguments"));
                arguments.setProperty(new StringProperty(TestElement.NAME, "arguments"));
                arguments.setEnabled(true);
                arguments.addArgument("influxdbMetricsSender", "org.apache.jmeter.visualizers.backend.influxdb.HttpMetricsSender", "=");
                arguments.addArgument("influxdbUrl", url+"/write?db="+database, "=");
                arguments.addArgument("application", taskId, "=");
                arguments.addArgument("measurement", "jmeter", "=");
                arguments.addArgument("summaryOnly", "false", "=");
                arguments.addArgument("samplersRegex", ".*", "=");
                arguments.addArgument("percentiles", "90;95;99", "=");
                arguments.addArgument("testTitle", taskId, "=");
                arguments.addArgument("eventTags", this.address, "=");

                BackendListener backendListener = new BackendListener();
                backendListener.setProperty(new StringProperty(TestElement.GUI_CLASS, "BackendListenerGui"));
                backendListener.setProperty(new StringProperty(TestElement.TEST_CLASS, "BackendListener"));
                backendListener.setProperty(new StringProperty(TestElement.NAME, "influxdbBackendListener"));
                backendListener.setEnabled(true);
                backendListener.setClassname("org.apache.jmeter.visualizers.backend.influxdb.InfluxdbBackendListenerClient");
                backendListener.setArguments(arguments);

                testPlanTree.add(testPlanTree.getArray()[0], backendListener);
            }

            try (FileOutputStream outputStream = new FileOutputStream(new File(this.path + "/tmp/" +taskDO.getTaskId() +".jmx"))) {
                SaveService.saveTree(testPlanTree, outputStream);
            }
        }
    }

    public Map<String, String> editJmxDebugConfig(CaseDO caseDO, Long debugId, JFileService jFileService) throws IOException {
        // jmx文件路径
        String jmxPath = null;
        // 文件下载目录
        String dir = Paths.get(jFileService.getStoreDir(), caseDO.getId().toString(), debugId.toString()).toString();
        // jtl 文件路径
        String jtlPath = Paths.get(dir, "debug.jtl").toString();
        // 下载jmx文件
        String jmxStr = caseDO.getJmx();
        List<JFileDO> jmxFileDOList= Arrays.stream(jmxStr.isEmpty() ? new String[]{} : jmxStr.split(","))
                .map(Integer::parseInt).map(jFileService::searchById).collect(Collectors.toList());
        if (jmxFileDOList.isEmpty()) {
            throw new ParameterException();
        } else {
            jmxPath = jFileService.downloadFile(jmxFileDOList.get(0).getId(), dir);
        }
        // jar文件下载
        String jarStr = caseDO.getJar();
        List<JFileDO> jarFileDOList= Arrays.stream(jarStr.isEmpty() ? new String[]{} : jarStr.split(","))
                .map(Integer::parseInt).map(jFileService::searchById).collect(Collectors.toList());
        for (JFileDO jFileDO: jarFileDOList) {
            jFileService.downloadFile(jFileDO.getId(), dir);
        }
        // csv 文件下载
        String csvStr = caseDO.getCsv();
        List<JFileDO> csvFileDOList= Arrays.stream(csvStr.isEmpty() ? new String[]{} : csvStr.split(","))
                .map(Integer::parseInt).map(jFileService::searchById).collect(Collectors.toList());
        List<File> csvFiles = new ArrayList<>();
        for (JFileDO jFileDO: csvFileDOList) {
            File csvFile = new File(jFileService.downloadFile(jFileDO.getId(), dir));
            csvFiles.add(csvFile);
        }
        // 修改压测配置
        HashTree testPlanTree = SaveService.loadTree(new File(jmxPath));
        SearchByClass<SetupThreadGroup> setupThreadGroupSearch = new SearchByClass<>(SetupThreadGroup.class);
        testPlanTree.traverse(setupThreadGroupSearch);
        for (SetupThreadGroup setupThreadGroup : setupThreadGroupSearch.getSearchResults()) {
            setupThreadGroup.setNumThreads(1);
            setupThreadGroup.setScheduler(false);
            setupThreadGroup.setRampUp(1);
            setupThreadGroup.getSamplerController().setProperty("LoopController.loops", 1);
        }
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
        // 加入查看结果数
        Summariser summer = new Summariser("debug");
        ResultCollector resultCollector = new ResultCollector(summer);
        resultCollector.setEnabled(true);
        resultCollector.setProperty(new StringProperty(TestElement.GUI_CLASS, "ViewResultsFullVisualizer"));
        resultCollector.setProperty(new StringProperty(TestElement.TEST_CLASS, "ResultCollector"));
        resultCollector.setProperty(new StringProperty(TestElement.NAME, "debug"));
        resultCollector.setFilename(jtlPath);
        testPlanTree.add(testPlanTree.getArray()[0], resultCollector);
        // 写回jmx文件
        try (FileOutputStream outputStream = new FileOutputStream(new File(jmxPath))) {
            SaveService.saveTree(testPlanTree, outputStream);
        }
        
        Map<String, String> map = new HashMap<>();
        map.put("jmxPath", jmxPath);
        map.put("jtlPath", jtlPath);

        return map;
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
        Integer qpsLimit;
        if (taskDO.getQpsLimit() == 0) {
            qpsLimit = 999999999;
        } else {
            qpsLimit = Math.round((float) taskDO.getQpsLimit() /taskDO.getMachineNum());
        }
        String bshPath = this.createBshFile();
        ProcessBuilder processBuilder =
                new ProcessBuilder("java", "-jar", clientJarPath, "localhost", "38927", bshPath, "throughput", String.valueOf(qpsLimit));
        processBuilder.environment().putAll(System.getenv());
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

    public void clean(TaskDO taskDO) throws IOException {
        // 如果测试进程仍然存在，则杀掉进程
        if (ThreadUtil.isProcessContainingName(taskDO.getTaskId()+".jmx")) {
            ThreadUtil.killProcessContainingName(taskDO.getTaskId()+".jmx");
        }
        // 将tmp目录重命名为任务目录
        Path tmpPath = Paths.get(this.path, "tmp");
        Path taskPath = Paths.get(this.path, taskDO.getTaskId());
        Files.move(tmpPath, taskPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public void collect(TaskDO taskDO, JFileService jFileService) {
        // 上传jmeter日志
        File jmeterLogPath = new File(Paths.get(this.path, "tmp", "jmeter.log").toString());
        File newLogPath = new File(Paths.get(this.path, "tmp", this.address+"_jmeter.log").toString());
        boolean LogRename = jmeterLogPath.renameTo(newLogPath);
        if (LogRename) {
            JFileDO file = jFileService.createFile(newLogPath.getAbsolutePath());
            file.setTaskId(taskDO.getTaskId());
            jFileService.updateById(file);
        }
        // 上传jtl文件
        File jtlPath = new File(Paths.get(this.path, "tmp", "result.jtl").toString());
        File newJtlPath = new File(Paths.get(this.path, "tmp", this.address + taskDO.getTaskId() +"_result.jtl").toString());
        boolean jtlRename = jtlPath.renameTo(newJtlPath);
        if (jtlRename) {
            JFileDO file = jFileService.createFile(newJtlPath.getAbsolutePath());
            file.setTaskId(taskDO.getTaskId());
            jFileService.updateById(file);
        }
    }

    public void downloadConfigFile(TaskDO taskDO, JFileService jFileService, MachineCutFileVO machineCutFileVO) {
        String tmpDir = Paths.get(this.path, "tmp").toString();
        String dependencyDir = Paths.get(this.path,"tmp", "dependencies").toString();
        // 下载分配给本机的切分文件
        if (machineCutFileVO.getMachineDOCutFileVOListMap() != null) {
            Map<String, List<CutFileVO>> map = machineCutFileVO.getMachineDOCutFileVOListMap();
            for (Map.Entry<String, List<CutFileVO>> entry : map.entrySet()) {
                if (entry.getKey().equals(new JmeterExternal(socket).getAddress())) {
                    jFileService.downloadCutFile(entry.getValue(), tmpDir);
                }
            }
        }
        // 下载无需切分的文件
        String[] csvFileIds = (taskDO.getCsv() != null && !taskDO.getCsv().isEmpty()) ? taskDO.getCsv().split(",") : new String[]{};
        for (String csvFileId : csvFileIds){
            JFileDO jFileCsvDO = jFileService.searchById(Integer.valueOf(csvFileId));
            if (!jFileCsvDO.getCut()){
                jFileService.downloadFile(Integer.valueOf(csvFileId), tmpDir);
            }
        }
        String[] jmxFileIds = (taskDO.getJmx() != null && !taskDO.getJmx().isEmpty()) ? taskDO.getJmx().split(",") : new String[]{};
        for (String jmxFileId : jmxFileIds){
            jFileService.downloadFile(Integer.valueOf(jmxFileId), tmpDir);
        }
        String[] jarFileIds = (taskDO.getJar()!= null &&!taskDO.getJar().isEmpty())? taskDO.getJar().split(",") : new String[]{};
        for (String jarFileId : jarFileIds){
            jFileService.downloadFile(Integer.valueOf(jarFileId), dependencyDir);
        }
    }

    public String mergeJtlFile(TaskDO taskDO, JFileService jFileService) {
        List<JFileDO> jFileDOS = jFileService.searchJtlByTaskId(taskDO.getTaskId());
        List<String> filePaths = new ArrayList<>();
        for (JFileDO file : jFileDOS) {
            String filePath = jFileService.downloadFile(file.getId(), null);
            filePaths.add(filePath);
        }
        // 获取文件首行
        String firstLine = "";
        if (!filePaths.isEmpty()) {
            ProcessBuilder processBuilder = new ProcessBuilder("head", "-1", filePaths.get(0));
            processBuilder.environment().putAll(System.getenv());
            try {
                Process process = processBuilder.start();
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    firstLine = reader.readLine();
                }
                int exitCode = process.waitFor();
            } catch (IOException | InterruptedException e) {
                log.error("获取jtl文件首行失败", e);
            }
        }
        // 所有文件去除首行
        for (String filePath : filePaths) {
            ProcessBuilder processBuilder = new ProcessBuilder("sed", "-i", "1d", filePath);
            processBuilder.environment().putAll(System.getenv());
            try {
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
            } catch (IOException | InterruptedException e) {
                log.error("删除jtl文件首行失败", e);
            }
        }
        // 加入文件第一行
        String newJtlPath = Paths.get(jFileService.getStoreDir(), taskDO.getTaskId() + ".jtl").toString();
        File newJtlFile = new File(newJtlPath);
        try {
            newJtlFile.createNewFile();
        } catch (IOException e) {
            log.error("创建jtl文件失败");
        }
        try (PrintWriter writer = new PrintWriter(newJtlFile)) {
            writer.println(firstLine);
        } catch (IOException e) {
            log.error("写入首行失败", e);
        }
        // 合并所有文件
        filePaths.add(0, "cat");
        filePaths.add(">>");
        filePaths.add(newJtlPath);
        String commandString = String.join(" ", filePaths);
        ProcessBuilder processBuilder = new ProcessBuilder("sh","-c",commandString);
        processBuilder.environment().putAll(System.getenv());
        try {
            Process process = processBuilder.start();
            List<String> command = processBuilder.command();
            int exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            log.error("jtl文件聚合失败", e);
        }
        log.info("合并jtl成功");
        return newJtlPath;
    }

    public String generateReport(TaskDO taskDO, String jtlPath, JFileService jFileService) {
        // 设置报告输出路径
        String outputReportPath = Paths.get(jFileService.getStoreDir(), "report_" + taskDO.getTaskId()).toString();
        JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, outputReportPath);

        try {
            JMeterUtils.setProperty("jmeter.save.saveservice.output_format", "csv");
            ReportGenerator generator = new ReportGenerator(jtlPath, null);
            generator.generate();
        } catch (ConfigurationException | GenerationException e) {
            log.error("生成报告失败", e);
            throw new RuntimeException(e);
        }
        log.info("生成report报告成功");
        return outputReportPath;
    }

    public JFileDO compressReportAndUpload(TaskDO taskDO, String reportPath, JFileService jFileService) {
        String reportZipPath = Paths.get(jFileService.getStoreDir(), "report_" + taskDO.getTaskId() + ".zip").toString();
        try {
            ZipUtil.zipFolderWithArchiveOutputStream(reportPath, reportZipPath);
        } catch (IOException e) {
            log.error("压缩报告失败", e);
            throw new RuntimeException(e);
        }
        // 上传压缩文件到minio
        JFileDO file = jFileService.createFile(reportZipPath);
        file.setTaskId(taskDO.getTaskId());
        jFileService.updateById(file);
        log.info("压缩上传报告成功");
        return file;
    }

    public void serverCollect(TaskDO taskDO, JFileService jFileService, ReportRepository reportRepository) {
        this.initServerJmeterUtils(taskDO);
        String jtlPath = this.mergeJtlFile(taskDO, jFileService);
        String outputReportPath = this.generateReport(taskDO, jtlPath, jFileService);
        JFileDO jFileDO = this.compressReportAndUpload(taskDO, outputReportPath, jFileService);
        ReportDO data = new ReportDataProcess().getData(taskDO, outputReportPath, jFileDO);
        // 数据保存至mongodb
        reportRepository.save(data);
        log.info("report上传mongodb成功");
    }
}
