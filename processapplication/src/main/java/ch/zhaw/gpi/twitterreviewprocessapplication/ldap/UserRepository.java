package ch.zhaw.gpi.twitterreviewprocessapplication.ldap;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    
}
