package io.github.guojiaxing1995.easyJmeter.common.jmeter;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.MachineOnlineEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
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
        ProcessBuilder processBuilder = new ProcessBuilder( this.path + "/bin/jmeter", "--version");
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
        File file = new File(this.path + "/bin/user.properties");
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
            File file = new File(this.path + "/bin/user.properties");
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
                writer.println(property);
            } catch (IOException e) {
                log.error("写入user.properties失败，属性：" + property, e);
                throw new RuntimeException(e);
            }
        }
    }

    public void loadProperties() {
        this.setProperties("plugin_dependency_paths=../tmp/dependencies/;");
    }
}
