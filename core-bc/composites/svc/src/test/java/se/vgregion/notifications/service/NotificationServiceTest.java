package se.vgregion.notifications.service;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portlet.social.model.SocialRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.vgregion.alfrescoclient.domain.Document;
import se.vgregion.alfrescoclient.domain.Site;
import se.vgregion.liferay.expando.UserExpandoHelper;
import se.vgregion.notifications.NotificationException;
import se.vgregion.notifications.domain.CountResult;
import se.vgregion.notifications.domain.NotificationServiceName;
import se.vgregion.portal.medcontrol.domain.DeviationCase;
import se.vgregion.raindancenotifier.domain.InvoiceNotification;
import se.vgregion.usdservice.domain.Issue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Patrik Bergström
 */
public class NotificationServiceTest {

    private AlfrescoDocumentsService alfrescoDocumentsService = mock(AlfrescoDocumentsService.class);
    private NotesCalendarCounterService notesCalendarCounterService = mock(NotesCalendarCounterService.class);
    private NotesEmailCounterService notesEmailCounterService = mock(NotesEmailCounterService.class);
    private RaindanceInvoiceService raindanceInvoiceService = mock(RaindanceInvoiceService.class);
    private UsdIssuesService usdIssuesService = mock(UsdIssuesService.class);
    private SocialRelationService socialRelationService = mock(SocialRelationService.class);
    private MedControlService medControlService = mock(MedControlService.class);
    private UserExpandoHelper userExpandoHelper = mock(UserExpandoHelper.class);

    private NotificationService notificationService = new NotificationService(
            alfrescoDocumentsService,
            notesCalendarCounterService,
            notesEmailCounterService,
            raindanceInvoiceService,
            usdIssuesService,
            socialRelationService,
            medControlService,
            userExpandoHelper);

    @Before
    public void setup() {
        when(userExpandoHelper.get(eq("isDominoUser"), any(User.class))).thenReturn(Boolean.TRUE);
    }

    @Test
    public void testGetCount() throws Exception {

        User user = mock(User.class);
        when(user.getScreenName()).thenReturn("anyScreenName");

        for (String s : NotificationServiceName.allNamesAsList()) {

            notificationService.getCount(s, user);

        }

        // No exception is good, meaning that no exception related to reflection occured.
    }

    @Test
    public void testGetAlfrescoCount() throws ExecutionException, InterruptedException {

        // Given
        Site site = new Site();
        Document document = new Document();
        site.setRecentModifiedDocuments(Arrays.asList(document));
        when(alfrescoDocumentsService.getRecentlyModified(anyString(), anyBoolean())).thenReturn(Arrays.asList(site));

        // When
        Future<CountResult> count = notificationService.getAlfrescoCount("anyScreenName");

        // Then
        assertEquals(1, (int) count.get().getCount());
    }

    @Test
    public void testGetUsdIssuesCount() throws Exception {

        // Given
        Issue issue = new Issue();
        when(usdIssuesService.getUsdIssues(anyString(), anyBoolean())).thenReturn(Arrays.asList(issue));

        // When
        Future<CountResult> issues = notificationService.getUsdIssuesCount("anyScreenName");

        // Then
        assertEquals(1, (int) issues.get().getCount());

    }

    @Test
    public void testGetEmailCount() throws Exception {

        // Given
        when(notesEmailCounterService.getCount(anyString())).thenReturn(1);
        User user = mock(User.class);
        when(user.getScreenName()).thenReturn("anyScreenName");

        // When
        Future<CountResult> count = notificationService.getEmailCount(user);

        // Then
        assertEquals(1, (int) count.get().getCount());
    }

    @Test
    public void testGetInvoicesCount() throws Exception {

        // Given
        InvoiceNotification invoiceNotification = new InvoiceNotification();

        when(raindanceInvoiceService.getInvoices(anyString(), anyBoolean())).thenReturn(Arrays.asList(
                invoiceNotification));

        // When
        Future<CountResult> count = notificationService.getInvoicesCount("anyScreenName");

        // Then
        assertEquals(1, (int) count.get().getCount());
    }

    @Test
    public void testGetSocialRequestCount() throws Exception {

        // Given
        SocialRequest socialRequest = mock(SocialRequest.class);
        when(socialRelationService.getUserRequests(any(User.class), anyBoolean())).thenReturn(Arrays.asList(socialRequest));

        // When
        User user = mock(User.class);
        Future<CountResult> count = notificationService.getSocialRequestCount(user);

        // Then
        assertEquals(1, (int) count.get().getCount());
    }

