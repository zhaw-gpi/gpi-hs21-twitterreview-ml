package ch.zhaw.gpi.twitteretc;

import java.util.List;

import javax.annotation.PostConstruct;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

@Component
@ExternalTaskSubscription("SendTweet")
public class SendTweetTaskHandler implements ExternalTaskHandler {

    private Twitter twitter; 

    @PostConstruct
    public void init(){
        twitter = TwitterFactory.getSingleton();

        List<Status> statuses;
        try {
            statuses = twitter.getHomeTimeline();
            System.out.println("TWITTER: Erfolgreich angemeldet. Im Folgenden die letzten maximal 20 Tweets");
            for (Status status : statuses) {
                System.out.println("TWITTER: " + status.getText());
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
            
    }


    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String tweetContent = (String) externalTask.getVariable("tweetContent");

        try {
            Status status = twitter.updateStatus(tweetContent);
            System.out.println("TWITTER: Erfolgreich getweetet:" + status.getText());
            externalTaskService.complete(externalTask);
        } catch (Exception e) {
            if(e instanceof TwitterException){
                TwitterException te = (TwitterException) e;
                if(te.getErrorCode() == 187){
                    externalTaskService.handleBpmnError(externalTask, "DuplicateTweet");
                }
            } else {
                externalTaskService.handleFailure(externalTask, "Fehler beim Posten des Tweets", e.getLocalizedMessage(), 0, 1);
            }
        }
    }
}