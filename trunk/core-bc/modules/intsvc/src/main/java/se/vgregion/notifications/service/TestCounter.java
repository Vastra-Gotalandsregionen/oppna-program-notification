package se.vgregion.notifications.service;

import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.ws.rs.*;
import java.util.HashMap;
import java.util.Random;

/**
 * User: pabe
 * Date: 2011-08-02
 * Time: 10:06
 */

@Path("/n")
@Produces("application/json")
public class TestCounter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCounter.class);

    @Path("/alfresco")
    @GET
    public String getAlfresco() {
        String msg = getFromMessageBus("vgr/notification_alfresco", null);
        return msg;
    }

    @Path("/invoices/{userId}")
    @GET
    public String getInvoices(@PathParam("userId") String userId) {
        String msg = getFromMessageBus("vgr/raindance_invoices_count", userId);
        return msg;
    }

    @Path("/mail/{userId}")
    @GET
    public String getMail(@PathParam("userId") String userId) {
        System.out.println("getAlfresco userId = " + userId);
        String msg = getFromMessageBus("vgr/notes_email_count", userId);
        return msg;
    }

    @Path("/calendar/{userId}")
    @GET
    public String getCalendar(@PathParam("userId") String userId) {
        String msg = getFromMessageBus("vgr/notes_calendar_count", userId);
        return msg;
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

    @POST
    public String getRandom() {
        String msg = get();
        System.out.println("POST: " + msg);
        return msg;
    }

    @GET
    public String random() {
        String msg = get();
        System.out.println("GET: " + msg);
        return msg;
    }

    private String get() {
        Random random = new Random();
        return random.nextInt(100) + "";
    }
}
