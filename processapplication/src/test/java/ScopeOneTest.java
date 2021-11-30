import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import ch.zhaw.gpi.twitterreviewprocessapplication.GetUserInformationDelegate;
import ch.zhaw.gpi.twitterreviewprocessapplication.SendGridClient;
import ch.zhaw.gpi.twitterreviewprocessapplication.SendNotificationDelegate;
import ch.zhaw.gpi.twitterreviewprocessapplication.TwilioClient;
import ch.zhaw.gpi.twitterreviewprocessapplication.UserRepresentation;
import ch.zhaw.gpi.twitterreviewprocessapplication.UserRepresentation.Links;
import ch.zhaw.gpi.twitterreviewprocessapplication.UserRepresentation.Links.HomeOrganization;

@Deployment(resources = { "twitter-review-process.bpmn", "TweetContentValidation.dmn" })
public class ScopeOneTest {

  // In-Memory Process Engine mit Testabdeckungs-Generierung instanzieren
  @Rule
  @ClassRule
  public static ProcessEngineRule processEngineRule = TestCoverageProcessEngineRuleBuilder.create().build();

  // SendGrid- und Twilio-Client mocken, SendNotificationDelegate hingegen "echt"
  // instanzieren mit den Mocks injected
  @Mock
  private SendGridClient sendGridClient;

  @Mock
  private TwilioClient twilioClient;

  @InjectMocks
  private SendNotificationDelegate sendNotificationDelegate = new SendNotificationDelegate();

  // RestTemplate mocken, GetUserInformationDelegate hingegen "echt" instanzieren
  // mit dem Mock injected
  @Mock
  private RestTemplate activeDirectoryRestClient = new RestTemplate();

  @InjectMocks
  private GetUserInformationDelegate getUserInformationDelegate = new GetUserInformationDelegate();

  @Before
  public void setUp() {
    // Sicherstellen, dass die oberhalb deklarierten Mocks und InjectMocks auch
    // ausgeführt werden, bevor der Test startet
    MockitoAnnotations.openMocks(this);
  }

  // Happy-Path-Test
  @Test
  public void testHappyPath() {
    // Eine neue Prozessinstanz starten anstatt dem echten Formular ausfüllen sowie
    // prüfen, ob Prozessinstanz läuft
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("Process_TwitterReview", withVariables(
        "tweetContent", "Ich liebe GPI passt nun!", "anfrageStellenderBenutzer", "a"));

    assertThat(processInstance).isStarted();

    // Den gemockten GetUserInformationDelegate als getUserInformationAdapter
    // bekannt machen
    Mocks.register("getUserInformationAdapter", getUserInformationDelegate);

    // Da leider eigentliche Rest-Calls direkt in JavaDelegate, müssen wir nun
    // umständlich die Antwort der exchange-Methode mocken für die zwei Varianten
    // (User und OrgUnit)
    UserRepresentation testUser = new UserRepresentation();
    testUser.setFirstName("Björn");
    testUser.setOfficialName("Scheppler");
    testUser.setNotificationChannel("email");
    testUser.seteMail("scep@zhaw.ch");
    Links testLinks = testUser.new Links();
    HomeOrganization testHomeOrg = testLinks.new HomeOrganization();
    testHomeOrg.setHref("http://localhost:8070/api/orgunits/gl");
    testLinks.setHomeOrganization(testHomeOrg);
    testUser.set_links(testLinks);
    ResponseEntity<UserRepresentation> testUserResponseEntity = new ResponseEntity<UserRepresentation>(testUser,
        HttpStatus.OK);
    Mockito.when(activeDirectoryRestClient.exchange(
        ArgumentMatchers.startsWith("http://localhost:8070/api/users/"),
        ArgumentMatchers.eq(HttpMethod.GET),
        ArgumentMatchers.isNull(),
        ArgumentMatchers.<Class<UserRepresentation>>any(),
        ArgumentMatchers.<ParameterizedTypeReference<List<Object>>>any())).thenReturn(testUserResponseEntity);

    ResponseEntity<String> testOrgUnitResponseEntity = new ResponseEntity<String>(
        "{\"homeOrganization\": \"Geschäftsleitung\"}", HttpStatus.OK);
    Mockito.when(activeDirectoryRestClient.exchange(
        ArgumentMatchers.startsWith("http://localhost:8070/api/orgunits/"),
        ArgumentMatchers.eq(HttpMethod.GET),
        ArgumentMatchers.isNull(),
        ArgumentMatchers.<Class<String>>any())).thenReturn(testOrgUnitResponseEntity);

    // Wegen Asynchronous after den einzigen Job ausführen und prüfen, ob Service
    // Task erfolgreich hinter sich und ob die exchange-Methode aufgerufen wurde
    execute(job());
    assertThat(processInstance).isWaitingAt("TweetTextAutomatischPrufenTask");
    verify(activeDirectoryRestClient).exchange("http://localhost:8070/api/users/{username}", HttpMethod.GET, null,
        UserRepresentation.class, "a");
    verify(activeDirectoryRestClient).exchange("http://localhost:8070/api/orgunits/gl", HttpMethod.GET, null,
        String.class);

    // Dito wegen Asynchronous After und prüfen, ob bei User Task wartend
    execute(job());
    assertThat(processInstance).isWaitingAt("TweetAnfragePrufenTask1");

    // User Task beenden und wegen Asynchronous after Job ausführen
    complete(task(), withVariables("checkResult", "accepted"));
    execute(job());

    // Prüfen ob bei Service Task mit External Task-Implementation und diesen
    // erledigen (da keine Return Variablen), super simpel im Vergleich zum Java
    // Delegate oben
    assertThat(processInstance).isWaitingAt("TweetSendenTask");
    complete(externalTask("TweetSendenTask"));

    // Den gemockten SendNotificationDelegate als sendNotificationAdapter bekannt
    // machen
    Mocks.register("sendNotificationAdapter", sendNotificationDelegate);

    // Wegen Asynchronous after Job ausführen. Ein customized Mocking der
    // sendHtmlMail-Methode ist nicht erforderlich da void als Return-Type. Doch
    // selbst wenn, wäre es aufgrund Schichtentrennung einfacher als bei erstem
    // JavaDelegate
    execute(job());

    assertThat(processInstance).isEnded();
  }

  @Test
  public void testAutomaticTweetRejectionPath() throws InterruptedException {
    // Eine neue Prozessinstanz "starten", aber direkt vor der Aktivität
    // TweetTextAutomatischPrufenTask
    ProcessInstance processInstance = runtimeService().createProcessInstanceByKey("Process_TwitterReview")
        .startBeforeActivity("TweetTextAutomatischPrufenTask").setVariables(withVariables("tweetContent",
            "Ich bin zu kurz", "userMail", "max.muster@a.ch", "userNotificationChannel", "email"))
        .execute();

    // Prüfen, ob die Prozessinstanz läuft
    assertThat(processInstance).isStarted();

    // Den gemockten SendNotificationDelegate als sendNotificationAdapter bekannt
    // machen
    Mocks.register("sendNotificationAdapter", sendNotificationDelegate);

    // Wegen Asynchronous After Job ausführen
    execute(job());

    // Prüfen, ob Prozess beendet wurde
    assertThat(processInstance).hasPassed("TweetAbgelehntEndEvent1").isEnded();
  }

}