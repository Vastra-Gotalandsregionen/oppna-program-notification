package se.vgregion.notifications.service;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portlet.social.model.SocialRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import se.vgregion.alfrescoclient.domain.Site;
import se.vgregion.liferay.expando.UserExpandoHelper;
import se.vgregion.notifications.NotificationException;
import se.vgregion.notifications.UserSiteCredentialNotFoundException;
import se.vgregion.notifications.domain.CountResult;
import se.vgregion.portal.medcontrol.domain.DeviationCase;
import se.vgregion.raindancenotifier.domain.InvoiceNotification;
import se.vgregion.usdservice.domain.Issue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
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
@SuppressWarnings("unchecked")
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private AlfrescoDocumentsService alfrescoDocumentsService;
    private NotesCalendarCounterService notesCalendarCounterService;
    private NotesEmailCounterService notesEmailCounterService;
    private RaindanceInvoiceService raindanceInvoiceService;
    private UsdIssuesService usdIssuesService;
    private SocialRelationService socialRelationService;
    private MedControlService medControlService;
    private UserExpandoHelper userExpandoHelper;

    @Value("${iNotesUrl}")
    private String iNotesUrl;

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
     * @param socialRelationService       socialRelationService
     * @param medControlService           medControlService
     * @param userExpandoHelper           userExpandoHelper
     */
    @Autowired
    public NotificationService(AlfrescoDocumentsService alfrescoDocumentsService,
                               NotesCalendarCounterService notesCalendarCounterService,
                               NotesEmailCounterService notesEmailCounterService,
                               RaindanceInvoiceService raindanceInvoiceService,
                               UsdIssuesService usdIssuesService,
                               SocialRelationService socialRelationService,
                               MedControlService medControlService,
                               UserExpandoHelper userExpandoHelper) {
        this.alfrescoDocumentsService = alfrescoDocumentsService;
        this.notesCalendarCounterService = notesCalendarCounterService;
        this.notesEmailCounterService = notesEmailCounterService;
        this.raindanceInvoiceService = raindanceInvoiceService;
        this.usdIssuesService = usdIssuesService;
        this.socialRelationService = socialRelationService;
        this.medControlService = medControlService;
        this.userExpandoHelper = userExpandoHelper;
    }

    /**
     * Get the count for a given service. The method uses reflection to delegate to the target method. E.g. if the
     * serviceName is given as "alfresco" the call will be delegated to the
     * {@link NotificationService#getAlfrescoCount(java.lang.String)} method. The screenName of the {@link User}
     * instance will be used if the target method requires a {@link String} parameter and the {@link User} instance
     * itself will be used if the target method requires a {@link User} parameter.
     *
     * @param serviceName the serviceName
     * @param user        the {@link User} instance
     * @return the count wrapped in a {@link Future}
     * @throws NotificationException NotificationException
     */
    @Async
    public Future<CountResult> getCount(String serviceName, User user) throws NotificationException {
        // Make first letter upper-case.
        serviceName = serviceName.substring(0, 1).toUpperCase(Locale.getDefault())
                + serviceName.substring(1, serviceName.length());

        try {
            // First try with String for screenName
            Method method = this.getClass().getDeclaredMethod("get" + serviceName + "Count", String.class);
            return (Future<CountResult>) method.invoke(this, user.getScreenName());
        } catch (NoSuchMethodException e) {
            // No method found, try with User class instead
            try {
                Method method = this.getClass().getDeclaredMethod("get" + serviceName + "Count", User.class);
                return (Future<CountResult>) method.invoke(this, user);
            } catch (NoSuchMethodException e1) {
                throw new RuntimeException(e1);
            } catch (InvocationTargetException e1) {
                throw new NotificationException(e1);
            } catch (IllegalAccessException e1) {
                throw new RuntimeException(e1);
            }
        } catch (InvocationTargetException e) {
            throw new NotificationException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the number of recently modified Alfresco {@link se.vgregion.alfrescoclient.domain.Document}s for all
     * {@link Site}s a user is a member of.
     *
     * @param screenName the user's screen name
     * @return the count wrapped in a {@link Future}
     */
    @Async
    public Future<CountResult> getAlfrescoCount(String screenName) {
        List<Site> alfrescoResponse = alfrescoDocumentsService.getRecentlyModified(screenName, false);

        if (alfrescoResponse == null) {
            return new AsyncResult<CountResult>(CountResult.createNullResult());
        }

        int n = 0;
        for (Site site : alfrescoResponse) {
            n += site.getRecentModifiedDocuments().size();
        }

        return new AsyncResult<CountResult>(CountResult.createWithCount(n));
    }

    /**
     * Get the number of USD issues for a user.
     *
     * @param screenName the user's screen name
     * @return the count wrapped in a {@link Future}
     */
    @Async
    public Future<CountResult> getUsdIssuesCount(String screenName) {
        List<Issue> issues = usdIssuesService.getUsdIssues(screenName, false);

        if (issues == null) {
            return new AsyncResult<CountResult>(CountResult.createNullResult());
        }

        return new AsyncResult<CountResult>(CountResult.createWithCount(issues.size()));
    }

    /**
     * Get a random number between 0 and 1000.
     *
     * @return random number
     */
    @Async
    public Future<CountResult> getRandomCount() {
        final int n = 1000;
        return new AsyncResult<CountResult>(CountResult.createWithCount(new Random().nextInt(n)));
    }

    /**
     * Get the number of unread emails for a user.
     *
     * @param user the user
     * @return the count wrapped in a {@link Future}
     */
    @Async
    public Future<CountResult> getEmailCount(User user) {
        Integer count = null;
        if (!(Boolean) userExpandoHelper.get("isDominoUser", user)) {
            return new AsyncResult<CountResult>(CountResult.createNullResult());
        }
        try {
            count = notesEmailCounterService.getCount(user.getScreenName());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (UserSiteCredentialNotFoundException e) {
            return new AsyncResult<CountResult>(CountResult.createWithMessage("Du har inte angivit dina"
                    + " inloggningsuppgifter till iNotes. Gå <a href=\"" + iNotesUrl + "\">hit</a> för att ange dem."
                    + " Efter det kommer du se när du fått ny e-post här. Observera att det kan ta några minuter innan"
                    + " ändringen träder i kraft."));
        }
        return new AsyncResult<CountResult>(CountResult.createWithCount(count));
    }

    /**
     * Get the number of invoices for a user.
     *
     * @param screenName the user's screen name
     * @return the count wrapped in a {@link Future}
     */
    @Async
    public Future<CountResult> getInvoicesCount(String screenName) {
        List<InvoiceNotification> invoices = raindanceInvoiceService.getInvoices(screenName, false);

        if (invoices == null) {
            return new AsyncResult<CountResult>(CountResult.createNullResult());
        }

        return new AsyncResult<CountResult>(CountResult.createWithCount(invoices.size()));
    }


    /**
     * Get the number of social requests for a user.
     *
     * @param user the regarded user
     * @return the count wrapped in a {@link Future}
     */
    //@Async // Can't have this Async due to a liferay bug. Some cached and potentially wrong value will be returned.
    // Possibly related to http://issues.liferay.com/browse/LPS-26465.
    public Future<CountResult> getSocialRequestCount(User user) {
        return new AsyncResult<CountResult>(CountResult.createWithCount(socialRelationService.getUserRequests(user,
                false).size()));
    }

    /**
     * Get the number of MedControl Cases for a user.
     *
     * @param screenName the screenName of the user
     * @return the count wrapped in a {@link Future}
     */
    @Async
    public Future<CountResult> getMedControlCasesCount(String screenName) {

        List<DeviationCase> deviationCases = null;
        try {
            deviationCases = medControlService.listDeviationCases(screenName, false);
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (deviationCases == null) {
            return new AsyncResult<CountResult>(CountResult.createNullResult());
        }

        return new AsyncResult<CountResult>(CountResult.createWithCount(deviationCases.size()));
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

    /**
     * Get the {@link SocialRequest}s for a user.
     *
     * @param user the regarded user
     * @return a list of {@link SocialRequest}s
     */
    public List<SocialRequest> getSocialRequests(User user) {
        return socialRelationService.getUserRequests(user, true);
    }

    /**
     * Similar to
     * {@link se.vgregion.notifications.service.NotificationService#getSocialRequests(com.liferay.portal.model.User)}
     * but each {@link SocialRequest} is mapped to the requested {@link User} object. The {@link SocialRequest} class
     * only provides access to the userId but this method provides easy access to the whole {@link User} object.
     *
     * @param user the regarded user
     * @return a map of {@link SocialRequest}s mapped to the requested {@link User}
     */
    public Map<SocialRequest, User> getSocialRequestsWithRespectiveUser(User user) {
        return socialRelationService.getUserRequestsWithUser(user, true);
    }

    /**
     * Get the MedControl Cases for a user.
     *
     * @param user the {@link User} instance
     * @return a list of {@link DeviationCase}s
     */
    public List<DeviationCase> getMedControlCases(User user) {
        return medControlService.listDeviationCases(user.getScreenName(), true);
    }

    /**
     * Confirm a {@link SocialRequest}.
     *
     * @param requestId the id of the {@link SocialRequest}
     * @throws NotificationException NotificationException
     */
    public void confirmRequest(Long requestId) throws NotificationException {
        try {
            socialRelationService.confirmRequest(requestId);
        } catch (SystemException e) {
            throw new NotificationException(e);
        } catch (PortalException e) {
            throw new NotificationException(e);
        }
    }

    /**
     * Reject a {@link SocialRequest}.
     *
     * @param requestId the id of the {@link SocialRequest}
     * @throws NotificationException NotificationException
     */
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
