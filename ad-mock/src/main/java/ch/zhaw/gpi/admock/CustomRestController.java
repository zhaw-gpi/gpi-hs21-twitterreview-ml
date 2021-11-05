package ch.zhaw.gpi.admock;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@RestController
public class CustomRestController {
    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value="/customapi/users/{username}", method=RequestMethod.GET)
    public ResponseEntity<User> requestMethodName(@PathVariable String username) {

        Optional<User> user = userRepository.findById(username);

        if(user.isPresent()){
            return new ResponseEntity<User>(user.get(), HttpStatus.OK);
        } else  {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
