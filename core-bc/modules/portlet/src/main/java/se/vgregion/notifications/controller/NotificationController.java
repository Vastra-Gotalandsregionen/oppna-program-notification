/**
 *
 */
package se.vgregion.notifications.controller;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
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
import se.vgregion.notifications.service.NotificationService;
import se.vgregion.raindancenotifier.domain.InvoiceNotification;
import se.vgregion.usdservice.domain.Issue;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author simongoransson
 */

@Controller
@RequestMapping("VIEW")
@ManagedResource
public class NotificationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);
    private static final int INTERVAL = 10;

    private NotificationService notificationService;
    private final ScheduledExecutorService executorService;// = Executors.newScheduledThreadPool(10, Executors.defaultThreadFactory());   

    @Resource(name = "usersNotificationsCache")
    private Cache cache;
    private final String recentlyCheckedSuffix = "RecentlyChecked";

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;

        // Initialize executorService with a proper thread factory
        final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
        executorService = Executors.newScheduledThreadPool(10, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = defaultThreadFactory.newThread(r);
                thread.setDaemon(true); // We don't want these threads to block a shutdown of the JVM
                return thread;
            }
        });
    }
    
    @PreDestroy
    public void destroy() {
        LOGGER.debug("Shutting down executorService.");
        executorService.shutdown();
    }
    
    @ManagedOperation
    public void resetCache() {
        LOGGER.debug("Resetting cache.");
        cache.removeAll();
    }

    @RenderMapping
    public String viewNotifications(Model model, RenderRequest request) throws ExecutionException,
            InterruptedException {

        final String screenName = getScreenName(request);

        model.addAttribute("interval", INTERVAL * 1000);

        Element element = cache.get(screenName);

        Map<String, Integer> systemNoNotifications;
        if (element != null && element.getValue() != null) {
            systemNoNotifications = (Map<String, Integer>) element.getValue();
        } else {
            executorService.schedule(new CacheUpdater(screenName), 1, TimeUnit.SECONDS);
            return "view";
        }

        Integer alfrescoCount = systemNoNotifications.get("alfrescoCount");
        Integer usdIssuesCount = systemNoNotifications.get("usdIssuesCount");
        Integer randomCount = systemNoNotifications.get("randomCount");
        Integer emailCount = systemNoNotifications.get("emailCount");
        Integer invoicesCount = systemNoNotifications.get("invoicesCount");

        model.addAttribute("alfrescoCount", alfrescoCount);
        model.addAttribute("alfrescoHighlightCount", highlightCount(screenName, "alfrescoCount", alfrescoCount));
        model.addAttribute("usdIssuesCount", usdIssuesCount);
        model.addAttribute("usdIssuesHighlightCount", highlightCount(screenName, "usdIssuesCount", usdIssuesCount));
        model.addAttribute("randomCount", randomCount);
        model.addAttribute("randomHighlightCount", highlightCount(screenName, "randomCount", randomCount));
        model.addAttribute("emailCount", emailCount);
        model.addAttribute("emailHighlightCount", highlightCount(screenName, "emailCount", emailCount));
        model.addAttribute("invoicesCount", invoicesCount);
        model.addAttribute("invoicesHighlightCount", highlightCount(screenName, "invoicesCount", invoicesCount));

        return "view";
    }

    private String getScreenName(PortletRequest request) {
        return ((ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY)).getUser()
                .getScreenName();
    }


    // We highlight the count if there is a value and the user hasn't recently clicked on it.
    private boolean highlightCount(String screenName, String countName, Integer newCount) {
        Element element = cache.get(screenName);

        // If we have no value at the moment we should never display it.
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
                // The user hasn't checked it recently so we should display the count.
                return true;
            } else {
                // The user has recently checked it so we display it depending on whether the value is new.
                return !((Map<String, Integer>) element.getValue()).get(countName).equals(newCount);
            }
        }
    }

    @RenderMapping(params = "action=showExpandedNotifications")
    public String showExpandedNotifications(RenderRequest request, RenderResponse response, Model model) {

        final String screenName = getScreenName(request);

        String notificationType = request.getParameter("notificationType");

        manageRecentlyChecked(screenName, notificationType);

        model.addAttribute("notificationType", upperFirstCase(notificationType));
        if (notificationType.equals("random")) {
            Random r = new Random();
            model.addAttribute("values", Arrays.asList(r.nextInt(), r.nextInt(), r.nextInt(), r.nextInt()));
            return "view_random";
        } else if (notificationType.equals("alfresco")) {
            List<Site> alfrescoSites = notificationService.getAlfrescoDocuments(screenName);
            model.addAttribute("sites", alfrescoSites);
            return "view_alfresco";
        } else if (notificationType.equals("email")) {
            return "view_email";
        } else if (notificationType.equals("usdIssues")) {
            List<Issue> usdIssues = notificationService.getUsdIssues(screenName);
            model.addAttribute("usdIssues", usdIssues);
            return "view_usd_issues";
        } else if (notificationType.equals("invoices")) {
            List<InvoiceNotification> invoices = notificationService.getInvoices(screenName);
            model.addAttribute("invoices", invoices);
            return "view_invoices";
        }

        return "view_notifications";
    }

    private String upperFirstCase(String s) {
        if (s == null || s.length() < 1) {
            return null;
        }
        String firstChar = s.substring(0, 1);
        return s.replaceFirst(firstChar, firstChar.toUpperCase());
    }

    private void manageRecentlyChecked(String screenName, String notificationType) {
        String countName = notificationType + "Count";

        Element element = cache.get(screenName + recentlyCheckedSuffix);

        if (element != null && element.getValue() != null) {
            Set<String> values = (Set<String>) element.getValue();
            values.add(countName);
        } else {
            Set<String> values = new HashSet<String>();
            values.add(countName);
            element = new Element(screenName + recentlyCheckedSuffix, values);
            cache.put(element);
        }
    }
    
    private Integer getValue(Integer value) {
        if (value != null && value > 0) {
            return value;
        } else {
            return null;
        }
    }
    
    private Integer getValue(Future<Integer> count) {
		try {
			Integer value = count.get();
			
			return value;
		} catch (InterruptedException e) {
			return null;
		} catch (ExecutionException e) {
			return null;
		}
    }
    

    private Map<String, Integer> getSystemNoNotifications(String screenName) {
    	
        try {
            Future<Integer> alfrescoCount = notificationService.getAlfrescoCount(screenName);
            Future<Integer> usdIssuesCount = notificationService.getUsdIssuesCount(screenName);
            Future<Integer> randomCount = notificationService.getRandomCount();
            Future<Integer> emailCount = notificationService.getEmailCount(screenName);
            Future<Integer> invoicesCount = notificationService.getInvoicesCount(screenName);
            
            Map<String, Integer> systemNoNotifications = new HashMap<String, Integer>();
            
            systemNoNotifications.put("alfrescoCount", getValue(alfrescoCount));
            systemNoNotifications.put("usdIssuesCount", getValue(usdIssuesCount));
            systemNoNotifications.put("randomCount", getValue(randomCount));
            systemNoNotifications.put("emailCount", getValue(emailCount));
            systemNoNotifications.put("invoicesCount", getValue(invoicesCount));

            return systemNoNotifications;
        }
        catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<String, Integer>();
    }

    @ResourceMapping
    public void pollNotifications(ResourceRequest request, ResourceResponse response) throws IOException {
        final String screenName = ((ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY)).getUser().getScreenName();

        executorService.schedule(new CacheUpdater(screenName), INTERVAL, TimeUnit.SECONDS);

        Map<String, Integer> systemNoNotifications = null;
        Element element = cache.get(screenName);
        if (element != null) {
            systemNoNotifications = (Map<String, Integer>) element.getValue();
        }

        if (systemNoNotifications == null && request.getParameter("onlyCache").equals("false")) {
        	
        	System.out.println("SYSO - NotificationController - pollNotification - will take the long route");
        	
            // If we have nothing in cache and do not require cache we can make the "long" request.
            systemNoNotifications = getSystemNoNotifications(screenName);
        }

        writeJsonObjectToResponse(response, systemNoNotifications);
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
            String userId = getScreenName(request);
            String bopsId = notificationService.getBopsId(userId);
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getPortletOutputStream(), "+BOPSID=" + bopsId);
        } catch (Exception ex) {
            try {
                new ObjectMapper().writeValue(response.getPortletOutputStream(), "");
            } catch (IOException e) {
                // Really wrong writing response
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

    private class CacheUpdater implements Runnable {

        private String screenName;

        private CacheUpdater(String screenName) {
            this.screenName = screenName;
        }

        @Override
        public void run() {
            Map<String, Integer> systemNoNotifications = getSystemNoNotifications(screenName);

            // Compare the new values with the old to see if any value is updated. If so, it should not be considered
            // recently checked.
            Element recentlyCheckedSet = cache.get(screenName + recentlyCheckedSuffix);
            if (recentlyCheckedSet != null) {
            	
                // If recentlyCheckedSet is null we don't need to do this at all since there will be nothing to remove.
                Element element = cache.get(screenName);
                if (element != null && element.getValue() != null) {
                	
                    Map<String, Integer> cachedValues = (Map<String, Integer>) element.getValue();
                    for (Map.Entry<String, Integer> countNameValue : cachedValues.entrySet()) {
                        // Is there a recent check for this key?
                        String counterName = countNameValue.getKey();
                        if (recentlyCheckedSet != null && recentlyCheckedSet.getValue() != null) {
                            if (((Set) recentlyCheckedSet.getValue()).contains(counterName)) {
                                // If it was recently checked, we compare the new value with the old. If they differ we remove
                                // the recent check.
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

            cache.put(new Element(screenName, systemNoNotifications));
        }
    }


}
