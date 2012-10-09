package se.vgregion.notifications;

/**
 * Exception used when a {@code se.vgregion.portal.cs.domain.UserSiteCredential} is not found.
 *
 * @author Patrik Bergström
 */
public class UserSiteCredentialNotFoundException extends NotificationException {

    /**
     * Constructor.
     *
     * @param message the message
     */
    public UserSiteCredentialNotFoundException(String message) {
        super(message);
    }
}
