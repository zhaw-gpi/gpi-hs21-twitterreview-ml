package ch.zhaw.gpi.twitterreviewprocessapplication;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TwilioClient {

    @Value("${phoneNumber}")
    private String myTwilioPhoneNumber;

    @Autowired
    public TwilioClient(@Value("${twilioAccountSid}") String twilioAccountSid,
            @Value("${twilioAuthToken}") String twilioAuthToken) {
        Twilio.init(twilioAccountSid, twilioAuthToken);
    }

    public void sendSms(String to, String content) {
        Message.creator(new PhoneNumber(to), new PhoneNumber(myTwilioPhoneNumber), content).create();
    }

    public void notifyViaCall(String to, String content) {
        Call.creator(new PhoneNumber(to), new PhoneNumber(myTwilioPhoneNumber),
                new Twiml("<Response><Say language=\"de-DE\" loop=\"2\">" + content + "</Say><Hangup/></Response>")).create();
    }
}
