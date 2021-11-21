package ch.zhaw.gpi.twitterreviewprocessapplication;

import java.io.IOException;
import java.util.Map;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGridAPI;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendGridClient {

    @Autowired
    private SendGridAPI sendGridAPI;

    public void sendSimpleMail(Email to, Email from, String subject, String body) {
        Content sendGridBody = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, sendGridBody);
        sendMailWithSendGrid(mail);
    }

    public void sendHtmlMail(Email to, Email from, String subject, String templateId, Map<String, Object> templateData){
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(subject);

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        templateData.forEach((key, value) -> personalization.addDynamicTemplateData(key, value));
        mail.addPersonalization(personalization);
        mail.setTemplateId(templateId);
        
        sendMailWithSendGrid(mail);
    }

    private void sendMailWithSendGrid(Mail mail) {
        Request mailRequest = new Request();
        try {
            mailRequest.setMethod(Method.POST);
            mailRequest.setEndpoint("mail/send");
            mailRequest.setBody(mail.build());
            Response response = sendGridAPI.api(mailRequest);
            System.out.println(response.getStatusCode());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
