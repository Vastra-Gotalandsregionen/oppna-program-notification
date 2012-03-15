/**
 * 
 */
package se.vgregion.notifications.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.RenderRequest;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import se.vgregion.alfrescoclient.domain.Document;

import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusException;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import se.vgregion.usdservice.domain.Issue;

/**
 * @author simongoransson
 * 
 */

@Controller
@Scope("session")
@RequestMapping("VIEW")
public class NotificationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);

    @RenderMapping
    public String viewNotifications(Model model, RenderRequest request) {

        try {
            String screenName = ((ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY)).getUser().getScreenName();
            
            int n = getNumberNewAlfresco(screenName);

            model.addAttribute("numberNewAlfresco", n);

            n = getUsdIssues(screenName);

            model.addAttribute("numberUsdIssues", n);

        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "view";
    }

    private int getNumberNewAlfresco(String screenName) throws IOException {
        String alfrescoResponse = getFromMessageBus("vgr/alfresco_recently_modified_json", screenName);


        ObjectMapper mapper = new ObjectMapper();

        ObjectReader reader = mapper.reader(Map.class);

        Map<String, List<Document>> alfrescoMap = reader.readValue(alfrescoResponse);

        int n = 0;
        for (Entry<String, List<Document>> entry : alfrescoMap.entrySet()) {
            n = n + entry.getValue().size();
        }
        return n;
    }

    private int getUsdIssues(String screenName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String usdResponse = getFromMessageBus("vgr/usd_issues_json", screenName);
        ObjectReader usdReader = mapper.reader(List.class);
        List<Issue> issues = usdReader.readValue(usdResponse);
        return issues.size();
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


}
