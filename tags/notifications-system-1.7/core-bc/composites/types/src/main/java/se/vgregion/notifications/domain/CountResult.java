package se.vgregion.notifications.domain;

/**
 * A class representing a result containing a count and/or a message.
 *
 * @author Patrik Bergström
 */
public class CountResult {

    private String message;
    private Integer count;

    /**
     * Factory method with message provided.
     *
     * @param message the message
     * @return the result
     */
    public static CountResult createWithMessage(String message) {
        CountResult cr = new CountResult();
        cr.setMessage(message);
        return cr;
    }

    /**
     * Factory method with count provided.
     *
     * @param count the count
     * @return the result
     */
    public static CountResult createWithCount(Integer count) {
        CountResult cr = new CountResult();
        cr.setCount(count);
        return cr;
    }

    /**
     * Factory method to create a result with null count and no message.
     *
     * @return the result
     */
    public static CountResult createNullResult() {
        return createWithCount(null);
    }

    /**
     * Constructor.
     */
    public CountResult() {

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CountResult that = (CountResult) o;

        if (count != null ? !count.equals(that.count) : that.count != null) {
            return false;
        }
        if (message != null ? !message.equals(that.message) : that.message != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = message != null ? message.hashCode() : 0;
        final int prime = 31;
        result = prime * result + (count != null ? count.hashCode() : 0);
        return result;
    }

}
