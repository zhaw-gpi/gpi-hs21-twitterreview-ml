package ch.zhaw.gpi.twitterreviewprocessapplication;

import java.util.Date;

import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Named("sendTweetAdapter")
public class SendTweetDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String instanceStarted = " (Instance started: " + execution.getVariable("instanceStarted") + ")";
        System.out.println(new Date().toString() + ": Delegate Execution started for: " + execution.getProcessInstanceId() + instanceStarted);
        Thread.sleep(120000);
        System.out.println(new Date().toString() + ": Delegate Execution ended for:   " + execution.getProcessInstanceId() + instanceStarted);
    }

}
