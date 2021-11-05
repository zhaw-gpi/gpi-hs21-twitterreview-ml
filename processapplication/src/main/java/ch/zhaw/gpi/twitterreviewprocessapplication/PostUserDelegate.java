package ch.zhaw.gpi.twitterreviewprocessapplication;

import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import camundajar.impl.com.google.gson.JsonObject;

@Named("postUserAdapter")
public class PostUserDelegate implements JavaDelegate {

    @Autowired
    private RestTemplate activeDirectoryRestClient; 

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String userName = (String) execution.getVariable("username");
        String firstName = (String) execution.getVariable("firstname");
        String homeOrganization = (String) execution.getVariable("homeorg");

        JsonObject userAsJsonObject = new JsonObject();
        userAsJsonObject.addProperty("userName", userName);
        userAsJsonObject.addProperty("firstName", firstName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> httpEntity = new HttpEntity<String>(userAsJsonObject.toString(), headers);

        try {
            activeDirectoryRestClient.exchange("http://localhost:8070/api/users", HttpMethod.POST, httpEntity, Void.class);

            // Vereinfachte Annahme, dass homeorg a) existiert und b) korrekt angegeben wurde
            headers.setContentType(MediaType.valueOf("text/uri-list"));
            httpEntity = new HttpEntity<String>("http://localhost:8070/api/orgUnits/" + homeOrganization, headers);
            activeDirectoryRestClient.exchange("http://localhost:8070/api/users/" + userName + "/homeOrganization", HttpMethod.PUT, httpEntity, Void.class);
        } catch (Exception e) {
            throw e;
        }

    }
    
}
