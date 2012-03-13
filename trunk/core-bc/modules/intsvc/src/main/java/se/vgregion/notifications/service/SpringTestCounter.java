package se.vgregion.notifications.service;

import java.util.HashMap;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;

/**
 * @author Patrik Bergström
 */

@Controller
public class SpringTestCounter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringTestCounter.class);

    @RequestMapping("/getCount")
    public ModelAndView getRandom() {
        String msg = get();
        System.out.println("GET: " + msg);
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("val", msg);
        return new ModelAndView("value", model);
    }

    private String get() {
        Random random = new Random();
        return random.nextInt(100) + "";
    }

    @RequestMapping("/getAlfresco")
    public ModelAndView getAlfresco() {

        Message message = new Message();
        // message.setPayload(userId == null ? "" : userId);

        Object response;
        try {
            response = MessageBusUtil.sendSynchronousMessage("vgr/notification/alfresco", message, 10000);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            response = "-";
        }

        String msg;

        if (response instanceof String) {
            msg = response.toString();
        } else {
            if (response instanceof Exception) {
                ((Exception) response).printStackTrace();
            }
            msg = "-";
        }

        System.out.println("GET: " + msg);
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("val", msg);
        return new ModelAndView("value", model);
    }
}
