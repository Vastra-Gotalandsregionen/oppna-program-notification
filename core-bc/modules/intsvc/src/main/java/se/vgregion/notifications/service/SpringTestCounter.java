package se.vgregion.notifications.service;

import java.util.HashMap;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;

/**
 * @author Patrik Bergström
 * @author Simon Göransson
 */

@Controller
public class SpringTestCounter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringTestCounter.class);

    @RequestMapping("/getCount")
    public ModelAndView getRandom() {
        String msg = getRandomNumber();
        System.out.println("GET: " + msg);
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("val", msg);
        return new ModelAndView("value", model);
    }

    @RequestMapping("/getAlfresco")
    public ModelAndView getAlfresco() {
        String msg = getFromMessageBus("vgr/notificationalfresco", null);
        System.out.println("getAlfresco message = " + msg);
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("val", msg);
        return new ModelAndView("value", model);
    }

    @RequestMapping("/getMail")
    public ModelAndView getMail(@RequestParam("userId") String userId) {

        System.out.println("getAlfresco userId = " + userId);

        String msg = getFromMessageBus("vgr/counter_notes_email", userId);
        System.out.println("getAlfresco message = " + msg);
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("val", msg);
        return new ModelAndView("value", model);
    }

    @RequestMapping("/getCalendar")
    public ModelAndView getCalendar(@RequestParam("userId") String userId) {
        String msg = getFromMessageBus("vgr/counter_notes_calendar", userId);
        // System.out.println("getAlfresco message = " + msg);
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("val", msg);
        return new ModelAndView("value", model);
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
            LOGGER.info(e.getMessage());
            response = "-";
        }

        if (response instanceof String) {
            msg = response.toString();
        } else {
            if (response instanceof Exception) {
                ((Exception) response).printStackTrace();
            }
            msg = "-";
        }
        return msg;
    }

    private String getRandomNumber() {
        Random random = new Random();
        return random.nextInt(100) + "";
    }

}
