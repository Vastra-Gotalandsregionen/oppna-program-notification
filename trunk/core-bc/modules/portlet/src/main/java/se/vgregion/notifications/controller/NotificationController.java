/**
 *
 */
package se.vgregion.notifications.controller;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.social.model.SocialRequest;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import se.vgregion.alfrescoclient.domain.Site;
import se.vgregion.notifications.NotificationException;
import se.vgregion.notifications.domain.CountResult;
import se.vgregion.notifications.domain.NotificationServiceName;
import se.vgregion.notifications.service.NotificationCallManager;
import se.vgregion.notifications.service.NotificationService;
import se.vgregion.portal.medcontrol.domain.DeviationCase;
import se.vgregion.raindancenotifier.domain.InvoiceNotification;
import se.vgregion.usdservice.domain.Issue;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.portlet.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Controller class for managing views and cache related to the notifications GUI.
 *
 * @author simongoransson
 * @author Patrik Bergström
 */

@Controller
@RequestMapping("VIEW")
@ManagedResource
@SuppressWarnings("unchecked")
public class NotificationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);
    private static final int INTERVAL = 30; // Seconds

    private NotificationService notificationService;
    private final ScheduledExecutorService executorService;
    private NotificationCallManager notificationCallManager;

    @Resource(name = "usersNotificationsCache")
    protected Cache cache; // Protected access to make access from CacheUpdater more efficient (see Findbugs)
    protected final Set<String> currentlyScheduledUpdates = Collections.synchronizedSet(new HashSet<String>());
    private final String recentlyCheckedSuffix = "RecentlyChecked";

    private List<String> notificationCountServices = NotificationServiceName.allNamesAsList();

    @Resource
    private List<String> exceptedUsers = new ArrayList<String>();

    @Value("${iNotesUrl}")
    private String iNotesUrl;

    /**
     * Constructor.
     *
     * @param notificationService     the {@link NotificationService}
     * @param notificationCallManager the {@link NotificationCallManager}
     */
    @Autowired
    public NotificationController(NotificationService notificationService,
                                  NotificationCallManager notificationCallManager) {
        this.notificationService = notificationService;
        this.notificationCallManager = notificationCallManager;

        // Initialize executorService with a proper thread factory
        final int poolSize = 20;
        executorService = Executors.newScheduledThreadPool(poolSize, new DaemonThreadFactory());
    }

    /**
     * When the controller is destroyed. It shuts down the executorService.
     */
    @PreDestroy
    public void destroy() {
        LOGGER.debug("Shutting down executorService.");
        executorService.shutdown();
    }

    /**
     * Method which is accessible by a JMX agent like e.g. jconsole. It resets the cache.
     */
    @ManagedOperation
    public void resetCache() {
        LOGGER.debug("Resetting cache.");
        cache.removeAll();
    }

    @ManagedOperation
    public Set<String> getCurrentlyScheduledUpdates() {
        return currentlyScheduledUpdates;
    }

    /**
     * The method which as accessed as the browser refreshes the page. This method will never make a long synchronous
     * call to fetch the count values. Instead it uses the cache and if there is nothing cached it will schedule a cache
     * update to execute directly.
     *
     * @param model   model
     * @param request request
     * @return a view
     */
    @RenderMapping
    public String viewNotifications(Model model, RenderRequest request) {

        final User user = getUser(request);

        final int millisInSecond = 1000;
        model.addAttribute("interval", INTERVAL * millisInSecond);

        if (exceptedUsers.contains(user.getScreenName())) {
            return "view";
        }

        Element element = cache.get(user.getScreenName());

        Map<String, CountResult> systemNoNotifications;
        if (element != null && element.getValue() != null) {
            systemNoNotifications = (Map<String, CountResult>) element.getValue();
        } else {
            final int delay = 10;
            scheduleCacheUpdate(user, delay);
            return "view";
        }

        // Do this synchronously due to a problem with liferay's services when using separate threads
        CountResult socialRequestCount = getValue(notificationService.getSocialRequestCount(user),
                "socialRequestCount");

        for (String name : notificationCountServices) {
            // E.g. "alfrescoCount" and "3"
            String namePlusCount = name + "Count";
            CountResult value = systemNoNotifications.get(namePlusCount);

            if (value == null) {
                value = CountResult.createWithCount(0);
            }

            model.addAttribute(namePlusCount, value);
            model.addAttribute(name + "HighlightCount", highlightCount(user.getScreenName(), namePlusCount,
                    value.getCount()));
        }
        model.addAttribute("socialRequestCount", socialRequestCount);
        model.addAttribute("socialRequestHighlightCount", highlightCount(user.getScreenName(), "socialRequestCount",
                socialRequestCount.getCount()));

        return "view";
    }

    private void scheduleCacheUpdate(User user, int delay) {
        if (exceptedUsers.contains(user.getScreenName())) {
            return;
        }

        if (!currentlyScheduledUpdates.contains(user.getScreenName())) {
            executorService.schedule(new CacheUpdater(user), delay, TimeUnit.MILLISECONDS);
            currentlyScheduledUpdates.add(user.getScreenName());
        } else {
            LOGGER.warn("Skipped cache update since user has ongoing update.");
        }
    }

    private User getUser(PortletRequest request) {
        return ((ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY)).getUser();
    }

    // We highlight the count if there is a value and the user hasn't recently clicked on it.
    private boolean highlightCount(String screenName, String countName, Integer newCount) {
        Element element = cache.get(screenName);

        // If we have no value at the moment we should never highlight it.
        if (newCount == null || newCount == 0) {
            return false;
        }

        // If any of these are null the value is not stored in cache.
        if (element == null || element.getValue() == null || ((Map<String, Integer>) element.getValue())
                .get(countName) == null) {
            return true;
        } else {
            // First check whether the user have checked this recently.
            Element checked = cache.get(screenName + recentlyCheckedSuffix);
            if (checked == null || checked.getValue() == null || !((Set<String>) checked.getValue())
                    .contains(countName)) {
                // The user hasn't checked it recently so we should highlight the count.
                return true;
            } else {
                // The user has recently checked it so we highlight it depending on whether the value is new.
                return !((Map<String, CountResult>) element.getValue()).get(countName).getCount().equals(newCount);
            }
        }
    }

    /**
     * This method is called when a detailed view of respective notification type is requested. It checks which type of
     * notification which is wanted and places content into the model.
     *
     * @param request  request
     * @param response response
     * @param model    model
     * @return a view
     */
    @RenderMapping(params = "action=showExpandedNotifications")
    public String showExpandedNotifications(RenderRequest request, RenderResponse response, Model model) {

        final User user = getUser(request);

        String notificationType = request.getParameter("notificationType");

        manageRecentlyChecked(user.getScreenName(), notificationType);

        model.addAttribute("notificationType", upperFirstCase(notificationType));
        if (notificationType.equals("random")) {
            Random r = new Random();
            model.addAttribute("values", Arrays.asList(r.nextInt(), r.nextInt(), r.nextInt(), r.nextInt()));
            return "view_random";
        } else if (notificationType.equals("alfresco")) {
            List<Site> alfrescoSites = notificationService.getAlfrescoDocuments(user.getScreenName());
            model.addAttribute("sites", alfrescoSites);
            return "view_alfresco";
        } else if (notificationType.equals("email")) {
            String defaultMessage = "<p>Gå till <a href=\"" + iNotesUrl + "\">iNotes</a>.</p>";
            String message = getMessageIfThereIsAny(user, notificationType);
            model.addAttribute("message", message != null ? message : defaultMessage);
            return "view_email";
        } else if (notificationType.equals("usdIssues")) {
            List<Issue> usdIssues = notificationService.getUsdIssues(user.getScreenName());
            List<Issue> myUsdIssues = new ArrayList<Issue>();
            List<Issue> groupUsdIssues = new ArrayList<Issue>();
            for (Issue usdIssue : usdIssues) {
                if ("A".equals(usdIssue.getAssociated()) || "U".equals(usdIssue.getAssociated())) {
                    myUsdIssues.add(usdIssue);
                } else if ("G".equals(usdIssue.getAssociated())) {
                    groupUsdIssues.add(usdIssue);
                }
            }
            model.addAttribute("myUsdIssues", myUsdIssues);
            model.addAttribute("groupUsdIssues", groupUsdIssues);
            return "view_usd_issues";
        } else if (notificationType.equals("invoices")) {
            List<InvoiceNotification> invoices = notificationService.getInvoices(user.getScreenName());
            model.addAttribute("invoices", invoices);
            return "view_invoices";
        } else if (notificationType.equals("medControl")) {
            List<DeviationCase> deviationCases = notificationService.getMedControlCases(user);
            model.addAttribute("deviationCases", deviationCases);
            return "view_med_control";
        } else if (notificationType.equals("socialRequests")) {
            Map<SocialRequest, User> socialRequests = notificationService.getSocialRequestsWithRespectiveUser(user);
            model.addAttribute("socialRequests", socialRequests);
            return "view_social_requests";
        } else {
            throw new IllegalArgumentException("NotificationType [" + notificationType + "] is unknown.");
        }
    }

    private String getMessageIfThereIsAny(User user, String notificationType) {
        String message = null;
        Map<String, CountResult> systemNoNotificationsFromCache = getSystemNoNotificationsFromCache(user);
        if (systemNoNotificationsFromCache != null) {
            CountResult countResult = systemNoNotificationsFromCache.get(notificationType + "Count");
            if (countResult != null) {
                message = countResult.getMessage();
            }
        }
        return message;
    }

    private String upperFirstCase(String s) {
        if (s == null || s.length() < 1) {
            return null;
        }
        String firstChar = s.substring(0, 1);
        return s.replaceFirst(firstChar, firstChar.toUpperCase(Locale.getDefault()));
    }

    private void manageRecentlyChecked(String screenName, String notificationType) {
        String countName = notificationType + "Count";

        Element element = cache.get(screenName + recentlyCheckedSuffix);

        if (element != null && element.getValue() != null) {
            // Add to existing set
            Set<String> values = (Set<String>) element.getValue();
            values.add(countName);
        } else {
            // Create new set and add to ditto
            Set<String> values = new HashSet<String>();
            values.add(countName);
            element = new Element(screenName + recentlyCheckedSuffix, values);
            cache.put(element);
        }
    }

    private CountResult getValue(Future<CountResult> count, String countName) {
        try {
            final int timeout = 10;
            CountResult value = count.get(timeout, TimeUnit.SECONDS);

            return value;
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            return CountResult.createNullResult();
        } catch (ExecutionException e) {
            LOGGER.error(countName + ": " + e.getMessage(), e);
            return CountResult.createNullResult();
        } catch (TimeoutException e) {
            LOGGER.error(countName + ": " + e.getMessage(), e);
            return CountResult.createNullResult();
        }
    }

    private Map<String, CountResult> getSystemNoNotifications(User user) {

        Map<String, Future<CountResult>> serviceNameWithCount = new HashMap<String, Future<CountResult>>();

        // Get counts for all services asynchronously, i.e. just start threads to start retrieving the counts.
        for (String serviceName : notificationCountServices) {
            if (notificationCallManager.shouldICallThisService(serviceName, user.getScreenName())) {
                try {
                    serviceNameWithCount.put(serviceName, notificationService.getCount(serviceName, user));
                } catch (NotificationException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        Map<String, CountResult> systemNoNotifications = new HashMap<String, CountResult>();

        for (String name : notificationCountServices) {
            String countName = name + "Count";
            Future<CountResult> countResultFuture = serviceNameWithCount.get(name);
            if (countResultFuture == null) {
                // We didn't call the given service
                systemNoNotifications.put(countName, null);
            } else {
                CountResult value = getValue(countResultFuture, countName);
                notificationCallManager.notifyValue(name, value, user.getScreenName());
                systemNoNotifications.put(countName, value == null ? CountResult.createWithCount(0) : value);
            }
        }

        return systemNoNotifications;
    }

    /**
     * This method is called when the client makes an ajax request to poll for notifications. The method gets the
     * notification counts only from cache to provide fast responses. When a poll request is made for a given screen
     * name a cache update is scheduled with the given interval. This means that the
     *
     * @param request  request
     * @param response response
     * @throws IOException IOException
     */
    @ResourceMapping
    public void pollNotifications(ResourceRequest request, ResourceResponse response) throws IOException {
        final User user = ((ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY)).getUser();

        if (exceptedUsers.contains(user.getScreenName())) {
            return;
        }

        scheduleCacheUpdate(user, INTERVAL);

        Map<String, CountResult> systemNoNotifications = getSystemNoNotificationsFromCache(user);

        if (systemNoNotifications == null && request.getParameter("onlyCache").equals("false")) {
            // If we have nothing in cache and do not require cache we can make the "long" request.
            systemNoNotifications = getSystemNoNotifications(user);
            storeInCache(user, systemNoNotifications);
        }

        // Do this specific request synchronously for now due to non-deterministic results otherwise (Liferay returns
        // a cached result so it's rather efficient.
        if (systemNoNotifications != null) {
            CountResult socialRequestCount = getValue(notificationService.getSocialRequestCount(user),
                    "socialRequestCount");
            systemNoNotifications.put("socialRequestCount", socialRequestCount);
        }

        writeJsonObjectToResponse(response, systemNoNotifications);

        response.addProperty("Cache-control", "no-cache");
    }

    private Map<String, CountResult> getSystemNoNotificationsFromCache(User user) {
        Map<String, CountResult> systemNoNotifications = null;
        Element element = cache.get(user.getScreenName());
        if (element != null) {
            systemNoNotifications = (Map<String, CountResult>) element.getValue();
        }
        return systemNoNotifications;
    }

    /**
     * Ajax call method returning shorttime USD Single Sign On key.
     *
     * @param request  - ajax request.
     * @param response - ajax response as json.
     */
    @ResourceMapping(value = "lookupBopsId")
    public void lookupBopsId(ResourceRequest request, ResourceResponse response) {
        try {
            String userId = getUser(request).getScreenName();
            String bopsId = notificationService.getBopsId(userId);
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getPortletOutputStream(), "+BOPSID=" + bopsId);
        } catch (RuntimeException ex) {
            try {
                new ObjectMapper().writeValue(response.getPortletOutputStream(), "");
                LOGGER.error(ex.getMessage(), ex);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (IOException ex) {
            try {
                new ObjectMapper().writeValue(response.getPortletOutputStream(), "");
                LOGGER.error(ex.getMessage(), ex);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Method to confirm a friend request.
     *
     * @param request  request
     * @param response response
     * @throws NotificationException NotificationException
     * @throws IOException           IOException
     */
    @ResourceMapping(value = "confirmRequest")
    public void confirmRequest(ResourceRequest request, ResourceResponse response) throws NotificationException,
            IOException {
        Long requestId = Long.valueOf(request.getParameter("requestId"));
        notificationService.confirmRequest(requestId);
        writeMessageToResponse(response, "Du har accepterat denna förfrågan.");
    }

    /**
     * Method to reject a friend request.
     *
     * @param request  request
     * @param response response
     * @throws NotificationException NotificationException
     * @throws IOException           IOException
     */
    @ResourceMapping(value = "rejectRequest")
    public void rejectRequest(ResourceRequest request, ResourceResponse response) throws NotificationException,
            IOException {
        Long requestId = Long.valueOf(request.getParameter("requestId"));
        notificationService.rejectRequest(requestId);
        writeMessageToResponse(response, "Du har ignorerat denna förfrågan.");
    }

    private void writeMessageToResponse(ResourceResponse response, String message) throws IOException {
        OutputStream output = null;
        try {
            output = response.getPortletOutputStream(); // Use outputStream so we can decide encoding.
            output.write(message.getBytes("UTF-8"));
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private void writeJsonObjectToResponse(ResourceResponse response, Object object) throws IOException {

        PrintWriter writer = null;
        try {
            response.setContentType("application/json");

            writer = response.getWriter();
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.writeValue(writer, object);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = defaultThreadFactory.newThread(r);
            thread.setName("notificationCacheUpdater-" + thread.getName());
            thread.setDaemon(true); // We don't want these threads to block a shutdown of the JVM
            return thread;
        }
    }

    class CacheUpdater implements Runnable {

        private User user;

        CacheUpdater(User user) {
            this.user = user;
        }

        @Override
        public void run() {
            try {
                Map<String, CountResult> systemNoNotifications = getSystemNoNotifications(user);
                // Compare the new values with the old to see if any value is updated. If so, it should not be
                // considered recently checked.
                Element recentlyCheckedSet = cache.get(user.getScreenName() + recentlyCheckedSuffix);
                if (recentlyCheckedSet != null) {

                    // If recentlyCheckedSet is null we don't need to do this at all since there will be nothing to
                    // remove.
                    Element element = cache.get(user.getScreenName());
                    if (element != null && element.getValue() != null) {

                        Map<String, CountResult> cachedValues = (Map<String, CountResult>) element.getValue();
                        for (Map.Entry<String, CountResult> countNameValue : cachedValues.entrySet()) {
                            // Is there a recent check for this key?
                            String counterName = countNameValue.getKey();
                            if (recentlyCheckedSet.getValue() != null) {
                                if (((Set) recentlyCheckedSet.getValue()).contains(counterName)) {
                                    // If it was recently checked, we compare the new value with the old. If they differ
                                    // we remove the recent check.
                                    CountResult oldValue = cachedValues.get(counterName);
                                    CountResult newValue = systemNoNotifications.get(counterName);

                                    boolean isSame = isSame(oldValue, newValue);

                                    if (!isSame) {
                                        ((Set) recentlyCheckedSet.getValue()).remove(counterName);
                                    }
                                }
                            }
                        }
                    }
                }

                storeInCache(user, systemNoNotifications);
            } catch (RuntimeException e) {
                LOGGER.error(e.getMessage(), e);
                throw e;
            } finally {
                // Finished the cache update so remove it from currentlyScheduledUpdates
                currentlyScheduledUpdates.remove(user.getScreenName());
            }
        }
    }

    protected void storeInCache(User user, Map<String, CountResult> systemNoNotifications) {
        cache.put(new Element(user.getScreenName(), systemNoNotifications));
    }

    protected boolean isSame(CountResult oldValue, CountResult newValue) {
        if (oldValue == null && newValue == null) {
            return true;
        }
        if ((oldValue == null && newValue != null) || (oldValue != null && newValue == null)) {
            return false;
        }
        // If we get here no-one is null.
        if (oldValue.getCount() == null && newValue.getCount() == null) {
            return true;
        }
        if ((oldValue.getCount() == null && newValue.getCount() != null) ||
                (oldValue.getCount() != null && newValue.getCount() == null)) {
            return false;
        }
        // If we get here no count is null.
        if (oldValue.getCount().equals(newValue.getCount())) {
            return true;
        } else {
            return false;
        }
    }
}
