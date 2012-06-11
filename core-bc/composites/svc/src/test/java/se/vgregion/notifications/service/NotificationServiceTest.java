package se.vgregion.notifications.service;

import com.liferay.portal.model.User;
import org.junit.Test;
import se.vgregion.notifications.domain.NotificationServiceName;

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

    private NotificationService notificationService = new NotificationService(
            alfrescoDocumentsService,
            notesCalendarCounterService,
            notesEmailCounterService,
            raindanceInvoiceService,
            usdIssuesService,
            socialRelationService,
            medControlService);

    @Test
    public void testGetCount() throws Exception {

        User user = mock(User.class);
        when(user.getScreenName()).thenReturn("anyScreenName");

        for (String s : NotificationServiceName.allNamesAsList()) {

            notificationService.getCount(s, user);

        }

        // No exception is good, meaning that no exception related to reflection occured.
    }
}