    @Test
    public void testGetMedControlCasesCount() throws Exception {

        // Given
        DeviationCase deviationCase = new DeviationCase();
        when(medControlService.listDeviationCases(anyString(), anyBoolean())).thenReturn(Arrays.asList(deviationCase));

        // When
        User user = mock(User.class);
        Future<CountResult> count = notificationService.getMedControlCasesCount("anyScreenName");

        // Then
        assertEquals(1, (int) count.get().getCount());
    }

    @Test
    public void testGetAlfrescoDocuments() {

        // Given
        Site site = new Site();
        Document document = new Document();
        site.setRecentModifiedDocuments(Arrays.asList(document));
        when(alfrescoDocumentsService.getRecentlyModified(anyString(), anyBoolean())).thenReturn(Arrays.asList(site));

        // When
        List<Site> sites = notificationService.getAlfrescoDocuments("anyScreenName");

        // Then
        assertEquals(1, sites.get(0).getRecentModifiedDocuments().size());
    }

    @Test
    public void testGetUsdIssues() {

        // Given
        Issue issue = new Issue();
        when(usdIssuesService.getUsdIssues(anyString(), anyBoolean())).thenReturn(Arrays.asList(issue));

        // When
        List<Issue> issues = notificationService.getUsdIssues("anyScreenName");

        // Then
        assertEquals(1, issues.size());
    }

    @Test
    public void testGetBopsId() {

        // Given
        when(usdIssuesService.getBopsId(anyString())).thenReturn("asdf");

        // When
        String id = notificationService.getBopsId("ewiljfaölj");

        // Then
        assertEquals("asdf", id);
    }

    @Test
    public void testGetInvoices() {

        // Given
        InvoiceNotification invoiceNotification = new InvoiceNotification();
        when(raindanceInvoiceService.getInvoices(anyString(), anyBoolean())).thenReturn(Arrays.asList(
                invoiceNotification));

        // When
        List<InvoiceNotification> invoiceNotifications = notificationService.getInvoices("asldkfj");

        // Then
        assertEquals(1, invoiceNotifications.size());
    }

    @Test
    public void testGetSocialRequests() {

        // Given
        SocialRequest socialRequest = mock(SocialRequest.class);
        when(socialRelationService.getUserRequests(any(User.class), anyBoolean())).thenReturn(Arrays.asList(socialRequest));

        // When
        User user = mock(User.class);
        List<SocialRequest> requests = notificationService.getSocialRequests(user);

        // Then
        assertEquals(1, requests.size());
    }

    @Test
    public void testGetSocialRequestsWithRespectiveUser() {

        // Given
        HashMap<SocialRequest, User> value = new HashMap<SocialRequest, User>();
        SocialRequest socialRequest = mock(SocialRequest.class);
        User mock = mock(User.class);
        value.put(socialRequest, mock);
        when(socialRelationService.getUserRequestsWithUser(any(User.class), anyBoolean())).thenReturn(value);

        // When
        User user = mock(User.class);
        Map<SocialRequest,User> socialRequestsWithRespectiveUser = notificationService.getSocialRequestsWithRespectiveUser(user);

        // Then
        assertEquals(1, socialRequestsWithRespectiveUser.size());
    }

    @Test
    public void testGetMedControlCases() {

        // Given
        DeviationCase deviationCase = new DeviationCase();
        when(medControlService.listDeviationCases(anyString(), anyBoolean())).thenReturn(Arrays.asList(deviationCase));

        // When
        User user = mock(User.class);
        List<DeviationCase> medControlCases = notificationService.getMedControlCases(user);

        // Then
        assertEquals(1, medControlCases.size());
    }

    @Test(expected = NotificationException.class)
    public void testConfirmRequest() throws SystemException, PortalException, NotificationException {

        // Given
        Mockito.doThrow(new SystemException()).when(socialRelationService).confirmRequest(anyLong());

        // When
        notificationService.confirmRequest(23353L);
    }

    @Test(expected = NotificationException.class)
    public void testRejectRequest() throws SystemException, PortalException, NotificationException {

        // Given
        Mockito.doThrow(new SystemException()).when(socialRelationService).rejectRequest(anyLong());

        // When
        notificationService.rejectRequest(23353L);
    }
}
