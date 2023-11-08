package io.github.guojiaxing1995.easyJmeter.common.jmeter;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;

public interface LinkStrategy {

    void setTask(TaskDO taskDO);

    Boolean reportSuccess() throws JsonProcessingException;

    Boolean reportFail();

    Boolean interruptThread();

}
