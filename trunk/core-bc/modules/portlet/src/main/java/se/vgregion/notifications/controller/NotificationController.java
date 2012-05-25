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
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import se.vgregion.alfrescoclient.domain.Site;
import se.vgregion.notifications.NotificationException;
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
public class NotificationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);
    private static final int INTERVAL = 30; // Seconds

    private NotificationService notificationService;
    private final ScheduledExecutorService executorService;

    @Resource(name = "usersNotificationsCache")
    protected Cache cache; // Protected access to make access from CacheUpdater more efficient (see Findbugs)
    protected final Set<String> currentlyScheduledUpdates = Collections.synchronizedSet(new HashSet<String>());
    private final String recentlyCheckedSuffix = "RecentlyChecked";

    @Resource
    private List<String> exceptedUsers = new ArrayList<String>();

    /**
     * Constructor.
     *
     * @param notificationService the {@link NotificationService}
     */
    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;

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

        Map<String, Integer> systemNoNotifications;
        if (element != null && element.getValue() != null) {
            systemNoNotifications = (Map<String, Integer>) element.getValue();
        } else {
            final int delay = 10;
            scheduleCacheUpdate(user, delay);
            return "view";
        }

        Integer alfrescoCount = systemNoNotifications.get("alfrescoCount");
        Integer usdIssuesCount = systemNoNotifications.get("usdIssuesCount");
        Integer emailCount = systemNoNotifications.get("emailCount");
        Integer invoicesCount = systemNoNotifications.get("invoicesCount");
        Integer medControlCount = systemNoNotifications.get("medControlCount");
        // Do this synchronously due to a problem with liferay's services when using separate threads
        Integer socialRequestCount = getValue(notificationService.getSocialRequestCount(user));


        model.addAttribute("alfrescoCount", alfrescoCount);
        model.addAttribute("alfrescoHighlightCount", highlightCount(user.getScreenName(), "alfrescoCount",
                alfrescoCount));
        model.addAttribute("usdIssuesCount", usdIssuesCount);
        model.addAttribute("usdIssuesHighlightCount", highlightCount(user.getScreenName(), "usdIssuesCount",
                usdIssuesCount));
        model.addAttribute("emailCount", emailCount);
        model.addAttribute("emailHighlightCount", highlightCount(user.getScreenName(), "emailCount", emailCount));
        model.addAttribute("invoicesCount", invoicesCount);
        model.addAttribute("invoicesHighlightCount", highlightCount(user.getScreenName(), "invoicesCount",
                invoicesCount));
        model.addAttribute("medControlCount", medControlCount);
        model.addAttribute("medControlHighlightCount", highlightCount(user.getScreenName(), "medControlCount",
                medControlCount));
        model.addAttribute("socialRequestCount", socialRequestCount);
        model.addAttribute("socialRequestHighlightCount", highlightCount(user.getScreenName(), "socialRequestCount",
                socialRequestCount));

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
            LOGGER.warn("Skipped cache update since user has ongoing update. This is a sign that the update takes "
                    + "time.");
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
                return !((Map<String, Integer>) element.getValue()).get(countName).equals(newCount);
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
            return "view_email";
        } else if (notificationType.equals("usdIssues")) {
            List<Issue> usdIssues = notificationService.getUsdIssues(user.getScreenName());
            List<Issue> myUsdIssues = new ArrayList<Issue>();
            List<Issue> groupUsdIssues = new ArrayList<Issue>();
            for (Issue usdIssue : usdIssues) {
                if ("A".equals(usdIssue.getAssociated())) {
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

    private Integer getValue(Future<Integer> count) {
        try {
            final int timeout = 5;
            Integer value = count.get(timeout, TimeUnit.SECONDS);

            return value;
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        } catch (TimeoutException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private Map<String, Integer> getSystemNoNotifications(User user) {

        Future<Integer> alfrescoCount = notificationService.getAlfrescoCount(user.getScreenName());
        Future<Integer> usdIssuesCount = notificationService.getUsdIssuesCount(user.getScreenName());
        Future<Integer> emailCount = notificationService.getEmailCount(user.getScreenName());
        Future<Integer> invoicesCount = notificationService.getInvoicesCount(user.getScreenName());
        Future<Integer> medControlCount = notificationService.getMedControlCasesCount(user.getScreenName());
        // Should getSocialRequestCount here if we can work around the problem or the problem has been solved

        Map<String, Integer> systemNoNotifications = new HashMap<String, Integer>();

        systemNoNotifications.put("alfrescoCount", getValue(alfrescoCount));
        systemNoNotifications.put("usdIssuesCount", getValue(usdIssuesCount));
        systemNoNotifications.put("emailCount", getValue(emailCount));
        systemNoNotifications.put("invoicesCount", getValue(invoicesCount));
        systemNoNotifications.put("medControlCount", getValue(medControlCount));
//        systemNoNotifications.put("socialRequestCount", getValue(socialRequestCount));

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

        scheduleCacheUpdate(user, INTERVAL);

        Map<String, Integer> systemNoNotifications = null;
        Element element = cache.get(user.getScreenName());
        if (element != null) {
            systemNoNotifications = (Map<String, Integer>) element.getValue();
        }

        if (systemNoNotifications == null && request.getParameter("onlyCache").equals("false")) {
            // If we have nothing in cache and do not require cache we can make the "long" request.
            systemNoNotifications = getSystemNoNotifications(user);
        }

        // Do this specific request synchronously for now due to non-deterministic results otherwise (Liferay returns
        // a cached result so it's rather efficient.
        if (systemNoNotifications != null) {
            Integer socialRequestCount = getValue(notificationService.getSocialRequestCount(user));
            systemNoNotifications.put("socialRequestCount", socialRequestCount);
        }

        writeJsonObjectToResponse(response, systemNoNotifications);

        response.addProperty("Cache-control", "no-cache");
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
     * @param request request
     * @param response response
     * @throws NotificationException NotificationException
     * @throws IOException IOException
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
            Map<String, Integer> systemNoNotifications = getSystemNoNotifications(user);

            // Compare the new values with the old to see if any value is updated. If so, it should not be considered
            // recently checked.
            Element recentlyCheckedSet = cache.get(user.getScreenName() + recentlyCheckedSuffix);
            if (recentlyCheckedSet != null) {

                // If recentlyCheckedSet is null we don't need to do this at all since there will be nothing to remove.
                Element element = cache.get(user.getScreenName());
                if (element != null && element.getValue() != null) {

                    Map<String, Integer> cachedValues = (Map<String, Integer>) element.getValue();
                    for (Map.Entry<String, Integer> countNameValue : cachedValues.entrySet()) {
                        // Is there a recent check for this key?
                        String counterName = countNameValue.getKey();
                        if (recentlyCheckedSet.getValue() != null) {
                            if (((Set) recentlyCheckedSet.getValue()).contains(counterName)) {
                                // If it was recently checked, we compare the new value with the old. If they differ we
                                // remove the recent check.
                                Integer oldValue = cachedValues.get(counterName);
                                Integer newValue = systemNoNotifications.get(counterName);
                                if (!oldValue.equals(newValue)) {
                                    ((Set) recentlyCheckedSet.getValue()).remove(counterName);
                                }
                            }
                        }
                    }
                }
            }

            cache.put(new Element(user.getScreenName(), systemNoNotifications));

            // Finished the cache update so remove it from currentlyScheduledUpdates
            currentlyScheduledUpdates.remove(user.getScreenName());
        }
    }


}
