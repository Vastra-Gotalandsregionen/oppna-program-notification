/**
 *
 */
package se.vgregion.notifications.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.*;

import javax.annotation.Resource;
import javax.portlet.RenderRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.hibernate.EhCache;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import se.vgregion.alfrescoclient.domain.Document;

import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusException;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import se.vgregion.notifications.service.NotificationService;
import se.vgregion.usdservice.domain.Issue;

/**
 * @author simongoransson
 */

@Controller
//@Scope("session")
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

        model.addAttribute("numberNewAlfresco", systemNoNotifications.get("numberNewAlfresco"));
        model.addAttribute("numberUsdIssues", systemNoNotifications.get("numberUsdIssues"));
        model.addAttribute("slowRandom", systemNoNotifications.get("slowRandom"));

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
            Future<Integer> numberNewAlfresco = notificationService.getNumberNewAlfresco(screenName);
            Future<Integer> numberUsd = notificationService.getUsdIssues(screenName);
            Future<Integer> slowRandom = notificationService.getSlowRandom();

            Map<String, Integer> systemNoNotifications = new HashMap<String, Integer>();

            systemNoNotifications.put("numberNewAlfresco", getValue(numberNewAlfresco.get()));
            systemNoNotifications.put("numberUsdIssues", getValue(numberUsd.get()));
            systemNoNotifications.put("slowRandom", getValue(slowRandom.get()));

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

        PrintWriter writer = null;
        try {
            response.setContentType("application/json");

            writer = response.getWriter();

            new ObjectMapper().writeValue(writer, systemNoNotifications);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

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
