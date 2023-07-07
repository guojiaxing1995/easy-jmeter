package io.github.guojiaxing1995.easyJmeter.common.jmeter;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.MachineOnlineEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Slf4j
public class BasicProperties {

    private String version;

    private String path;

    private String address;

    private MachineOnlineEnum online;

    public BasicProperties() {
        this.path = System.getenv("JMETER_HOME");
        this.version();
        this.ipAddress();
        this.online();
    }

    public void version() {
        ProcessBuilder processBuilder = new ProcessBuilder( System.getenv("JMETER_HOME") + "/bin/jmeter", "--version");
        processBuilder.environment().putAll(System.getenv());
        StringBuilder outputString = new StringBuilder();
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                outputString.append(line).append("\n");
            }
            int exitCode = process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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
                log.info("获取本机ip异常");
            }
        }catch(SocketException e){
            log.info("获取本机ip异常");
            e.printStackTrace();
        }
    }

    public void online() {
        if (!this.version.isEmpty()){
            this.online = MachineOnlineEnum.ONLINE;
        } else {
            this.online = MachineOnlineEnum.OFFLINE;
        }
    }


}
