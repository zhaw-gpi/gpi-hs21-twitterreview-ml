package ch.zhaw.gpi.twitterreviewprocessapplication.ldap;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class OrgUnit {
    @Id
    private String shortName;
    private String longName;


    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    public String getLongName() {
        return longName;
    }
    public void setLongName(String longName) {
        this.longName = longName;
    }

    
}
