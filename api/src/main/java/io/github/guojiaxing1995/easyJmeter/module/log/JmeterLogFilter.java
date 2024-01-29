package io.github.guojiaxing1995.easyJmeter.module.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class JmeterLogFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String logMessage = event.getMessage();
        String loggerName = event.getLoggerName();
        if (loggerName.startsWith("org.apache.jmeter") ||
                logMessage.contains("debug_start") || logMessage.contains("debug_end")) {
            return FilterReply.ACCEPT; // 接受来自org.apache.jmeter 的所有日志
        } else {
            return FilterReply.DENY; // 过滤掉其他来源的日志
        }
    }
}
