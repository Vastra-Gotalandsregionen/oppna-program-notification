package se.vgregion.notifications.service;

import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.util.Random;

/**
 * Class which exposes methods as REST service methods.
 *
 * @author Patrik Bergström
 */

@Path("/n")
@Produces("application/json;charset=UTF-8")
public class TestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestService.class);

    /**
     * Get recently modified {@link se.vgregion.alfrescoclient.domain.Document}s for a user in a list of
     * {@link se.vgregion.alfrescoclient.domain.Site}s.
     *
     * @param userId the user id
     * @return the list {@link se.vgregion.alfrescoclient.domain.Site}s with recently modified
     * {@link se.vgregion.alfrescoclient.domain.Site}s in JSON format
     */
    @Path("/alfresco/{userId}")
    @GET
    public String getAlfresco(@PathParam("userId") String userId) {
        String msg = getFromMessageBus("vgr/alfresco_recently_modified_json", userId);
        return msg;
    }

    /**
     * Get the number Raindance invoices for a user.
     *
     * @param userId the user id
     * @return the number of invoices
     */
    @Path("/invoices/{userId}")
    @GET
    public String getInvoices(@PathParam("userId") String userId) {
        String msg = getFromMessageBus("vgr/raindance_invoices_count", userId);
        return msg;
    }

    /**
     * Get the Raindance invoices for a user.
     *
     * @param userId the user id
     * @return the invoices in JSON format
     */
    @Path("/invoices/json/{userId}")
    @Produces("application/json")
    @GET
    public String getInvoicesJson(@PathParam("userId") String userId) {
        String msg = getFromMessageBus("vgr/raindance_invoices_json", userId);
        return msg;
    }

    /**
     * Get the number of unread emails for a user.
     *
     * @param userId the user id
     * @return the number of unread emails
     */
    @Path("/mail/{userId}")
    @GET
    public String getMail(@PathParam("userId") String userId) {
        System.out.println("getAlfresco userId = " + userId);
        String msg = getFromMessageBus("vgr/notes_email_count", userId);
        return msg;
    }

    /**
     * Get the number of calendar events for the current day for a user.
     *
     * @param userId the user id
     * @return number of calendar events
     */
    @Path("/calendar/{userId}")
    @GET
    public String getCalendar(@PathParam("userId") String userId) {
        String msg = getFromMessageBus("vgr/notes_calendar_count", userId);
        return msg;
    }

    /**
     * Get the USD issues for a user.
     *
     * @param userId the user id
     * @return the USD issues in JSON format
     */
    @Path("/usd/{userId}")
    @GET
    public String getUsd(@PathParam("userId") String userId) {
        String msg = getFromMessageBus("vgr/usd_issues_json", userId);
        return msg;
    }

    private String getFromMessageBus(String dest, String userId) {
        String msg;
        Message message = new Message();
        message.setPayload(userId == null ? "" : userId);

        Object response;
        try {
            LOGGER.info("message send");
            final int timeout = 10000;
            response = MessageBusUtil.sendSynchronousMessage(dest, message, timeout);
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

    /**
     * Get a random number.
     *
     * @return a random number
     */
    @POST
    public String getRandom() {
        String msg = get();
        System.out.println("POST: " + msg);
        return msg;
    }

    /**
     * Get a random number.
     *
     * @return a random number
     */
    @GET
    public String random() {
        String msg = get();
        System.out.println("GET: " + msg);
        return msg;
    }

    private String get() {
        Random random = new Random();
        final int n = 100;
        return random.nextInt(n) + "";
    }
}
