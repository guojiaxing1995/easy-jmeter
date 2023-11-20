package io.github.guojiaxing1995.easyJmeter.common.util;

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
}
