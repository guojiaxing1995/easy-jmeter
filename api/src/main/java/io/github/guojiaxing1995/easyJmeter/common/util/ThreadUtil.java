package io.github.guojiaxing1995.easyJmeter.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class ThreadUtil {

    public static Thread getThread(String threadName){
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int noThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[noThreads];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < noThreads; i++) {
            if (lstThreads[i].getName().equals(threadName)) {
                return lstThreads[i];
            }
        }
        return null;
    }

    public static Boolean isExist(String threadName){
        return getThread(threadName) != null;
    }

    public static void interruptThread(String threadName){
        Thread thread = ThreadUtil.getThread(threadName);
        if (thread != null) {
            thread.interrupt();
        }
        while (isExist(threadName)){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean isProcessContainingName(String processName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ProcessBuilder("ps", "aux").start().getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(processName)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            log.error("获取进程信息失败", e);
            throw new RuntimeException(e);
        }
    }

    public static void killProcessContainingName(String processName) {
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ProcessBuilder("ps", "aux").start().getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(processName)) {
                        String pid = line.split("\\s+")[1]; // 获取进程ID（第二列）
                        // 终止进程
                        Process killProcess = new ProcessBuilder("kill", pid).start();
                        killProcess.waitFor(); // 等待进程终止
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            log.error("获取进程信息失败", e);
            throw new RuntimeException(e);
        }
    }


}
