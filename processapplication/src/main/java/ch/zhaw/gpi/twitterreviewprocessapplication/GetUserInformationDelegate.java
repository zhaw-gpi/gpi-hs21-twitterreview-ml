package ch.zhaw.gpi.twitterreviewprocessapplication;

import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import camundajar.impl.com.google.gson.JsonObject;
import camundajar.impl.com.google.gson.JsonParser;

@Named("getUserInformationAdapter")
public class GetUserInformationDelegate implements JavaDelegate{

    @Bean(name = "activeDirectoryRestClient")
    public RestTemplate getActiveDirectoryRestClient(RestTemplateBuilder builder){
        return builder.build();
    }

    @Autowired
    private RestTemplate activeDirectoryRestClient; 

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String userName = (String) execution.getVariable("anfrageStellenderBenutzer");

        String fullUserDescription = "";
        String eMail;

        try {
            ResponseEntity<UserRepresentation> userResponse = activeDirectoryRestClient.exchange("http://localhost:8070/api/users/{username}", HttpMethod.GET, null, UserRepresentation.class, userName);

            UserRepresentation foundUser = userResponse.getBody();
            eMail = foundUser.geteMail();

            String homeOrganizationUri = foundUser.get_links().getHomeOrganization().getHref();

            ResponseEntity<String> orgUnitResponse = activeDirectoryRestClient.exchange(homeOrganizationUri, HttpMethod.GET, null, String.class);

            JsonObject orgUnitResponseAsJsonObject = new JsonParser().parse(orgUnitResponse.getBody()).getAsJsonObject();
            
            String homeOrganization = orgUnitResponseAsJsonObject.get("homeOrganization").getAsString();

            fullUserDescription = foundUser.getFirstName() + " " + foundUser.getOfficialName() + " (" + homeOrganization + ")";
        } catch (Exception e) {
            fullUserDescription = "Alien (Universe)";
            eMail = "alien@universe.os";
        }
        execution.setVariable("userMail", eMail);
        execution.setVariable("userFullDescription", fullUserDescription);
    }
}
