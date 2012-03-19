/**
 *
 */
package se.vgregion.notifications.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import javax.annotation.Resource;
import javax.portlet.RenderRequest;
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
    private Cache ehCache;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RenderMapping
    public String viewNotifications(Model model, RenderRequest request) throws ExecutionException,
            InterruptedException {

        final String screenName = ((ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY)).getUser().getScreenName();

        Map<String, Integer> systemNoNotifications = getSystemNoNotifications(screenName);

        model.addAttribute("alfrescoCount", systemNoNotifications.get("alfrescoCount"));
        model.addAttribute("usdIssuesCount", systemNoNotifications.get("usdIssuesCount"));
        model.addAttribute("randomCount", systemNoNotifications.get("randomCount"));
        model.addAttribute("emailCount", systemNoNotifications.get("emailCount"));
        model.addAttribute("invoicesCount", systemNoNotifications.get("invoicesCount"));

        model.addAttribute("interval", INTERVAL * 1000);

        ehCache.put(new Element(screenName, systemNoNotifications));

        executorService.schedule(new CacheUpdater(screenName), INTERVAL, TimeUnit.SECONDS);

        return "view";
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

        Map<String, Integer> systemNoNotifications = (Map<String, Integer>) ehCache.get(screenName).getValue();//todo om det inte finns...

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
        final String screenName = ((ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY)).getUser().getScreenName();

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
            ehCache.put(new Element(screenName, systemNoNotifications));
        }
    }


}
