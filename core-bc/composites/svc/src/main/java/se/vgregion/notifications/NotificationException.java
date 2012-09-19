package se.vgregion.notifications;

/**
 * Exception class for exceptions related to notifications.
 *
 * @author Patrik Bergström
 */
public class NotificationException extends Exception {

    /**
     * Constructor.
     *
     * @param cause the cause
     */
    public NotificationException(Throwable cause) {
        super(cause);
    }

    public NotificationException(String message) {
        super(message);
    }
}
