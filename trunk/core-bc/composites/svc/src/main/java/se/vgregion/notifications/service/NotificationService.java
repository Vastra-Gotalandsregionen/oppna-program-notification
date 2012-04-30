package se.vgregion.notifications.service;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portlet.social.model.SocialRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import se.vgregion.alfrescoclient.domain.Site;
import se.vgregion.notifications.NotificationException;
import se.vgregion.raindancenotifier.domain.InvoiceNotification;
import se.vgregion.usdservice.domain.Issue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;

/**
 * Service class which aggregates the functionality of other services. The methods that return a certain count are
 * annotated with @{@link Async} to enable asynchronous processing.
 *
 * @author Patrik Bergström
 */
@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private AlfrescoDocumentsService alfrescoDocumentsService;
    private NotesCalendarCounterService notesCalendarCounterService;
    private NotesEmailCounterService notesEmailCounterService;
    private RaindanceInvoiceService raindanceInvoiceService;
    private UsdIssuesService usdIssuesService;
    private SocialRelationService socialRelationService;

    /**
     * Constructor.
     */
    public NotificationService() {
        // Empty constructor is needed to make CGLIB happy
    }

    /**
     * Constructor.
     *
     * @param alfrescoDocumentsService    alfrescoDocumentsService
     * @param notesCalendarCounterService notesCalendarCounterService
     * @param notesEmailCounterService    notesEmailCounterService
     * @param raindanceInvoiceService     raindanceInvoiceService
     * @param usdIssuesService            usdIssuesService
     */
    @Autowired
    public NotificationService(AlfrescoDocumentsService alfrescoDocumentsService,
                               NotesCalendarCounterService notesCalendarCounterService,
                               NotesEmailCounterService notesEmailCounterService,
                               RaindanceInvoiceService raindanceInvoiceService,
                               UsdIssuesService usdIssuesService,
                               SocialRelationService socialRelationService) {
        this.alfrescoDocumentsService = alfrescoDocumentsService;
        this.notesCalendarCounterService = notesCalendarCounterService;
        this.notesEmailCounterService = notesEmailCounterService;
        this.raindanceInvoiceService = raindanceInvoiceService;
        this.usdIssuesService = usdIssuesService;
        this.socialRelationService = socialRelationService;
    }

    /**
     * Get the number of recently modified Alfresco {@link se.vgregion.alfrescoclient.domain.Document}s for all
     * {@link Site}s a user is a member of.
     *
     * @param screenName the user's screen name
     * @return the count wrapped in a {@link Future}
     */
    @Async
    public Future<Integer> getAlfrescoCount(String screenName) {
        List<Site> alfrescoResponse = alfrescoDocumentsService.getRecentlyModified(screenName, false);

        int n = 0;
        for (Site site : alfrescoResponse) {
            n += site.getRecentModifiedDocuments().size();
        }

        return new AsyncResult<Integer>(n);
    }

    /**
     * Get the number of USD issues for a user.
     *
     * @param screenName the user's screen name
     * @return the count wrapped in a {@link Future}
     */
    @Async
    public Future<Integer> getUsdIssuesCount(String screenName) {
        List<Issue> issues = usdIssuesService.getUsdIssues(screenName, false);

        return new AsyncResult<Integer>(issues.size());
    }

    /**
     * Get a random number between 0 and 1000.
     *
     * @return random number
     */
    @Async
    public Future<Integer> getRandomCount() {
        final int n = 1000;
        return new AsyncResult<Integer>(new Random().nextInt(n));
    }

    /**
     * Get the number of unread emails for a user.
     *
     * @param screenName the user's screen name
     * @return the count wrapped in a {@link Future}
     */
    @Async
    public Future<Integer> getEmailCount(String screenName) {
        Integer count = null;
        try {
            count = notesEmailCounterService.getCount(screenName);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new AsyncResult<Integer>(count);
    }

    /**
     * Get the number of invoices for a user.
     *
     * @param screenName the user's screen name
     * @return the count wrapped in a {@link Future}
     */
    @Async
    public Future<Integer> getInvoicesCount(String screenName) {
        List<InvoiceNotification> invoices = raindanceInvoiceService.getInvoices(screenName, false);
        return new AsyncResult<Integer>(invoices.size());
    }


//    @Async // Can't have this Async due to a bug. Some cached and potentially wrong value will be returned. Possibly
    //related to http://issues.liferay.com/browse/LPS-26465.
    public Future<Integer> getSocialRequestCount(User user) {
        return new AsyncResult<Integer>(socialRelationService.getUserRequests(user, false).size());
    }

    /**
     * Get recently modified {@link se.vgregion.alfrescoclient.domain.Document}s in a list of {@link Site}s. The
     * {@link se.vgregion.alfrescoclient.domain.Document}s are accessible by calling
     * {@link se.vgregion.alfrescoclient.domain.Site#getRecentModifiedDocuments()}.
     *
     * @param screenName the user's screen name
     * @return a list of {@link Site}s containing the recently modified
     *         {@link se.vgregion.alfrescoclient.domain.Document}s
     */
    public List<Site> getAlfrescoDocuments(String screenName) {
        List<Site> sites = alfrescoDocumentsService.getRecentlyModified(screenName, true);
        return sites;
    }

    /**
     * Get the USD issues for a user.
     *
     * @param screenName the user's screen name
     * @return a list of {@link Issue}s
     */
    public List<Issue> getUsdIssues(String screenName) {
        return usdIssuesService.getUsdIssues(screenName, true);
    }

    /**
     * Get a bops id for a user.
     *
     * @param userId the user id
     * @return a bops id
     * @see se.vgregion.usdservice.USDServiceImpl#getBopsId(java.lang.String)
     */
    public String getBopsId(String userId) {
        return usdIssuesService.getBopsId(userId);
    }

    /**
     * Get the invoices for a user.
     *
     * @param screenName the user's screen name
     * @return a list of {@link InvoiceNotification}s
     */
    public List<InvoiceNotification> getInvoices(String screenName) {
        return raindanceInvoiceService.getInvoices(screenName, true);
    }

    public List<SocialRequest> getSocialRequests(User user) {
        return socialRelationService.getUserRequests(user, true);
    }

    public Map<SocialRequest, User> getSocialRequestsWithRespectiveUser(User user) {
        return socialRelationService.getUserRequestsWithUser(user, true);
    }

    public void confirmRequest(Long requestId) throws NotificationException {
        try {
            socialRelationService.confirmRequest(requestId);
        } catch (SystemException e) {
            throw new NotificationException(e);
        } catch (PortalException e) {
            throw new NotificationException(e);
        }
    }

    public void rejectRequest(Long requestId) throws NotificationException {
        try {
            socialRelationService.rejectRequest(requestId);
        } catch (SystemException e) {
            throw new NotificationException(e);
        } catch (PortalException e) {
            throw new NotificationException(e);
        }
    }
}
