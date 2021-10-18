package ch.zhaw.gpi.twitterreviewprocessapplication;

import java.util.Optional;

import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;

import ch.zhaw.gpi.twitterreviewprocessapplication.ldap.User;
import ch.zhaw.gpi.twitterreviewprocessapplication.ldap.UserRepository;

@Named("getUserInformationAdapter")
public class GetUserInformationDelegate implements JavaDelegate{

    @Autowired
    private UserRepository userRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String userName = (String) execution.getVariable("anfrageStellenderBenutzer");
        
        Optional<User> user = userRepository.findById(userName);

        String fullUserDescription;
        String eMail;

        if(user.isPresent()){
            User existingUser = user.get();
            eMail = existingUser.geteMail();
            fullUserDescription = existingUser.getFirstName() + " " + existingUser.getOfficialName() + " (" + existingUser.getHomeOrganization().getLongName() + ")";
        } else {
            fullUserDescription = "Alien (Universe)";
            eMail = "alien@universe.os";
        }

        execution.setVariable("userMail", eMail);
        execution.setVariable("userFullDescription", fullUserDescription);
        
    }
    
}
