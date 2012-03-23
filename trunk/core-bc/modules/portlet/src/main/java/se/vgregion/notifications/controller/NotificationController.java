/**
 *
 */
package se.vgregion.notifications.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

import javax.annotation.Resource;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import se.vgregion.alfrescoclient.domain.Document;
import se.vgregion.notifications.service.NotificationService;

/**
 * @author simongoransson
 */

@Controller
@RequestMapping("VIEW")
public class NotificationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);
    private static final int INTERVAL = 10;

    private NotificationService notificationService;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(20);

    @Resource(name = "usersNotificationsCache")
    private Cache cache;
    private final String recentlyCheckedSuffix = "RecentlyChecked";

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RenderMapping
    public String viewNotifications(Model model, RenderRequest request) throws ExecutionException,
            InterruptedException {

        final String screenName = getScreenName(request);

        model.addAttribute("interval", INTERVAL * 1000);

        /*Element element = cache.get(screenName);
        
        Map<String, Integer> previousValues = new HashMap<String, Integer>()
        if (element == null || element.getValue() == null) {
            // Just instantiate an empty map
            previousValues = new HashMap<String, Integer>();
        }*/
        Element element = cache.get(screenName);

        Map<String, Integer> systemNoNotifications;
        if (element != null && element.getValue() != null) {
            systemNoNotifications = (Map<String, Integer>) element.getValue();
        } else {
//            systemNoNotifications = getSystemNoNotifications(screenName);
//            cache.put(new Element(screenName, systemNoNotifications));
            executorService.schedule(new CacheUpdater(screenName), 1, TimeUnit.SECONDS);
            return "view";
        }

        Integer alfrescoCount = systemNoNotifications.get("alfrescoCount");
        Integer usdIssuesCount = systemNoNotifications.get("usdIssuesCount");
        Integer randomCount = systemNoNotifications.get("randomCount");
        Integer emailCount = systemNoNotifications.get("emailCount");
        Integer invoicesCount = systemNoNotifications.get("invoicesCount");

        model.addAttribute("alfrescoCount", alfrescoCount);
        model.addAttribute("alfrescoDisplayCount", displayCount(screenName, "alfrescoCount", alfrescoCount));
        model.addAttribute("usdIssuesCount", usdIssuesCount);
        model.addAttribute("usdIssuesDisplayCount", displayCount(screenName, "usdIssuesCount", usdIssuesCount));
        model.addAttribute("randomCount", randomCount);
        model.addAttribute("randomDisplayCount", displayCount(screenName, "randomCount", randomCount));
        model.addAttribute("emailCount", emailCount);
        model.addAttribute("emailDisplayCount", displayCount(screenName, "emailCount", emailCount));
        model.addAttribute("invoicesCount", invoicesCount);
        model.addAttribute("invoicesDisplayCount", displayCount(screenName, "invoicesCount", invoicesCount));

        return "view";
    }

    private String getScreenName(RenderRequest request) {
        return ((ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY)).getUser()
                .getScreenName();
    }


    // Tests whether the new value already is stored in cache. If no value is stored in cache the value is new.
    private boolean displayCount(String screenName, String countName, Integer newCount) {
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

    private Map<String, Integer> getSystemNoNotifications(String screenName) {
        try {
            Future<Integer> alfrescoCount = notificationService.getAlfrescoCount(screenName);
            Future<Integer> usdIssuesCount = notificationService.getUsdIssuesCount(screenName);
            Future<Integer> randomCount = notificationService.getRandomCount();
            Future<Integer> emailCount = notificationService.getEmailCount(screenName);
            Future<Integer> invoicesCount = notificationService.getInvoicesCount(screenName);

            Map<String, Integer> systemNoNotifications = new HashMap<String, Integer>();

            systemNoNotifications.put("alfrescoCount", getValue(alfrescoCount.get()));
            systemNoNotifications.put("usdIssuesCount", getValue(usdIssuesCount.get()));
            systemNoNotifications.put("randomCount", getValue(randomCount.get()));
            systemNoNotifications.put("emailCount", getValue(emailCount.get()));
            systemNoNotifications.put("invoicesCount", getValue(invoicesCount.get()));

            return systemNoNotifications;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<String, Integer>();
    }

    @ResourceMapping
    public void pollNotifications(ResourceRequest request, ResourceResponse response) throws IOException {
        final String screenName = ((ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY)).getUser().getScreenName();

        executorService.schedule(new CacheUpdater(screenName), INTERVAL, TimeUnit.SECONDS);

        Map<String, Integer> systemNoNotifications = (Map<String, Integer>) cache.get(screenName).getValue();//todo om det inte finns...

        writeJsonObjectToResponse(response, systemNoNotifications);
    }

    private void writeJsonObjectToResponse(ResourceResponse response, Object object) throws IOException {
        PrintWriter writer = null;
        try {
            response.setContentType("application/json");

            writer = response.getWriter();

            new ObjectMapper().writeValue(writer, object);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    @ResourceMapping(value = "alfrescoResource")
    public void getAlfrescoDocuments(ResourceRequest request, ResourceResponse response) throws IOException {
        final String screenName = ((ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY)).getUser()
                .getScreenName();

        List<Document> alfrescoDocuments = notificationService.getAlfrescoDocuments(screenName);

        writeJsonObjectToResponse(response, alfrescoDocuments);
    }

    private String getFromMessageBus(String dest, String userId) {
        String msg;
        Message message = new Message();
        message.setPayload(userId == null ? "" : userId);

        Object response;
        try {
            LOGGER.info("message send");
            response = MessageBusUtil.sendSynchronousMessage(dest, message, 10000);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            response = "-";
        }

        if (response instanceof String) {
            msg = response.toString();
        } else {
            if (response instanceof Exception) {
                Exception e = ((Exception) response);
                LOGGER.error(e.getMessage(), e);
            }
            msg = "-";
        }
        return msg;
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
