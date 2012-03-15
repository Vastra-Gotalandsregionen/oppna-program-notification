/**
 * 
 */
package se.vgregion.notifications.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.RenderRequest;

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

        Message message = new Message();

        try {
            String alfrescoResponse =
                    (String) MessageBusUtil.sendSynchronousMessage("vgr/alfresco_recently_modified_json",
                            message);

            String usdResponse =
                    (String) MessageBusUtil.sendSynchronousMessage("vgr/usd_issues_json", message);

            ObjectMapper mapper = new ObjectMapper();

            ObjectReader reader = mapper.reader(Map.class);

            Map<String, List<Document>> alfrescoMap = reader.readValue(alfrescoResponse);

            int n = 0;
            for (Entry<String, List<Document>> entry : alfrescoMap.entrySet()) {
                n = n + entry.getValue().size();
            }

            model.addAttribute("numberNewAlfresco", n);

        } catch (MessageBusException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "view";

    }

}
