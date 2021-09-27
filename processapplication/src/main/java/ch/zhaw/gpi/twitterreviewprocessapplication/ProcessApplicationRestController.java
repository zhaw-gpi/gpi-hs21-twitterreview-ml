package ch.zhaw.gpi.twitterreviewprocessapplication;

import java.util.HashMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ProcessApplicationRestController {
    
    @Autowired
    private ProcessEngine processEngine;

    @PostMapping(value="/handle-tweet-proposal")
    public ResponseEntity<String> handleTweetProposal(TweetProposal tweetProposal) {
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("anfrageStellenderBenutzer", tweetProposal.getUserName());
        variables.put("tweetContent", tweetProposal.getTweetContent());
        processEngine.getRuntimeService().startProcessInstanceByKey("Process_TwitterReview", variables);
        
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value="/cancel-twitter-review")
    public ResponseEntity<String> cancelTwitterReview(String businessKey) {
        processEngine.getRuntimeService()
            .createMessageCorrelation("MessageCancelTwitterReview")
            .processInstanceBusinessKey(businessKey)
            .correlate();

        return new ResponseEntity<>(HttpStatus.OK);
    }
    
}
