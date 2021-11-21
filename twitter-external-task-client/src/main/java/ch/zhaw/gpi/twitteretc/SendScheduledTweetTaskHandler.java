package ch.zhaw.gpi.twitteretc;

import java.util.Date;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

@Component
@ExternalTaskSubscription("SendScheduledTweet")
public class SendScheduledTweetTaskHandler implements ExternalTaskHandler {

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String instanceStarted = " (Instance started: " + externalTask.getVariable("instanceStarted") + ")";
        System.out.println(new Date().toString() + ": Task Execution started for: " + externalTask.getProcessInstanceId() + instanceStarted);
        
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        externalTaskService.complete(externalTask);
        System.out.println(new Date().toString() + ": Task Execution ended for:   " + externalTask.getProcessInstanceId() + instanceStarted);
        
    }

}