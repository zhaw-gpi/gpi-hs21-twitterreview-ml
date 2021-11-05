package ch.zhaw.gpi.admock;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class OrgUnit {
    @Id
    @JsonIgnore
    private String shortName;
    @JsonProperty("homeOrganization")
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
