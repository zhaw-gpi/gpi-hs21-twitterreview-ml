package ch.zhaw.gpi.admock;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

@Entity
public class User {
    @Id
    private String userName;
    private String firstName;
    private String officialName;
    private String eMail;
    private String phoneNumber;
    private String notificationChannel;
    @ManyToOne
    @JsonUnwrapped
    private OrgUnit homeOrganization;


    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getOfficialName() {
        return officialName;
    }
    public void setOfficialName(String officialName) {
        this.officialName = officialName;
    }
    public String geteMail() {
        return eMail;
    }
    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getNotificationChannel() {
        return notificationChannel;
    }
    public void setNotificationChannel(String notificationChannel) {
        this.notificationChannel = notificationChannel;
    }
    public OrgUnit getHomeOrganization() {
        return homeOrganization;
    }
    public void setHomeOrganization(OrgUnit homeOrganization) {
        this.homeOrganization = homeOrganization;
    }

    
}
